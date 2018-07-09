# ArrayBlockingQueue

## 概述
>   ArrayBlockingQueue是一个由数组支持的有界的BlockingQueue阻塞队列；
  此队列按照FIFO（先进先出）规则对元素进行排序；
  队列的头部元素是队列中储存时间最长的元素，队列的尾部元素是队列中储存时间最短的元素；
  新元素插入到队列尾部，队列检索操作则是从队列头部开始获得元素；
  
> 这是一个典型的“有界缓存区”，固定大小的数组保存生产者插入的元素和消费者提取的元素。
  一旦创建了这样的缓存区，容量就不能改变。
  试图向已满的队列中插入元素会导致操作阻塞；
  试图从空队列中检索元素会导致类似的阻塞；


## 数据结构
> ArrayBlockingQueue底层由一个指定大小的数组实现

## 保证并发
> ArrayBlockingQueue是线程安全的，通过ReentrantLock（互斥所）来保护竞争资源，实现了多线程对竞争资源的互斥访问。

## 效率
> ArrayBlockingQueue内部使用一把锁，当存取两种操作同时竞争一把锁时效率较低，这种场景下可以使用LinkedBlockingQueue。

## 重要的属性
```Java
    /** 保存元素的queue数组 */
    final Object[] items;

    /** 下一次出队(take、poll、peek或remove)操作的元素索引 */
    int takeIndex;

    /** 下一次入队(put、offer或add)操作的元素索引 */
    int putIndex;

    /** 队列中的元素数量 */
    int count;

    /** 保护所有访问的主锁 */
    final ReentrantLock lock;

    /** 队列非空时的等待条件（出队操作等待条件） */
    private final Condition notEmpty;

    /** 队列非满时的等待条件（入队操作等待条件） */
    private final Condition notFull;
```

## 重要方法
> 构造函数：ArrayBlockingQueue(int capacity, boolean fair)/ ArrayBlockingQueue(int capacity, boolean fair, Collection<? extends E> c)

```Java
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
```

> 入列操作：enqueue/add/put/offer，其中add/put/offer方法是通过调用私有方法enqueue来实现的

```Java
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
        checkNotNull(e);    /**
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
```

> 出列操作dequeue/poll/take/peek/remove，其中poll/take是通过调用私有方法dequque方法来实现的。

```Java
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
```