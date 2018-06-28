# PriorityQueue (优先队列)

## 数据结构
> 底层为数组结构

## 线程安全
> PriorityQueue是非线程安全的，多线程场景中可以使用PriorityBlockingQueue

## 扩容
> PriorityQueue默认初始容量为11，当queue数组中元素数达到当前队列容量会在添加元素时进行扩容。   
> 扩容时如果当前元素数量少于64个会在原容量基础上增加一倍再加2，否则容量增长50%。
```Java
    /**
     * Increases the capacity of the array.
     *
     * @param minCapacity the desired minimum capacity
     */
    private void grow(int minCapacity) {
    	// 获取queue数组容量
        int oldCapacity = queue.length;
        // 如果当前容量小于64，则容量加2，否则增长50%
        int newCapacity = oldCapacity + ((oldCapacity < 64) ?
                                         (oldCapacity + 2) :
                                         (oldCapacity >> 1));
        // overflow-conscious code
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        // 以复制的方式将原来数组元素复制给新的数组
        queue = Arrays.copyOf(queue, newCapacity);
    }
```

## 重要属性
```Java 
	// 用于储存元素的数组
	transient Object[] queue; // non-private to simplify nested class access

	// 优先级队列中元素的数量.
	private int size = 0;

	// 比较器,如果有比较器优先使用比较器，如果比较器为null，要求元素必须实现IComparator接口，否则会在进行排序操作时报错。
	private final Comparator<? super E> comparator;
```

## 主要方法

> heapify()建堆方法   

```Java
    /**
     * Establishes the heap invariant (described above) in the entire tree,
     * assuming nothing about the order of the elements prior to the call.
     */
    @SuppressWarnings("unchecked")
    private void heapify() {
    	// 从n/2-1处开始到0，不停的调整堆，成为小顶堆。调用siftDown构建小顶堆，siftDown字面意思是向下筛选，就是从父节点开始向左右节点找最小值。
        for (int i = (size >>> 1) - 1; i >= 0; i--)
            siftDown(i, (E) queue[i]);
    }
```


> add/offer方法作用：向队列插入一个元素；add方法直接调用offer方法

```Java
	/**
	* Inserts the specified element into this priority queue.
	*
	* @return {@code true} (as specified by {@link Queue#offer})
	* @throws ClassCastException if the specified element cannot be
	*         compared with elements currently in this priority queue
	*         according to the priority queue's ordering
	* @throws NullPointerException if the specified element is null
	*/
	public boolean offer(E e) {
		// 不允许null,否则抛出异常
	    if (e == null)
	        throw new NullPointerException();
	    modCount++;
	    int i = size;
	    // 检查如果数组容量已满调用grow方法扩容
	    if (i >= queue.length)
	        grow(i + 1);
	    size = i + 1;
	    if (i == 0)// 当前队列为空，直接插入到queue[0]的位置
	        queue[0] = e;
	    else// 将元素直接“放到”一个有效位置上，然后调整，不断上浮
	        siftUp(i, e);
	    return true;
	}
```

> remove/poll方法作用：删除元素；remove调用poll方法

```Java
    @SuppressWarnings("unchecked")
    public E poll() {
    	// 如果数组为空，返回null
        if (size == 0)
            return null;
        // 容量计数调整
        int s = --size;
        modCount++;
        // 取出堆顶元素
        E result = (E) queue[0];
        E x = (E) queue[s];
        queue[s] = null;
        // 将最后一个元素暂时放到堆顶位置，然后调整堆顶元素，下移
        if (s != 0)
            siftDown(0, x);
        return result;
    }
```

> clear方法作用：清楚元素

```Java
    /**
     * Removes all of the elements from this priority queue.
     * The queue will be empty after this call returns.
     */
    public void clear() {
        modCount++;
        // 清除数组所有元素的引用
        for (int i = 0; i < size; i++)
            queue[i] = null;
        // 数组大小为0
        size = 0;
    }
```

