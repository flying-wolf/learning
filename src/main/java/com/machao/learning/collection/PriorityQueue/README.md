# PriorityQueue (优先队列)

## 数据结构
> 底层为数组结构

## 线程安全
> PriorityQueue是非线程安全的，多线程场景中可以使用PriorityBlockingQueue

## 扩容
> PriorityQueue默认初始容量为11，当queue数组中元素数达到当前队列容量会在添加元素时进行扩容。   
> 扩容时如果当前元素数量少于64个会在原容量基础上加2，否则容量扩充为之前的2倍。

## 重要属性
```Java 
	// 用于储存元素的数组
	transient Object[] queue; // non-private to simplify nested class access

	// 优先级队列中元素的数量.
	private int size = 0;

	// 比较器,如果有比较器优先使用比较器，如果比较器为null，要求元素必须实现IComparator接口，否则会在进行排序操作时报错。
	private final Comparator<? super E> comparator;
```
