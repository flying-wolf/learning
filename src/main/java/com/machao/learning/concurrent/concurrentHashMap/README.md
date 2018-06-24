# <center>ConcurrentHashMap</center>

## 数据结构
> ConcurrentHashMap底层结构为数组+链表+红黑树

## 并发
1. ConcurrentHashMap是线程安全的,内部通过CAS算法(无锁操作)实现线程安全
2. 通过CAS操作实现了3种原子性操作方法
3. 通过synchronized给头结点加锁，相比较之前版本减小了锁的粒度，减少了冲突与性能消耗
4. CAS + synchronized保证线程安全

## 效率

## 重要属性
1. table:默认为null,初始化发生在第一次插入操作，默认大小为16的数组，用了储存Node节点数据，扩容时总会时2的幂次方；
2. nextTable：默认为null，扩容时新生成的数组，其大小会是原数组的2倍。
3. sizeCtl：默认为0，用来控制table的初始化和扩容操作
	- -1代表table正在初始化
	- -N代表有N-1个线程正在进行扩容操作
	- 其余情况：
		- 如果table未初始化，则代表table需要初始化的大小
		- 如果table初始化完成，则代表table的容量，默认时table大小的0.75倍
4. private static final int MAXIMUM_CAPACITY = 1 << 30;//最大容量，当容量达到这个值时不再扩容
5. private static final int DEFAULT_CAPACITY = 16;//默认容量
6. private static final float LOAD_FACTOR = 0.75f;//加载因子，当map的大小达到capacity*laodfactor时扩容
7. static final int TREEIFY_THRESHOLD = 8;//当桶中的节点数大于8时将链表转化为红黑树
8. static final int UNTREEIFY_THRESHOLD = 6;//当桶中节点数小于6时将红黑树转化为链表
9. static final int MIN_TREEIFY_CAPACITY = 64;//当集合的大小大于64时才会进行链表与红黑树的转换
10. private static int RESIZE_STAMP_BITS = 16;
11. private static final int MAX_RESIZERS = (1 << (32 - RESIZE_STAMP_BITS)) - 1;
12. private static final int RESIZE_STAMP_SHIFT = 32 - RESIZE_STAMP_BITS;
13. static final int MOVED     = -1; // MOVED表示该节点是个forwarding Node，表示有线程处理过了
14. static final int TREEBIN   = -2;//表示判断到这个节点是一个树节点


## 重要方法
1. private final Node<K,V>[] initTable()//table初始化方法
	- 根据声明的table容量初始化table，默认大小为16，确保table的大小为2的幂次方
	- 第一次put操作时执行，且只会执行一次

2. public V put(K key, V value)//put操作
	- 1).校验key、value为空时抛出空指针异常
	- 2).计算key的hash，得到桶的位置
	- 3).进入无限循环直到插入为止如果table未初始化，则调用initTable初始化table，完成后进入下一次循环
		- 如果当前桶为空，直接CAS无锁操作将数据插入到当前桶，完成后跳出循环
		- 如果有其他线程正在进行扩容，则当前线程帮助扩容，完成后进入下一次循环
		- 否则说明当前桶不为空
			- 锁住当前桶中的头结点
			- 再次确认当前节点就是桶中的头节点
			- 判断如果是链表结构
				- 遍历这个链表判断当前节点hash相等切key的值相同活key为null，则更新当前节点的value，完成后跳出
				- 当遍历到链表结尾且没有找到key相同的节点，则直接在链表尾部插入新节点，完成后跳出
			- 如果时红黑树结构
				- 调用putTreeVal方法插入数据
			- 判断如果当前桶的节点数达到8时，将链表转为红黑树
			- 重新计算table中得元素数量，并检查扩容




3. addCount方法//增加计数，统计table元素数量
	- 统计tab中的节点个数大于阀值(sizeCtl)时会触发transfer，重新调整节点位置
        


4. treeifyBin//将tab的的index位置的链表转化为红黑树，如果tab的长度小于64则扩容tab两倍大小，就不转换红黑树了

5. tryPresize//


6. transfer//扩容方法
	- 1).遍历整个table,当前节点为空，则采用CAS的方式在当前位置放入ForwardingNode
	- 2).当前节点已经为ForwardingNode，则已经有线程处理过该节点了，直接跳过
	- 3).当前节点为链表或红黑树时，重新计算链表节点的hash值，移动到nextTable相应位置（构建一个反序链表和一个顺序链表分别放入nextTable的i和i+n位置），移动完成后在table的原位置放置ForwardingNode，表示当前节点已经完成扩容
        

7. size //统计元素个数baseCounter和数组里每个CounterCell的值之和






## 重要内部类
1. static class Node<K,V> implements Map.Entry<K,V>
	- 储存链表节点的数据结构，其中value和next属性使用volatile修饰，保证线程可见性
	- setValue方法直接抛出异常，不允许直接修改value的值
	- 新增find方法辅助查找查找节点

2. static final class TreeNode<K,V> extends Node<K,V> | static final class TreeBin<K,V> extends Node<K,V>
	- 储存红黑树的数据结构，在1.8ConcurrentHashMap中不是直接储存为TreeNode，是通过TreeBin封装TreeNode来实现的，ConcurrentHashMap桶中存放的时TreeBin结构，TreeNode继承自Node类，是为了附带Node的Next指针，而Next指针在TreeBin中可以查找下一个TreeNode；

3. static final class ForwardingNode<K,V> extends Node<K,V>
	- 特殊的Node节点，hash值为-1，其中存储nextTable的引用。
	- 只有table发生扩容时，ForwardingNode才会发挥作用，作为占位符放在table当中表示当前节点为null或者已经被移动。
















## jdk1.7与1.8之间的不同
1. 同步机制：
	- 1.7：分段锁，每个segment继承ReentrantLock
	- 1.8：CAS+synchronize保证并发更新
2. 存储结构：
	- 1.7：数组+链表
	- 1.8：数组+链表+红黑树
3. put操作：
	- 1.7：多个线程同时竞争同一个segment锁，获取成功的线程更新map；失败的线程尝试多次获取锁仍未成功，则挂起线程，等待释放锁；
	- 1.8：访问相应的bucket时用synchronize关键字，防止多个线程同时操作同一个bucket，如果该节点的hash不小于0，则遍历链表更新节点或插入新节点；如果该节点时treeBin，则通过putTreeVal方法插入节点；更新节点数量并检查链表红黑树转换与扩容；
4. size实现：
	- 1.7：统计每个Segment对象中的元素个数，然后进行累加，但是这种方式计算出来的结果并不一样的准确的。先采用不加锁的方式，连续计算元素的个数，最多计算3次：如果前后两次计算结果相同，则说明计算出来的元素个数是准确的；如果前后两次计算结果都不同，则给每个Segment进行加锁，再计算一次元素的个数；
	- 1.8：通过累加baseCounter与CounterCell数组中的数量，即可得到元素总个数；




## ConcurrentHashMap能完全代替HashTable吗？
> hashTablb的迭代器时强一致性的，而ConcurrentHashMap的迭代器时弱一致性的
