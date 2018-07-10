package com.machao.learning.concurrent.LinkedBlockingQueue;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * LinkedBlockingQueue是一个由单向链表结构支持的可选是否有界的BlockingQueue阻塞队列，不允许null值。
 * 此队列按照FIFO(先进先出)原则对队列中的元素进行排序。
 * 队列的头部元素是队列中储存时间最长的元素。
 * 队列的尾部元素是队列中储存时间最短的元素。
 * 新元素会插入到队列尾部，元素检索操作会从队列的头部开始。
 * 链接队列通常具有比基于数组的队列更高的吞吐量，但在大多数并发应用程序中的预测性能较低。
 * 
 * @since 1.5
 * @author Doug Lea
 * @param <E> the type of elements held in this collection
 */
public class LinkedBlockingQueue<E> extends AbstractQueue<E>
        implements BlockingQueue<E>, java.io.Serializable {
    private static final long serialVersionUID = -6903933977591709194L;

    /*
     * A variant of the "two lock queue" algorithm.  The putLock gates
     * entry to put (and offer), and has an associated condition for
     * waiting puts.  Similarly for the takeLock.  The "count" field
     * that they both rely on is maintained as an atomic to avoid
     * needing to get both locks in most cases. Also, to minimize need
     * for puts to get takeLock and vice-versa, cascading notifies are
     * used. When a put notices that it has enabled at least one take,
     * it signals taker. That taker in turn signals others if more
     * items have been entered since the signal. And symmetrically for
     * takes signalling puts. Operations such as remove(Object) and
     * iterators acquire both locks.
     *
     * Visibility between writers and readers is provided as follows:
     *
     * Whenever an element is enqueued, the putLock is acquired and
     * count updated.  A subsequent reader guarantees visibility to the
     * enqueued Node by either acquiring the putLock (via fullyLock)
     * or by acquiring the takeLock, and then reading n = count.get();
     * this gives visibility to the first n items.
     *
     * To implement weakly consistent iterators, it appears we need to
     * keep all Nodes GC-reachable from a predecessor dequeued Node.
     * That would cause two problems:
     * - allow a rogue Iterator to cause unbounded memory retention
     * - cause cross-generational linking of old Nodes to new Nodes if
     *   a Node was tenured while live, which generational GCs have a
     *   hard time dealing with, causing repeated major collections.
     * However, only non-deleted Nodes need to be reachable from
     * dequeued Nodes, and reachability does not necessarily have to
     * be of the kind understood by the GC.  We use the trick of
     * linking a Node that has just been dequeued to itself.  Such a
     * self-link implicitly means to advance to head.next.
     */

    /**
     * 链表中储存元素的节点
     */
    static class Node<E> {
        E item;

        /**
         * One of:
         * - the real successor Node
         * - this Node, meaning the successor is head.next
         * - null, meaning there is no successor (this is the last node)
         */
        Node<E> next;

        Node(E x) { item = x; }
    }

    /** 容量限制，如果没有，则为Integer.MAX_VALUE */
    private final int capacity;

    /** 当前队列中的元素数 */
    private final AtomicInteger count = new AtomicInteger();

    /**
     * 队列（链表）中的头元素
     */
    transient Node<E> head;

    /**
     * 队列（链表）结尾元素
     */
    private transient Node<E> last;

    /** 出队操作的可重入锁(peek/poll/take/remove) */
    private final ReentrantLock takeLock = new ReentrantLock();

    /** 出队操作的等待条件 */
    private final Condition notEmpty = takeLock.newCondition();

    /** 入队操作的可重入锁(put/add/offer) */
    private final ReentrantLock putLock = new ReentrantLock();

    /** 入队操作的等待条件 */
    private final Condition notFull = putLock.newCondition();

    /**
     * 唤醒在notEmpty条件上等待的线程
     * 此方法只在put/offer中使用
     */
    private void signalNotEmpty() {
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            notEmpty.signal();
        } finally {
            takeLock.unlock();
        }
    }

    /**
     * 唤醒notFull条件上阻塞的线程
     * 
     * 此方法由take/poll方法调用
     */
    private void signalNotFull() {
        final ReentrantLock putLock = this.putLock;
        putLock.lock();
        try {
            notFull.signal();
        } finally {
            putLock.unlock();
        }
    }

    /**
     * 入队操作
     * 将指定节点插入到队列尾部
     */
    private void enqueue(Node<E> node) {
        // assert putLock.isHeldByCurrentThread();
        // assert last.next == null;
    	// 封装新节点，并赋给当前的最后一个节点的下一个节点，然后在将这个节点设为最后一个节点
        last = last.next = node;
    }

    /**
     * 出队操作
     * 从队列头部移除一个节点
     * @return the node
     */
    private E dequeue() {
        // assert takeLock.isHeldByCurrentThread();
        // assert head.item == null;
        Node<E> h = head; //获取头结点
        Node<E> first = h.next; //将头结点的下一个节点赋值给first
        h.next = h; // 将当前要出队的节点置为null,有助于GC回收
        head = first; //将要出队的节点置为头结点
        E x = first.item; //获取要出队的节点值
        first.item = null; //将头结点值置为null
        return x;// 返回出队的节点值
    }

    /**
     * 同时获取入队锁与出队锁
     */
    void fullyLock() {
        putLock.lock();
        takeLock.lock();
    }

    /**
     * Unlocks to allow both puts and takes.
     */
    void fullyUnlock() {
        takeLock.unlock();
        putLock.unlock();
    }

//     /**
//      * Tells whether both locks are held by current thread.
//      */
//     boolean isFullyLocked() {
//         return (putLock.isHeldByCurrentThread() &&
//                 takeLock.isHeldByCurrentThread());
//     }

    /**
     * 创建一个容量为Integer.MAX_VALUE的阻塞队列
     */
    public LinkedBlockingQueue() {
        this(Integer.MAX_VALUE);
    }

    /**
     * 创建一个指定容量的阻塞队列
     * 
     * 如果指定的容量小于1则抛出IllegaArgumentException异常
     *
     */
    public LinkedBlockingQueue(int capacity) {
    	// 指定容量必须大于0，否则抛出异常
        if (capacity <= 0) throw new IllegalArgumentException();
        this.capacity = capacity;
        // 创建一个值为null的节点赋值给头结点，然后让尾部节点指向头结点
        last = head = new Node<E>(null);
    }

    /**
     * Creates a {@code LinkedBlockingQueue} with a capacity of
     * {@link Integer#MAX_VALUE}, initially containing the elements of the
     * given collection,
     * added in traversal order of the collection's iterator.
     *
     * @param c the collection of elements to initially contain
     * @throws NullPointerException if the specified collection or any
     *         of its elements are null
     */
    public LinkedBlockingQueue(Collection<? extends E> c) {
    	// 创建无界阻塞队列
        this(Integer.MAX_VALUE);
        // 获取入队的可重入锁
        final ReentrantLock putLock = this.putLock;
        putLock.lock(); // Never contended, but necessary for visibility
        try {
            int n = 0;
            // 遍历此集合
            for (E e : c) {
                if (e == null) // 如果集合中有null值则抛出空指针异常
                    throw new NullPointerException();
                if (n == capacity) // 当集合中元素数量大于此队列的容量时抛出异常
                    throw new IllegalStateException("Queue full");
                // 调用入队方法
                enqueue(new Node<E>(e));
                ++n;// 索引+1
            }
            count.set(n);// 原子操作设置当前队列包含的元素数
        } finally {
            putLock.unlock();
        }
    }

    // this doc comment is overridden to remove the reference to collections
    // greater in size than Integer.MAX_VALUE
    /**
     * Returns the number of elements in this queue.
     *
     * @return the number of elements in this queue
     */
    public int size() {
        return count.get();
    }

    // this doc comment is a modified copy of the inherited doc comment,
    // without the reference to unlimited queues.
    /**
     * Returns the number of additional elements that this queue can ideally
     * (in the absence of memory or resource constraints) accept without
     * blocking. This is always equal to the initial capacity of this queue
     * less the current {@code size} of this queue.
     *
     * <p>Note that you <em>cannot</em> always tell if an attempt to insert
     * an element will succeed by inspecting {@code remainingCapacity}
     * because it may be the case that another thread is about to
     * insert or remove an element.
     */
    public int remainingCapacity() {
        return capacity - count.get();
    }

    /**
     * 将指定元素插入到此队列的尾部，队列满则阻塞等待
     * 
     * 如果此队列已满则当前线程阻塞直至队列不满或当前线程被其他线程中断
     * 如果要插入的元素为null则抛出NullPointerException异常
     * 如果当前线程被中断则抛出InterruptedException异常
     */
    public void put(E e) throws InterruptedException {
    	// 插入的元素必须非空，否则抛出异常
        if (e == null) throw new NullPointerException();
        // Note: convention in all put/take/etc is to preset local var
        // holding count negative to indicate failure unless set.
        int c = -1;
        Node<E> node = new Node<E>(e); // 创建要插入的节点
        final ReentrantLock putLock = this.putLock; //入队锁
        final AtomicInteger count = this.count; // 获取当前队列元素个数 
        putLock.lockInterruptibly(); //获取可中断锁
        try {
            /*
             * Note that count is used in wait guard even though it is
             * not protected by lock. This works because count can
             * only decrease at this point (all other puts are shut
             * out by lock), and we (or some other waiting put) are
             * signalled if it ever changes from capacity. Similarly
             * for all other uses of count in other wait guards.
             */
            while (count.get() == capacity) {
                /*
                 *  如果当前队列已满，则阻塞
                 *  直到队列不满被其他线程唤醒，或当前线程被中断
                 */
            	notFull.await(); 
            }
            // 调用入队操作将新节点插入到队列尾部
            enqueue(node);
            // 入队数量+1
            c = count.getAndIncrement();
            if (c + 1 < capacity)
            	// 当队列不满时唤醒在notFull条件上等待的线程
                notFull.signal();
        } finally {
            putLock.unlock();
        }
        if (c == 0)
        	// 如果当前队列为空，唤醒在notEmpty条件上等待的线程
            signalNotEmpty(); 
    }

    /**
     * 将指定的元素插入到此队列尾部，成功立即返回true，队列满则阻塞等待
     * 
     * 如果队列已满且阻塞时间超出指定时间则返回false
     * 如果插入的元素为null则抛出NullPointerException异常
     * 如果当前线程被中断则抛出InterruptedException异常
     * 
     */
    public boolean offer(E e, long timeout, TimeUnit unit)
        throws InterruptedException {
    	//插入的元素必须非空，否则抛出异常
        if (e == null) throw new NullPointerException();
        long nanos = unit.toNanos(timeout); // 获取超时毫秒数
        int c = -1;
        final ReentrantLock putLock = this.putLock; // 入队锁
        final AtomicInteger count = this.count; // 获取当前队列元素个数
        putLock.lockInterruptibly(); // 获取可中断锁
        try {
            while (count.get() == capacity) { // 容量检查
                if (nanos <= 0)
                	// 超出阻塞时间则返回false
                    return false;
                //队列容量已满，则阻塞，当阻塞时间达到指定的超时时间后被唤醒
                nanos = notFull.awaitNanos(nanos);
            }
            // 创建新节点，调用入队操作将新节点插入到此队列尾部
            enqueue(new Node<E>(e));
            // 此队列元素个数+1
            c = count.getAndIncrement();
            if (c + 1 < capacity)
                notFull.signal();// 队列不满唤醒notFull条件上阻塞的线程
        } finally {
            putLock.unlock();
        }
        if (c == 0)
        	// 如果队列为空唤醒notEmpty条件上阻塞的线程
            signalNotEmpty();
        return true;
    }

    /**
     * 将指定元素插入此队列尾部，成功返回true,失败或队列已满立即返回false
     * 
     * 如果元素为null则抛出NullPointerException异常
     */
    public boolean offer(E e) {
    	// 插入的元素必须非空，否则抛出异常
        if (e == null) throw new NullPointerException();
        // 获取此队列中元素个数
        final AtomicInteger count = this.count;
        // 如果此队列已满则立即返回false
        if (count.get() == capacity)
            return false;
        int c = -1;
        Node<E> node = new Node<E>(e);// 创建新节点
        final ReentrantLock putLock = this.putLock; //入队锁
        putLock.lock(); //获取可重入锁
        try {
            if (count.get() < capacity) {// 二次验证此队列是否已满
            	// 调用入队操作将新节点插入此队列尾部
                enqueue(node);
                // 队列中元素个数+1
                c = count.getAndIncrement();
                if (c + 1 < capacity)
                	// 如果队列不满则唤醒notFull条件上阻塞的线程
                    notFull.signal();
            }
        } finally {
            putLock.unlock();
        }
        if (c == 0)
        	// 队列为空则唤醒notEmpty条件上阻塞的线程
            signalNotEmpty();
        return c >= 0;// 如果插入成功返回true
    }

    /**
     * 检索并删除此队列的头元素，为空则阻塞等待
     * 
     * 如果当前线程被中断则抛出InterruptedException异常
     */
    public E take() throws InterruptedException {
        E x;
        int c = -1;
        final AtomicInteger count = this.count; // 当前队列元素个数
        final ReentrantLock takeLock = this.takeLock; // 获取出队锁
        takeLock.lockInterruptibly(); // 获取可中断锁
        try {
            while (count.get() == 0) {
            	// 如果队列为空则阻塞
                notEmpty.await();
            }
            // 调用出队操作将队列头部元素取出
            x = dequeue();
            // 队列元素个数-1
            c = count.getAndDecrement();
            if (c > 1)
            	// 队列不为空则唤醒notEmpty条件上阻塞的线程
                notEmpty.signal();
        } finally {
            takeLock.unlock();
        }
        if (c == capacity)
        	// 队列满了则唤醒notFull条件上阻塞的线程
            signalNotFull();
        return x; // 返回取出的元素
    }

    /**
     * 检索并删除此队列的头元素，如果队列为空则阻塞
     * 
     * 如果阻塞时间超出指定的超时时间则返回null
     * 如果当前线程被中断则抛出InterruptedException异常
     */
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        E x = null;
        int c = -1;
        long nanos = unit.toNanos(timeout);// 获取超时毫秒数
        final AtomicInteger count = this.count; //当前队列中的元素个数
        final ReentrantLock takeLock = this.takeLock; //获取出队锁
        takeLock.lockInterruptibly(); //获取可中断锁
        try {
            while (count.get() == 0) { // 容量检查
                if (nanos <= 0)
                	// 如果阻塞超时则返回null
                    return null;
                // 如果队列为空，则阻塞，当阻塞时间超过设置的超时时间时唤醒当前线程
                nanos = notEmpty.awaitNanos(nanos);
            }
            // 调用出队方法取出此队列的头元素
            x = dequeue();
            // 队列元素个数-1
            c = count.getAndDecrement();
            if (c > 1)
            	// 如果队列不为空则唤醒notEmpty条件上阻塞的线程
                notEmpty.signal();
        } finally {
            takeLock.unlock();
        }
        if (c == capacity)
        	// 如果队列已满则唤醒
            signalNotFull();
        return x; // 返回取出的头元素
    }

    /**
     * 检索并删除此队列的头元素，如果队列为空则返回null
     */
    public E poll() {
        final AtomicInteger count = this.count; // 当前队列元素个数
        if (count.get() == 0) 
        	// 如果队列为空则返回null
            return null;
        E x = null;
        int c = -1;
        final ReentrantLock takeLock = this.takeLock; // 获取出队锁
        takeLock.lock(); // 获取可重入锁
        try {
            if (count.get() > 0) { //二次校验队列不为空
                x = dequeue();// 调用出队操作将此队列头部元素取出
                // 此队列元素个数-1
                c = count.getAndDecrement();
                if (c > 1)
                	// 如果队列不为空则唤醒notEmpty条件上阻塞的线程
                    notEmpty.signal();
            }
        } finally {
            takeLock.unlock();
        }
        if (c == capacity)
        	// 如果队列已满则唤醒notFull条件上阻塞的线程
            signalNotFull();
        return x; //返回取出的头元素
    }

    /**
     * 检索此队列的头元素,如果队列为空则返回null
     * 
     */
    public E peek() {
        if (count.get() == 0)
        	// 队列为空则返回null
            return null;
        final ReentrantLock takeLock = this.takeLock; // 出队锁
        takeLock.lock(); // 加锁
        try {
            Node<E> first = head.next; // 将头节点赋值给first
            if (first == null)// 二次校验队列非空，如果first为null则返回null
                return null;
            else
                return first.item; //返回头节点的值
        } finally {
            takeLock.unlock();
        }
    }

    /**
     * 取消链表内部p节点与trail节点的连接。
     */
    void unlink(Node<E> p, Node<E> trail) {
        // assert isFullyLocked();
        // p.next is not changed, to allow iterators that are
        // traversing p to maintain their weak-consistency guarantee.
        p.item = null;// 将p的值置为null
        trail.next = p.next;// 将trail的下一个节点与p的下一个节点连接
        if (last == p)
        	// 如果p是尾节点 将尾节点设置为trail
            last = trail;
        // 队列中元素个数-1
        if (count.getAndDecrement() == capacity)
        	// 如果队列满则唤醒notFull条件上阻塞的线程
            notFull.signal();
    }

    /**
     * 从此队列删除指定元素，成功返回true，失败返回false
     * 如果此队列包含多个这样的元素则从队列头部开始查找删除一个
     */
    public boolean remove(Object o) {
    	// 要移除的对象必须非空，否则返回false
        if (o == null) return false;
        fullyLock(); // 同时将入队锁与出队锁锁住
        try {
        	// 从队列头部开始遍历链表，删除指定节点
            for (Node<E> trail = head, p = trail.next;
                 p != null;
                 trail = p, p = p.next) {
                if (o.equals(p.item)) {
                	// 取消链表内部p节点与trail节点的连接。
                    unlink(p, trail);
                    // 成功后返回true
                    return true;
                }
            }
            // 如果失败返回false
            return false;
        } finally {
            fullyUnlock();
        }
    }

    /**
     * Returns {@code true} if this queue contains the specified element.
     * More formally, returns {@code true} if and only if this queue contains
     * at least one element {@code e} such that {@code o.equals(e)}.
     *
     * @param o object to be checked for containment in this queue
     * @return {@code true} if this queue contains the specified element
     */
    public boolean contains(Object o) {
        if (o == null) return false;
        fullyLock();
        try {
            for (Node<E> p = head.next; p != null; p = p.next)
                if (o.equals(p.item))
                    return true;
            return false;
        } finally {
            fullyUnlock();
        }
    }

    /**
     * Returns an array containing all of the elements in this queue, in
     * proper sequence.
     *
     * <p>The returned array will be "safe" in that no references to it are
     * maintained by this queue.  (In other words, this method must allocate
     * a new array).  The caller is thus free to modify the returned array.
     *
     * <p>This method acts as bridge between array-based and collection-based
     * APIs.
     *
     * @return an array containing all of the elements in this queue
     */
    public Object[] toArray() {
        fullyLock();
        try {
            int size = count.get();
            Object[] a = new Object[size];
            int k = 0;
            for (Node<E> p = head.next; p != null; p = p.next)
                a[k++] = p.item;
            return a;
        } finally {
            fullyUnlock();
        }
    }

    /**
     * Returns an array containing all of the elements in this queue, in
     * proper sequence; the runtime type of the returned array is that of
     * the specified array.  If the queue fits in the specified array, it
     * is returned therein.  Otherwise, a new array is allocated with the
     * runtime type of the specified array and the size of this queue.
     *
     * <p>If this queue fits in the specified array with room to spare
     * (i.e., the array has more elements than this queue), the element in
     * the array immediately following the end of the queue is set to
     * {@code null}.
     *
     * <p>Like the {@link #toArray()} method, this method acts as bridge between
     * array-based and collection-based APIs.  Further, this method allows
     * precise control over the runtime type of the output array, and may,
     * under certain circumstances, be used to save allocation costs.
     *
     * <p>Suppose {@code x} is a queue known to contain only strings.
     * The following code can be used to dump the queue into a newly
     * allocated array of {@code String}:
     *
     *  <pre> {@code String[] y = x.toArray(new String[0]);}</pre>
     *
     * Note that {@code toArray(new Object[0])} is identical in function to
     * {@code toArray()}.
     *
     * @param a the array into which the elements of the queue are to
     *          be stored, if it is big enough; otherwise, a new array of the
     *          same runtime type is allocated for this purpose
     * @return an array containing all of the elements in this queue
     * @throws ArrayStoreException if the runtime type of the specified array
     *         is not a supertype of the runtime type of every element in
     *         this queue
     * @throws NullPointerException if the specified array is null
     */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        fullyLock();
        try {
            int size = count.get();
            if (a.length < size)
                a = (T[])java.lang.reflect.Array.newInstance
                    (a.getClass().getComponentType(), size);

            int k = 0;
            for (Node<E> p = head.next; p != null; p = p.next)
                a[k++] = (T)p.item;
            if (a.length > k)
                a[k] = null;
            return a;
        } finally {
            fullyUnlock();
        }
    }

    public String toString() {
        fullyLock();
        try {
            Node<E> p = head.next;
            if (p == null)
                return "[]";

            StringBuilder sb = new StringBuilder();
            sb.append('[');
            for (;;) {
                E e = p.item;
                sb.append(e == this ? "(this Collection)" : e);
                p = p.next;
                if (p == null)
                    return sb.append(']').toString();
                sb.append(',').append(' ');
            }
        } finally {
            fullyUnlock();
        }
    }

    /**
     * Atomically removes all of the elements from this queue.
     * The queue will be empty after this call returns.
     */
    public void clear() {
        fullyLock();
        try {
            for (Node<E> p, h = head; (p = h.next) != null; h = p) {
                h.next = h;
                p.item = null;
            }
            head = last;
            // assert head.item == null && head.next == null;
            if (count.getAndSet(0) == capacity)
                notFull.signal();
        } finally {
            fullyUnlock();
        }
    }

    /**
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     */
    public int drainTo(Collection<? super E> c) {
        return drainTo(c, Integer.MAX_VALUE);
    }

    /**
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     */
    public int drainTo(Collection<? super E> c, int maxElements) {
        if (c == null)
            throw new NullPointerException();
        if (c == this)
            throw new IllegalArgumentException();
        if (maxElements <= 0)
            return 0;
        boolean signalNotFull = false;
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            int n = Math.min(maxElements, count.get());
            // count.get provides visibility to first n Nodes
            Node<E> h = head;
            int i = 0;
            try {
                while (i < n) {
                    Node<E> p = h.next;
                    c.add(p.item);
                    p.item = null;
                    h.next = h;
                    h = p;
                    ++i;
                }
                return n;
            } finally {
                // Restore invariants even if c.add() threw
                if (i > 0) {
                    // assert h.item == null;
                    head = h;
                    signalNotFull = (count.getAndAdd(-i) == capacity);
                }
            }
        } finally {
            takeLock.unlock();
            if (signalNotFull)
                signalNotFull();
        }
    }

    /**
     * Returns an iterator over the elements in this queue in proper sequence.
     * The elements will be returned in order from first (head) to last (tail).
     *
     * <p>The returned iterator is
     * <a href="package-summary.html#Weakly"><i>weakly consistent</i></a>.
     *
     * @return an iterator over the elements in this queue in proper sequence
     */
    public Iterator<E> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<E> {
        /*
         * Basic weakly-consistent iterator.  At all times hold the next
         * item to hand out so that if hasNext() reports true, we will
         * still have it to return even if lost race with a take etc.
         */

        private Node<E> current;
        private Node<E> lastRet;
        private E currentElement;

        Itr() {
            fullyLock();
            try {
                current = head.next;
                if (current != null)
                    currentElement = current.item;
            } finally {
                fullyUnlock();
            }
        }

        public boolean hasNext() {
            return current != null;
        }

        /**
         * Returns the next live successor of p, or null if no such.
         *
         * Unlike other traversal methods, iterators need to handle both:
         * - dequeued nodes (p.next == p)
         * - (possibly multiple) interior removed nodes (p.item == null)
         */
        private Node<E> nextNode(Node<E> p) {
            for (;;) {
                Node<E> s = p.next;
                if (s == p)
                    return head.next;
                if (s == null || s.item != null)
                    return s;
                p = s;
            }
        }

        public E next() {
            fullyLock();
            try {
                if (current == null)
                    throw new NoSuchElementException();
                E x = currentElement;
                lastRet = current;
                current = nextNode(current);
                currentElement = (current == null) ? null : current.item;
                return x;
            } finally {
                fullyUnlock();
            }
        }

        public void remove() {
            if (lastRet == null)
                throw new IllegalStateException();
            fullyLock();
            try {
                Node<E> node = lastRet;
                lastRet = null;
                for (Node<E> trail = head, p = trail.next;
                     p != null;
                     trail = p, p = p.next) {
                    if (p == node) {
                        unlink(p, trail);
                        break;
                    }
                }
            } finally {
                fullyUnlock();
            }
        }
    }

    /** A customized variant of Spliterators.IteratorSpliterator */
    static final class LBQSpliterator<E> implements Spliterator<E> {
        static final int MAX_BATCH = 1 << 25;  // max batch array size;
        final LinkedBlockingQueue<E> queue;
        Node<E> current;    // current node; null until initialized
        int batch;          // batch size for splits
        boolean exhausted;  // true when no more nodes
        long est;           // size estimate
        LBQSpliterator(LinkedBlockingQueue<E> queue) {
            this.queue = queue;
            this.est = queue.size();
        }

        public long estimateSize() { return est; }

        public Spliterator<E> trySplit() {
            Node<E> h;
            final LinkedBlockingQueue<E> q = this.queue;
            int b = batch;
            int n = (b <= 0) ? 1 : (b >= MAX_BATCH) ? MAX_BATCH : b + 1;
            if (!exhausted &&
                ((h = current) != null || (h = q.head.next) != null) &&
                h.next != null) {
                Object[] a = new Object[n];
                int i = 0;
                Node<E> p = current;
                q.fullyLock();
                try {
                    if (p != null || (p = q.head.next) != null) {
                        do {
                            if ((a[i] = p.item) != null)
                                ++i;
                        } while ((p = p.next) != null && i < n);
                    }
                } finally {
                    q.fullyUnlock();
                }
                if ((current = p) == null) {
                    est = 0L;
                    exhausted = true;
                }
                else if ((est -= i) < 0L)
                    est = 0L;
                if (i > 0) {
                    batch = i;
                    return Spliterators.spliterator
                        (a, 0, i, Spliterator.ORDERED | Spliterator.NONNULL |
                         Spliterator.CONCURRENT);
                }
            }
            return null;
        }

        public void forEachRemaining(Consumer<? super E> action) {
            if (action == null) throw new NullPointerException();
            final LinkedBlockingQueue<E> q = this.queue;
            if (!exhausted) {
                exhausted = true;
                Node<E> p = current;
                do {
                    E e = null;
                    q.fullyLock();
                    try {
                        if (p == null)
                            p = q.head.next;
                        while (p != null) {
                            e = p.item;
                            p = p.next;
                            if (e != null)
                                break;
                        }
                    } finally {
                        q.fullyUnlock();
                    }
                    if (e != null)
                        action.accept(e);
                } while (p != null);
            }
        }

        public boolean tryAdvance(Consumer<? super E> action) {
            if (action == null) throw new NullPointerException();
            final LinkedBlockingQueue<E> q = this.queue;
            if (!exhausted) {
                E e = null;
                q.fullyLock();
                try {
                    if (current == null)
                        current = q.head.next;
                    while (current != null) {
                        e = current.item;
                        current = current.next;
                        if (e != null)
                            break;
                    }
                } finally {
                    q.fullyUnlock();
                }
                if (current == null)
                    exhausted = true;
                if (e != null) {
                    action.accept(e);
                    return true;
                }
            }
            return false;
        }

        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.NONNULL |
                Spliterator.CONCURRENT;
        }
    }

    /**
     * Returns a {@link Spliterator} over the elements in this queue.
     *
     * <p>The returned spliterator is
     * <a href="package-summary.html#Weakly"><i>weakly consistent</i></a>.
     *
     * <p>The {@code Spliterator} reports {@link Spliterator#CONCURRENT},
     * {@link Spliterator#ORDERED}, and {@link Spliterator#NONNULL}.
     *
     * @implNote
     * The {@code Spliterator} implements {@code trySplit} to permit limited
     * parallelism.
     *
     * @return a {@code Spliterator} over the elements in this queue
     * @since 1.8
     */
    public Spliterator<E> spliterator() {
        return new LBQSpliterator<E>(this);
    }

    /**
     * Saves this queue to a stream (that is, serializes it).
     *
     * @param s the stream
     * @throws java.io.IOException if an I/O error occurs
     * @serialData The capacity is emitted (int), followed by all of
     * its elements (each an {@code Object}) in the proper order,
     * followed by a null
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {

        fullyLock();
        try {
            // Write out any hidden stuff, plus capacity
            s.defaultWriteObject();

            // Write out all elements in the proper order.
            for (Node<E> p = head.next; p != null; p = p.next)
                s.writeObject(p.item);

            // Use trailing null as sentinel
            s.writeObject(null);
        } finally {
            fullyUnlock();
        }
    }

    /**
     * Reconstitutes this queue from a stream (that is, deserializes it).
     * @param s the stream
     * @throws ClassNotFoundException if the class of a serialized object
     *         could not be found
     * @throws java.io.IOException if an I/O error occurs
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        // Read in capacity, and any hidden stuff
        s.defaultReadObject();

        count.set(0);
        last = head = new Node<E>(null);

        // Read in all elements and place in queue
        for (;;) {
            @SuppressWarnings("unchecked")
            E item = (E)s.readObject();
            if (item == null)
                break;
            add(item);
        }
    }
}
