package com.machao.learning.concurrent.ArrayBlockingQueue;

import java.lang.ref.WeakReference;
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ArrayBlockingQueue是一个由数组支持的有界的BlockingQueue阻塞队列；
 * 此队列按照FIFO（先进先出）规则对元素进行排序；
 * 队列的头部元素是队列中储存时间最长的元素，队列的尾部元素是队列中储存时间最短的元素；
 * 新元素插入到队列尾部，队列检索操作则是从队列头部开始获得元素；
 * 
 *
 * 这是一个典型的“有界缓存区”，固定大小的数组保存生产者插入的元素和消费者提取的元素。
 * 一旦创建了这样的缓存区，容量就不能改变。
 * 试图向已满的队列中插入元素会导致操作阻塞；
 * 试图从空队列中检索元素会导致类似的阻塞；
 */
public class ArrayBlockingQueue<E> extends AbstractQueue<E>
        implements BlockingQueue<E>, java.io.Serializable {

    /**
     * Serialization ID. This class relies on default serialization
     * even for the items array, which is default-serialized, even if
     * it is empty. Otherwise it could not be declared final, which is
     * necessary here.
     */
    private static final long serialVersionUID = -817911632652898426L;

    /** 保存元素的queue数组 */
    final Object[] items;

    /** 下一次出队(take、poll、peek或remove)操作的元素索引 */
    int takeIndex;

    /** 下一次入队(put、offer或add)操作的元素索引 */
    int putIndex;

    /** 队列中的元素数量 */
    int count;

    /*
     * Concurrency control uses the classic two-condition algorithm
     * found in any textbook.
     */

    /** 保护所有访问的主锁 */
    final ReentrantLock lock;

    /** 队列非空时的等待条件（出队操作等待条件） */
    private final Condition notEmpty;

    /** 队列非满时的等待条件（入队操作等待条件） */
    private final Condition notFull;

    /**
     * 当前活动迭代器的共享状态，如果不存在则返回null
     */
    transient Itrs itrs = null;

    // Internal helper methods

    /**
     * Circularly decrement i.
     */
    final int dec(int i) {
        return ((i == 0) ? items.length : i) - 1;
    }

    /**
     * Returns item at index i.
     */
    @SuppressWarnings("unchecked")
    final E itemAt(int i) {
        return (E) items[i];
    }

    /**
     * 检查传入对象不为Null
     * 如果为null，则抛出NullPointerException空指针异常
     *
     * @param v the element
     */
    private static void checkNotNull(Object v) {
        if (v == null)
            throw new NullPointerException();
    }

    /**
     * 入列
     * 将元素插入当前putIndex位置
     * 入列操作必须持有锁
     */
    private void enqueue(E x) {
        // assert lock.getHoldCount() == 1;
        // assert items[putIndex] == null;
        final Object[] items = this.items;
        //将值插入到当前位置
        items[putIndex] = x;
        // 如果入队索引等于数组长度
        if (++putIndex == items.length)
            // 重置入队索引
        	putIndex = 0;
        count++;// 累加数组中的元素数
        // 唤醒notEmpty条件上等待的线程
        notEmpty.signal();
    }

    /**
     * 出队
     * 从takeIndex索引位置取出元素，并将此位置赋值为null
     * 唤醒在notFull条件上等待的线程
     * 
     * 出队操作必须持有锁
     */
    private E dequeue() {
        // assert lock.getHoldCount() == 1;
        // assert items[takeIndex] != null;
        final Object[] items = this.items;
        @SuppressWarnings("unchecked")
        // 根据出队索引获取数组中索引位置的元素，并将该索引位置赋值为null
        // 出队指索引为数组中第一个元素的索引
        E x = (E) items[takeIndex];
        items[takeIndex] = null;
        // 检查出队索引等于数组长度
        if (++takeIndex == items.length)
        	// 重置出队索引
            takeIndex = 0;
        count--;// 数组中元素数量
        if (itrs != null)
            itrs.elementDequeued();
        // 唤醒notFull条件上等待的线程
        notFull.signal();
        return x;
    }

    /**
     * 删除指定索引位置的元素
     * 
     * 此操作必须持有锁，操作成功后会唤醒notFull条件上等待的线程
     */
    void removeAt(final int removeIndex) {
        // assert lock.getHoldCount() == 1;
        // assert items[removeIndex] != null;
        // assert removeIndex >= 0 && removeIndex < items.length;
        final Object[] items = this.items;
        if (removeIndex == takeIndex) {// 如果要删除的元素是此队列的头索引
            // removing front item; just advance
        	// 将此索引位置赋值为null
            items[takeIndex] = null;
            // 如果出队索引等于数组长度
            if (++takeIndex == items.length)
                takeIndex = 0;// 重置出队索引
            count--;// 数组中的元素数-1
            if (itrs != null)
                itrs.elementDequeued();
        } else {
            // an "interior" remove

            // slide over all others up through putIndex.
        	// 遍历数组删除指定索引的元素，并重置入队索引
            final int putIndex = this.putIndex;
            for (int i = removeIndex;;) {
                int next = i + 1;
                if (next == items.length)
                    next = 0;
                if (next != putIndex) {
                    items[i] = items[next];
                    i = next;
                } else {
                    items[i] = null;
                    this.putIndex = i;
                    break;
                }
            }
            count--;// 数组中元素数-1
            if (itrs != null)
                itrs.removedAt(removeIndex);
        }
        // 唤醒notFull条件上等待的线程
        notFull.signal();
    }

    /**
     * 创建一个制定容量的ArrayBlockingQueue队列
     * 访问策略默认为非公平锁
     */
    public ArrayBlockingQueue(int capacity) {
        this(capacity, false);
    }

    /**
     * 创建一个制定容量制定访问策略的ArrayBlockingQueue队列
     */
    public ArrayBlockingQueue(int capacity, boolean fair) {
    	// 容量检查
        if (capacity <= 0)
            throw new IllegalArgumentException();
        // 初始化制定容量的数组
        this.items = new Object[capacity];
        // 初始化制定策略的全局保护锁
        lock = new ReentrantLock(fair);
        // 获取非空和非满的条件实例
        notEmpty = lock.newCondition();
        notFull =  lock.newCondition();
    }

    /**
     * 创建一个指定容量指定访问策略并且包含指定集合元素的ArrayBlockingQueue队列
     */
    public ArrayBlockingQueue(int capacity, boolean fair,
                              Collection<? extends E> c) {
    	// 创建一个制定容量制定访问策略的ArrayBlockingQueue队列
        this(capacity, fair);

        final ReentrantLock lock = this.lock;
        lock.lock(); // 此处加锁仅是为了线程可见性，不是互斥
        try {
            int i = 0;
            try {
            	// 使用此集合迭代器迭代所有元素
                for (E e : c) {
                	// 检查元素不为null，如果为null抛出空指针异常
                    checkNotNull(e);
                    
                    items[i++] = e;
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                throw new IllegalArgumentException();
            }
            // 获取数组中元素数量
            count = i;
            // 获取下一次put操作的索引
            putIndex = (i == capacity) ? 0 : i;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 调用offer方法像队列尾部插入元素
     * 如果成功返回true
     * 如果队列已满抛出IllegalStateException异常
     * 如果元素为null则抛出NullPointerException异常
     */
    public boolean add(E e) {
        return super.add(e);
    }

    /**
     * 将指定元素插入此队列尾部
     * 如果成功返回true
     * 如果容量已满返回false
     * 如果待插入元素为null则抛出NullPointerException
     */
    public boolean offer(E e) {
    	// 元素检查，如果为null抛出空指针异常
        checkNotNull(e);
        final ReentrantLock lock = this.lock;
        lock.lock();// 加锁
        try {
        	// 校验容量如果已满则返回false
            if (count == items.length)
                return false;
            else {
            	// 入队操作
                enqueue(e);
                return true;
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 将指定元素插入此队列尾部
     * 如果队列已满则阻塞，直到容量不满或被其它线程中断
     *
     * 如果被其它线程调用该线程的interrupt()方法中断线程，则抛出InterruptedException异常
     * 如果指定要插入的元素为null，则抛出NullPoniterException异常
     * 
     */
    public void put(E e) throws InterruptedException {
    	// 检查如果元素为null抛出空指针异常
        checkNotNull(e);
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly(); // 可中断锁
        try {
        	// 检查如果队列已满则触发等待条件
            while (count == items.length)
                notFull.await();
            // 入队操作
            enqueue(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 将指定元素插入此队列的尾部
     * 
     * 如果当前队列已满则阻塞，直到队列不满、线程被中断或者阻塞超时
     * 如果阻塞超时则返回false
     * 如果指定元素为null则抛出NullPointerException异常
     * 如果当前线程被其它线程调用interrupt()方法中断则抛出InterruptedException异常
     * 插入成功返回true
     */
    public boolean offer(E e, long timeout,
    		TimeUnit unit)
        throws InterruptedException {

    	// 检查指定插入的元素为null时抛出空指针异常
        checkNotNull(e);
        // 获取超时时间(毫秒)
        long nanos = unit.toNanos(timeout);
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();// 可中断锁
        try {
        	// 当队列已满时
            while (count == items.length) {
                if (nanos <= 0)// 检查如果达到超时时间则返回false
                    return false;
                // 阻塞，当阻塞时间达到指定的超时时间后被唤醒
                nanos = notFull.awaitNanos(nanos);
            }
            // 入队方法
            enqueue(e);
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 检索并删除此队列的头部元素，如果队列为空则返回null
     * 
     * 此操作成功后会试图唤醒notFull条件上等待的线程
     */
    public E poll() {
    	// 可重入锁
        final ReentrantLock lock = this.lock;
        lock.lock(); // 取得锁
        try {
        	// 如果当前数组为空返回null，否则调用出队方法
            return (count == 0) ? null : dequeue();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 检索并删除此队列头部元素
     * 如果队列为空则阻塞直到队列非空或当前线程被中断
     * 
     * 此操作成功后会试图唤醒notFull条件上等待的线程
     * 
     * 当前线程被中断会抛出InterruptedException异常
     */
    public E take() throws InterruptedException {
    	// 可重入锁
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();// 获取可中断锁
        try {
        	// 如果队列为空则阻塞直到notEmpty被唤醒，或调用此线程的interrupt()方法中断当前线程
            while (count == 0)
                notEmpty.await();
            // 调用出队方法
            return dequeue();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 检索或删除此队列头部元素
     * 如果此队列为空则阻塞，直到达到下列条件
     * 		1.队列不为空
     * 		2.阻塞达到指定时间
     * 		3.当前线程被中断
     * 
     * 阻塞超时后返回null
     * 当前线程被中断后抛出InterruptedException异常
     * 
     * 此操作成功获取元素后会试图唤醒notFull条件上等待的线程
     */
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
    	// 超时毫秒数
        long nanos = unit.toNanos(timeout);
        final ReentrantLock lock = this.lock; // 可重入锁
        lock.lockInterruptibly(); // 获取可中断锁
        try {
        	// 此队列为空时
            while (count == 0) {
                if (nanos <= 0)// 当阻塞达到超时时间后返回null
                    return null;
                // 当前线程阻塞，直到notEmpty条件被唤醒或达到超时时间
                nanos = notEmpty.awaitNanos(nanos);
            }
            // 调用出队方法
            return dequeue();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取此队列头部元素
     */
    public E peek() {
        final ReentrantLock lock = this.lock;
        lock.lock();// 得到锁
        try {
        	// 返回数组中当前出队索引位置的元素
            return itemAt(takeIndex); // null when queue is empty
        } finally {
            lock.unlock();
        }
    }

    // this doc comment is overridden to remove the reference to collections
    // greater in size than Integer.MAX_VALUE
    /**
     * 返回此队列中的元素数
     *
     * @return the number of elements in this queue
     */
    public int size() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return count;
        } finally {
            lock.unlock();
        }
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
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return items.length - count;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 将指定元素从此队列中删除
     * 如果元素在此队列中存在一个或多个返回true
     * 如果此队列中没有此元素或队列为空返回false
     * 
     * 此操作返回true会唤醒notFull条件上等待的其它线程
     */
    public boolean remove(Object o) {
        if (o == null) return false;
        final Object[] items = this.items;
        final ReentrantLock lock = this.lock;
        lock.lock(); //获取锁
        try {
            if (count > 0) {
                final int putIndex = this.putIndex;
                int i = takeIndex;
                do {// 遍历数组
                	
                    if (o.equals(items[i])) {
                    	// 删除i位置的元素
                        removeAt(i);
                        return true;
                    }
                    if (++i == items.length)
                        i = 0;
                } while (i != putIndex);
            }
            return false;
        } finally {
            lock.unlock();
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
        final Object[] items = this.items;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (count > 0) {
                final int putIndex = this.putIndex;
                int i = takeIndex;
                do {
                    if (o.equals(items[i]))
                        return true;
                    if (++i == items.length)
                        i = 0;
                } while (i != putIndex);
            }
            return false;
        } finally {
            lock.unlock();
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
        Object[] a;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            final int count = this.count;
            a = new Object[count];
            int n = items.length - takeIndex;
            if (count <= n)
                System.arraycopy(items, takeIndex, a, 0, count);
            else {
                System.arraycopy(items, takeIndex, a, 0, n);
                System.arraycopy(items, 0, a, n, count - n);
            }
        } finally {
            lock.unlock();
        }
        return a;
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
        final Object[] items = this.items;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            final int count = this.count;
            final int len = a.length;
            if (len < count)
                a = (T[])java.lang.reflect.Array.newInstance(
                    a.getClass().getComponentType(), count);
            int n = items.length - takeIndex;
            if (count <= n)
                System.arraycopy(items, takeIndex, a, 0, count);
            else {
                System.arraycopy(items, takeIndex, a, 0, n);
                System.arraycopy(items, 0, a, n, count - n);
            }
            if (len > count)
                a[count] = null;
        } finally {
            lock.unlock();
        }
        return a;
    }

    public String toString() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int k = count;
            if (k == 0)
                return "[]";

            final Object[] items = this.items;
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            for (int i = takeIndex; ; ) {
                Object e = items[i];
                sb.append(e == this ? "(this Collection)" : e);
                if (--k == 0)
                    return sb.append(']').toString();
                sb.append(',').append(' ');
                if (++i == items.length)
                    i = 0;
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Atomically removes all of the elements from this queue.
     * The queue will be empty after this call returns.
     */
    public void clear() {
        final Object[] items = this.items;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int k = count;
            if (k > 0) {
                final int putIndex = this.putIndex;
                int i = takeIndex;
                do {
                    items[i] = null;
                    if (++i == items.length)
                        i = 0;
                } while (i != putIndex);
                takeIndex = putIndex;
                count = 0;
                if (itrs != null)
                    itrs.queueIsEmpty();
                for (; k > 0 && lock.hasWaiters(notFull); k--)
                    notFull.signal();
            }
        } finally {
            lock.unlock();
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
        checkNotNull(c);
        if (c == this)
            throw new IllegalArgumentException();
        if (maxElements <= 0)
            return 0;
        final Object[] items = this.items;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int n = Math.min(maxElements, count);
            int take = takeIndex;
            int i = 0;
            try {
                while (i < n) {
                    @SuppressWarnings("unchecked")
                    E x = (E) items[take];
                    c.add(x);
                    items[take] = null;
                    if (++take == items.length)
                        take = 0;
                    i++;
                }
                return n;
            } finally {
                // Restore invariants even if c.add() threw
                if (i > 0) {
                    count -= i;
                    takeIndex = take;
                    if (itrs != null) {
                        if (count == 0)
                            itrs.queueIsEmpty();
                        else if (i > take)
                            itrs.takeIndexWrapped();
                    }
                    for (; i > 0 && lock.hasWaiters(notFull); i--)
                        notFull.signal();
                }
            }
        } finally {
            lock.unlock();
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

    /**
     * Shared data between iterators and their queue, allowing queue
     * modifications to update iterators when elements are removed.
     *
     * This adds a lot of complexity for the sake of correctly
     * handling some uncommon operations, but the combination of
     * circular-arrays and supporting interior removes (i.e., those
     * not at head) would cause iterators to sometimes lose their
     * places and/or (re)report elements they shouldn't.  To avoid
     * this, when a queue has one or more iterators, it keeps iterator
     * state consistent by:
     *
     * (1) keeping track of the number of "cycles", that is, the
     *     number of times takeIndex has wrapped around to 0.
     * (2) notifying all iterators via the callback removedAt whenever
     *     an interior element is removed (and thus other elements may
     *     be shifted).
     *
     * These suffice to eliminate iterator inconsistencies, but
     * unfortunately add the secondary responsibility of maintaining
     * the list of iterators.  We track all active iterators in a
     * simple linked list (accessed only when the queue's lock is
     * held) of weak references to Itr.  The list is cleaned up using
     * 3 different mechanisms:
     *
     * (1) Whenever a new iterator is created, do some O(1) checking for
     *     stale list elements.
     *
     * (2) Whenever takeIndex wraps around to 0, check for iterators
     *     that have been unused for more than one wrap-around cycle.
     *
     * (3) Whenever the queue becomes empty, all iterators are notified
     *     and this entire data structure is discarded.
     *
     * So in addition to the removedAt callback that is necessary for
     * correctness, iterators have the shutdown and takeIndexWrapped
     * callbacks that help remove stale iterators from the list.
     *
     * Whenever a list element is examined, it is expunged if either
     * the GC has determined that the iterator is discarded, or if the
     * iterator reports that it is "detached" (does not need any
     * further state updates).  Overhead is maximal when takeIndex
     * never advances, iterators are discarded before they are
     * exhausted, and all removals are interior removes, in which case
     * all stale iterators are discovered by the GC.  But even in this
     * case we don't increase the amortized complexity.
     *
     * Care must be taken to keep list sweeping methods from
     * reentrantly invoking another such method, causing subtle
     * corruption bugs.
     */
    class Itrs {

        /**
         * Node in a linked list of weak iterator references.
         */
        private class Node extends WeakReference<Itr> {
            Node next;

            Node(Itr iterator, Node next) {
                super(iterator);
                this.next = next;
            }
        }

        /** Incremented whenever takeIndex wraps around to 0 */
        int cycles = 0;

        /** Linked list of weak iterator references */
        private Node head;

        /** Used to expunge stale iterators */
        private Node sweeper = null;

        private static final int SHORT_SWEEP_PROBES = 4;
        private static final int LONG_SWEEP_PROBES = 16;

        Itrs(Itr initial) {
            register(initial);
        }

        /**
         * Sweeps itrs, looking for and expunging stale iterators.
         * If at least one was found, tries harder to find more.
         * Called only from iterating thread.
         *
         * @param tryHarder whether to start in try-harder mode, because
         * there is known to be at least one iterator to collect
         */
        void doSomeSweeping(boolean tryHarder) {
            // assert lock.getHoldCount() == 1;
            // assert head != null;
            int probes = tryHarder ? LONG_SWEEP_PROBES : SHORT_SWEEP_PROBES;
            Node o, p;
            final Node sweeper = this.sweeper;
            boolean passedGo;   // to limit search to one full sweep

            if (sweeper == null) {
                o = null;
                p = head;
                passedGo = true;
            } else {
                o = sweeper;
                p = o.next;
                passedGo = false;
            }

            for (; probes > 0; probes--) {
                if (p == null) {
                    if (passedGo)
                        break;
                    o = null;
                    p = head;
                    passedGo = true;
                }
                final Itr it = p.get();
                final Node next = p.next;
                if (it == null || it.isDetached()) {
                    // found a discarded/exhausted iterator
                    probes = LONG_SWEEP_PROBES; // "try harder"
                    // unlink p
                    p.clear();
                    p.next = null;
                    if (o == null) {
                        head = next;
                        if (next == null) {
                            // We've run out of iterators to track; retire
                            itrs = null;
                            return;
                        }
                    }
                    else
                        o.next = next;
                } else {
                    o = p;
                }
                p = next;
            }

            this.sweeper = (p == null) ? null : o;
        }

        /**
         * Adds a new iterator to the linked list of tracked iterators.
         */
        void register(Itr itr) {
            // assert lock.getHoldCount() == 1;
            head = new Node(itr, head);
        }

        /**
         * Called whenever takeIndex wraps around to 0.
         *
         * Notifies all iterators, and expunges any that are now stale.
         */
        void takeIndexWrapped() {
            // assert lock.getHoldCount() == 1;
            cycles++;
            for (Node o = null, p = head; p != null;) {
                final Itr it = p.get();
                final Node next = p.next;
                if (it == null || it.takeIndexWrapped()) {
                    // unlink p
                    // assert it == null || it.isDetached();
                    p.clear();
                    p.next = null;
                    if (o == null)
                        head = next;
                    else
                        o.next = next;
                } else {
                    o = p;
                }
                p = next;
            }
            if (head == null)   // no more iterators to track
                itrs = null;
        }

        /**
         * Called whenever an interior remove (not at takeIndex) occurred.
         *
         * Notifies all iterators, and expunges any that are now stale.
         */
        void removedAt(int removedIndex) {
            for (Node o = null, p = head; p != null;) {
                final Itr it = p.get();
                final Node next = p.next;
                if (it == null || it.removedAt(removedIndex)) {
                    // unlink p
                    // assert it == null || it.isDetached();
                    p.clear();
                    p.next = null;
                    if (o == null)
                        head = next;
                    else
                        o.next = next;
                } else {
                    o = p;
                }
                p = next;
            }
            if (head == null)   // no more iterators to track
                itrs = null;
        }

        /**
         * Called whenever the queue becomes empty.
         *
         * Notifies all active iterators that the queue is empty,
         * clears all weak refs, and unlinks the itrs datastructure.
         */
        void queueIsEmpty() {
            // assert lock.getHoldCount() == 1;
            for (Node p = head; p != null; p = p.next) {
                Itr it = p.get();
                if (it != null) {
                    p.clear();
                    it.shutdown();
                }
            }
            head = null;
            itrs = null;
        }

        /**
         * Called whenever an element has been dequeued (at takeIndex).
         */
        void elementDequeued() {
            // assert lock.getHoldCount() == 1;
            if (count == 0)
                queueIsEmpty();
            else if (takeIndex == 0)
                takeIndexWrapped();
        }
    }

    /**
     * Iterator for ArrayBlockingQueue.
     *
     * To maintain weak consistency with respect to puts and takes, we
     * read ahead one slot, so as to not report hasNext true but then
     * not have an element to return.
     *
     * We switch into "detached" mode (allowing prompt unlinking from
     * itrs without help from the GC) when all indices are negative, or
     * when hasNext returns false for the first time.  This allows the
     * iterator to track concurrent updates completely accurately,
     * except for the corner case of the user calling Iterator.remove()
     * after hasNext() returned false.  Even in this case, we ensure
     * that we don't remove the wrong element by keeping track of the
     * expected element to remove, in lastItem.  Yes, we may fail to
     * remove lastItem from the queue if it moved due to an interleaved
     * interior remove while in detached mode.
     */
    private class Itr implements Iterator<E> {
        /** Index to look for new nextItem; NONE at end */
        private int cursor;

        /** Element to be returned by next call to next(); null if none */
        private E nextItem;

        /** Index of nextItem; NONE if none, REMOVED if removed elsewhere */
        private int nextIndex;

        /** Last element returned; null if none or not detached. */
        private E lastItem;

        /** Index of lastItem, NONE if none, REMOVED if removed elsewhere */
        private int lastRet;

        /** Previous value of takeIndex, or DETACHED when detached */
        private int prevTakeIndex;

        /** Previous value of iters.cycles */
        private int prevCycles;

        /** Special index value indicating "not available" or "undefined" */
        private static final int NONE = -1;

        /**
         * Special index value indicating "removed elsewhere", that is,
         * removed by some operation other than a call to this.remove().
         */
        private static final int REMOVED = -2;

        /** Special value for prevTakeIndex indicating "detached mode" */
        private static final int DETACHED = -3;

        Itr() {
            // assert lock.getHoldCount() == 0;
            lastRet = NONE;
            final ReentrantLock lock = ArrayBlockingQueue.this.lock;
            lock.lock();
            try {
                if (count == 0) {
                    // assert itrs == null;
                    cursor = NONE;
                    nextIndex = NONE;
                    prevTakeIndex = DETACHED;
                } else {
                    final int takeIndex = ArrayBlockingQueue.this.takeIndex;
                    prevTakeIndex = takeIndex;
                    nextItem = itemAt(nextIndex = takeIndex);
                    cursor = incCursor(takeIndex);
                    if (itrs == null) {
                        itrs = new Itrs(this);
                    } else {
                        itrs.register(this); // in this order
                        itrs.doSomeSweeping(false);
                    }
                    prevCycles = itrs.cycles;
                    // assert takeIndex >= 0;
                    // assert prevTakeIndex == takeIndex;
                    // assert nextIndex >= 0;
                    // assert nextItem != null;
                }
            } finally {
                lock.unlock();
            }
        }

        boolean isDetached() {
            // assert lock.getHoldCount() == 1;
            return prevTakeIndex < 0;
        }

        private int incCursor(int index) {
            // assert lock.getHoldCount() == 1;
            if (++index == items.length)
                index = 0;
            if (index == putIndex)
                index = NONE;
            return index;
        }

        /**
         * Returns true if index is invalidated by the given number of
         * dequeues, starting from prevTakeIndex.
         */
        private boolean invalidated(int index, int prevTakeIndex,
                                    long dequeues, int length) {
            if (index < 0)
                return false;
            int distance = index - prevTakeIndex;
            if (distance < 0)
                distance += length;
            return dequeues > distance;
        }

        /**
         * Adjusts indices to incorporate all dequeues since the last
         * operation on this iterator.  Call only from iterating thread.
         */
        private void incorporateDequeues() {
            // assert lock.getHoldCount() == 1;
            // assert itrs != null;
            // assert !isDetached();
            // assert count > 0;

            final int cycles = itrs.cycles;
            final int takeIndex = ArrayBlockingQueue.this.takeIndex;
            final int prevCycles = this.prevCycles;
            final int prevTakeIndex = this.prevTakeIndex;

            if (cycles != prevCycles || takeIndex != prevTakeIndex) {
                final int len = items.length;
                // how far takeIndex has advanced since the previous
                // operation of this iterator
                long dequeues = (cycles - prevCycles) * len
                    + (takeIndex - prevTakeIndex);

                // Check indices for invalidation
                if (invalidated(lastRet, prevTakeIndex, dequeues, len))
                    lastRet = REMOVED;
                if (invalidated(nextIndex, prevTakeIndex, dequeues, len))
                    nextIndex = REMOVED;
                if (invalidated(cursor, prevTakeIndex, dequeues, len))
                    cursor = takeIndex;

                if (cursor < 0 && nextIndex < 0 && lastRet < 0)
                    detach();
                else {
                    this.prevCycles = cycles;
                    this.prevTakeIndex = takeIndex;
                }
            }
        }

        /**
         * Called when itrs should stop tracking this iterator, either
         * because there are no more indices to update (cursor < 0 &&
         * nextIndex < 0 && lastRet < 0) or as a special exception, when
         * lastRet >= 0, because hasNext() is about to return false for the
         * first time.  Call only from iterating thread.
         */
        private void detach() {
            // Switch to detached mode
            // assert lock.getHoldCount() == 1;
            // assert cursor == NONE;
            // assert nextIndex < 0;
            // assert lastRet < 0 || nextItem == null;
            // assert lastRet < 0 ^ lastItem != null;
            if (prevTakeIndex >= 0) {
                // assert itrs != null;
                prevTakeIndex = DETACHED;
                // try to unlink from itrs (but not too hard)
                itrs.doSomeSweeping(true);
            }
        }

        /**
         * For performance reasons, we would like not to acquire a lock in
         * hasNext in the common case.  To allow for this, we only access
         * fields (i.e. nextItem) that are not modified by update operations
         * triggered by queue modifications.
         */
        public boolean hasNext() {
            // assert lock.getHoldCount() == 0;
            if (nextItem != null)
                return true;
            noNext();
            return false;
        }

        private void noNext() {
            final ReentrantLock lock = ArrayBlockingQueue.this.lock;
            lock.lock();
            try {
                // assert cursor == NONE;
                // assert nextIndex == NONE;
                if (!isDetached()) {
                    // assert lastRet >= 0;
                    incorporateDequeues(); // might update lastRet
                    if (lastRet >= 0) {
                        lastItem = itemAt(lastRet);
                        // assert lastItem != null;
                        detach();
                    }
                }
                // assert isDetached();
                // assert lastRet < 0 ^ lastItem != null;
            } finally {
                lock.unlock();
            }
        }

        public E next() {
            // assert lock.getHoldCount() == 0;
            final E x = nextItem;
            if (x == null)
                throw new NoSuchElementException();
            final ReentrantLock lock = ArrayBlockingQueue.this.lock;
            lock.lock();
            try {
                if (!isDetached())
                    incorporateDequeues();
                // assert nextIndex != NONE;
                // assert lastItem == null;
                lastRet = nextIndex;
                final int cursor = this.cursor;
                if (cursor >= 0) {
                    nextItem = itemAt(nextIndex = cursor);
                    // assert nextItem != null;
                    this.cursor = incCursor(cursor);
                } else {
                    nextIndex = NONE;
                    nextItem = null;
                }
            } finally {
                lock.unlock();
            }
            return x;
        }

        public void remove() {
            // assert lock.getHoldCount() == 0;
            final ReentrantLock lock = ArrayBlockingQueue.this.lock;
            lock.lock();
            try {
                if (!isDetached())
                    incorporateDequeues(); // might update lastRet or detach
                final int lastRet = this.lastRet;
                this.lastRet = NONE;
                if (lastRet >= 0) {
                    if (!isDetached())
                        removeAt(lastRet);
                    else {
                        final E lastItem = this.lastItem;
                        // assert lastItem != null;
                        this.lastItem = null;
                        if (itemAt(lastRet) == lastItem)
                            removeAt(lastRet);
                    }
                } else if (lastRet == NONE)
                    throw new IllegalStateException();
                // else lastRet == REMOVED and the last returned element was
                // previously asynchronously removed via an operation other
                // than this.remove(), so nothing to do.

                if (cursor < 0 && nextIndex < 0)
                    detach();
            } finally {
                lock.unlock();
                // assert lastRet == NONE;
                // assert lastItem == null;
            }
        }

        /**
         * Called to notify the iterator that the queue is empty, or that it
         * has fallen hopelessly behind, so that it should abandon any
         * further iteration, except possibly to return one more element
         * from next(), as promised by returning true from hasNext().
         */
        void shutdown() {
            // assert lock.getHoldCount() == 1;
            cursor = NONE;
            if (nextIndex >= 0)
                nextIndex = REMOVED;
            if (lastRet >= 0) {
                lastRet = REMOVED;
                lastItem = null;
            }
            prevTakeIndex = DETACHED;
            // Don't set nextItem to null because we must continue to be
            // able to return it on next().
            //
            // Caller will unlink from itrs when convenient.
        }

        private int distance(int index, int prevTakeIndex, int length) {
            int distance = index - prevTakeIndex;
            if (distance < 0)
                distance += length;
            return distance;
        }

        /**
         * Called whenever an interior remove (not at takeIndex) occurred.
         *
         * @return true if this iterator should be unlinked from itrs
         */
        boolean removedAt(int removedIndex) {
            // assert lock.getHoldCount() == 1;
            if (isDetached())
                return true;

            final int cycles = itrs.cycles;
            final int takeIndex = ArrayBlockingQueue.this.takeIndex;
            final int prevCycles = this.prevCycles;
            final int prevTakeIndex = this.prevTakeIndex;
            final int len = items.length;
            int cycleDiff = cycles - prevCycles;
            if (removedIndex < takeIndex)
                cycleDiff++;
            final int removedDistance =
                (cycleDiff * len) + (removedIndex - prevTakeIndex);
            // assert removedDistance >= 0;
            int cursor = this.cursor;
            if (cursor >= 0) {
                int x = distance(cursor, prevTakeIndex, len);
                if (x == removedDistance) {
                    if (cursor == putIndex)
                        this.cursor = cursor = NONE;
                }
                else if (x > removedDistance) {
                    // assert cursor != prevTakeIndex;
                    this.cursor = cursor = dec(cursor);
                }
            }
            int lastRet = this.lastRet;
            if (lastRet >= 0) {
                int x = distance(lastRet, prevTakeIndex, len);
                if (x == removedDistance)
                    this.lastRet = lastRet = REMOVED;
                else if (x > removedDistance)
                    this.lastRet = lastRet = dec(lastRet);
            }
            int nextIndex = this.nextIndex;
            if (nextIndex >= 0) {
                int x = distance(nextIndex, prevTakeIndex, len);
                if (x == removedDistance)
                    this.nextIndex = nextIndex = REMOVED;
                else if (x > removedDistance)
                    this.nextIndex = nextIndex = dec(nextIndex);
            }
            else if (cursor < 0 && nextIndex < 0 && lastRet < 0) {
                this.prevTakeIndex = DETACHED;
                return true;
            }
            return false;
        }

        /**
         * Called whenever takeIndex wraps around to zero.
         *
         * @return true if this iterator should be unlinked from itrs
         */
        boolean takeIndexWrapped() {
            // assert lock.getHoldCount() == 1;
            if (isDetached())
                return true;
            if (itrs.cycles - prevCycles > 1) {
                // All the elements that existed at the time of the last
                // operation are gone, so abandon further iteration.
                shutdown();
                return true;
            }
            return false;
        }

//         /** Uncomment for debugging. */
//         public String toString() {
//             return ("cursor=" + cursor + " " +
//                     "nextIndex=" + nextIndex + " " +
//                     "lastRet=" + lastRet + " " +
//                     "nextItem=" + nextItem + " " +
//                     "lastItem=" + lastItem + " " +
//                     "prevCycles=" + prevCycles + " " +
//                     "prevTakeIndex=" + prevTakeIndex + " " +
//                     "size()=" + size() + " " +
//                     "remainingCapacity()=" + remainingCapacity());
//         }
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
        return Spliterators.spliterator
            (this, Spliterator.ORDERED | Spliterator.NONNULL |
             Spliterator.CONCURRENT);
    }

    /**
     * Deserializes this queue and then checks some invariants.
     *
     * @param s the input stream
     * @throws ClassNotFoundException if the class of a serialized object
     *         could not be found
     * @throws java.io.InvalidObjectException if invariants are violated
     * @throws java.io.IOException if an I/O error occurs
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {

        // Read in items array and various fields
        s.defaultReadObject();

        // Check invariants over count and index fields. Note that
        // if putIndex==takeIndex, count can be either 0 or items.length.
        if (items.length == 0 ||
            takeIndex < 0 || takeIndex >= items.length ||
            putIndex  < 0 || putIndex  >= items.length ||
            count < 0     || count     >  items.length ||
            Math.floorMod(putIndex - takeIndex, items.length) !=
            Math.floorMod(count, items.length)) {
            throw new java.io.InvalidObjectException("invariants violated");
        }
    }
}
