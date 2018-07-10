# LinkedBlockingQueue

## 概述
>   LinkedBlockingQueue是一个又单向链表结构支持的可选是否有界的BlockingQueue阻塞队列,不允许Null值；
  此队列按照FIFO（先进先出）规则对元素进行排序；
  队列的头部元素是队列中储存时间最长的元素；
  队列的尾部元素是队列中储存时间最短的元素；
  新元素插入到队列尾部，队列检索操作则是从队列头部开始获得元素；
  链表队列通常具有比基于数组的队列更高的吞吐量，但在大多数并发应用程序中的预测性能较低；


## 结构
> LinkedBlockingQueue底层是有一个单向链表结构支持的，可指定链表的容量，如果不指定容量默认容量为Integer.MAX_VALUE。

## 并发
> LinkedBlockingQueue是线程安全的，通过两把ReentrantLock（互斥锁）来保护竞争资源，一把入队操作锁，一把出队操作锁，实现了多线程对竞争资源的互斥访问。

## 效率
> LinkedBlockingQueue内部使用两把互斥锁，入队锁和出队锁，保证入队和出队操作并发访问时的效率。

## 内部类
```Java
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
```

## 属性
```Java
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
```

## 方法
> 构造函数：LinkedBlockingQueue有三个构造函数，其中默认构造函数和包含指定集合元素的构造函数初始化的队列容量为Integer.MAX_VALUE

```Java
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
```

> 入列操作：enqueue/put/offer，其中put/offer方法是通过调用私有方法enqueue来实现的

```Java
    /**
     * 入队操作
     * 将指定节点插入到队列尾部
     */
    private void enqueue(Node<E> node) {
    	  // 封装新节点，并赋给当前的最后一个节点的下一个节点，然后在将这个节点设为最后一个节点
        last = last.next = node;
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
```

> 出列操作dequeue/poll/take/peek/remove，其中poll/take是通过调用私有方法dequeue方法来实现的。

```Java
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
```


## 总结
> LinkedBlockingQueue是一个由单向链表结构支持的可选是否有界的阻塞队列;
head记录队列的头结点，last记录链表为节点，头结点值始终为null，此队列不允许插入null；
此队列为线程安全的，内部由两把互斥锁putLock(入队锁)和takeLock(出队锁)保护竞争资源；
remove操作会同时锁住这两把锁；
出队操作使用notEmpty作为空队列的阻塞条件，入队操作使用notFull作为队列已满的阻塞条件；
put(e)方法：向队列尾部插入元素，如果队列已满则阻塞直到队列不满或线程被中断；
offer(e)方法：向队列尾部插入元素，成功返回true，如果队列已满返回false；
offer(e,timeout,unit)方法：向队列尾部插入元素，如果队列已满则阻塞，直到队列不满或阻塞超时或当前线程被中断；
take()方法：检索并删除队列头部元素，如果队列为空则阻塞，直到队列非空或当前线程被中断；
poll()方法：检索并删除队列头部元素，如果队列为空则立即返回null；
poll(timeout,unit)方法：检索并删除队列头部元素，如果队列为空则阻塞，直到队列非空或阻塞超时或当前线程被中断；
peek()方法：检索队列头部元素，如果队列为空立即返回null；
remove(o)方法：从队列头部开始检索删除指定元素并返回成功状态true/false，如果队列存在多个此元素则只删除检索到的第一个元素；
