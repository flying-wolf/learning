# ConcurrentHashMap源码解析 for JDK1.8

## 概述
> ConcurrentHashMap底层为数组+单链表/红黑树结构
> ConcurrentHashMap不允许null键或null值
> ConcurrentHashMap不保证键值对的顺序，键值的顺序可能会发生改变
> ConcurrentHashMap是线程安全的，通过CAS+synchronized保证并发安全

## 原理
> ConcurrentHashMap使用Node数组保存数据，Node本身是一个链表节点，它的val和next属性采用volatile关键字修饰保证线程可见性;
> 当Node数组同一个位置上的节点数大于8个时将链表转为红黑树，当红黑树节点小于等于6个时转为链表结构储存，提高查找性能
> ConcurrentHashMap利用CAS操作+Synchronized同步锁来保证线程安全，在进行增删改操作时会对数组元素加锁(Node)

## 源码分析

### 重要常量
```Java
    /**
     * 最大容量2^30
     */
    private static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * 默认容量2^4
     */
    private static final int DEFAULT_CAPACITY = 16;

    /**
     * toArray的最大长度
     */
    static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    /**
     * 默认并发级别
     */
    private static final int DEFAULT_CONCURRENCY_LEVEL = 16;

    /**
     * 负载因子，默认为0.75f
     */
    private static final float LOAD_FACTOR = 0.75f;

    /**
     * 链表转红黑树的阀值
     */
    static final int TREEIFY_THRESHOLD = 8;

    /**
     * 红黑色转链表的阀值
     */
    static final int UNTREEIFY_THRESHOLD = 6;

    /**
     * 转换操作发生的最小容量
     */
    static final int MIN_TREEIFY_CAPACITY = 64;

    /**
     * 扩容线程所负责的区间大小最低为16，避免发生大量的内存冲突
     */
    private static final int MIN_TRANSFER_STRIDE = 16;

    /**
     * 用于生成当前数组对应的基数戳
     */
    private static int RESIZE_STAMP_BITS = 16;

    /**
     * 表示最多能有多少个线程能够帮助进行扩容，因为sizeCtl只有低16位用于标识，所以最多只有2^16-1个线程帮助扩容
     */
    private static final int MAX_RESIZERS = (1 << (32 - RESIZE_STAMP_BITS)) - 1;

    /**
     * 将基数戳左移的位数，保证左移后的基数戳为负值，然后再加上n+1,表示n个线程正在扩容
     */
    private static final int RESIZE_STAMP_SHIFT = 32 - RESIZE_STAMP_BITS;

    //hash值为-1处的节点代表forwarding node
    static final int MOVED     = -1; // hash for forwarding nodes
    //数组位置中红黑树根节点的hash值为-2，小于0
    static final int TREEBIN   = -2; // hash for roots of trees
    //保留字段
    static final int RESERVED  = -3; // hash for transient reservations
    //将HASH_BITS和普通节点的hash相与，将hash值最高位置0，从而保证普通节点的hash值都是>=0的
    static final int HASH_BITS = 0x7fffffff; // usable bits of normal node hash

    /** CPU核心数量 */
    static final int NCPU = Runtime.getRuntime().availableProcessors();
```

### 重要属性
```Java
    /**
     * 节点数组，用于存储键值对，当第一次插入时进行初始化。
     */
    transient volatile Node<K,V>[] table;

    /**
     * 只有当数组处于扩容过程时，nextTable才不为null;否则其他时刻，nextTable为null;
     * nextTable主要用于扩容过程中指向扩容后的新数组
     */
    private transient volatile Node<K,V>[] nextTable;

    /**
     * 基本计数器，用来保存ConcurrentHashMap中键值对的个数；
     * 在没有多线程竞争的情况下使用
     */
    private transient volatile long baseCount;

    /**
     * 当值为-1时, 代表数组正在被初始化;
     * 按照源码注释翻译，当值为-(1+扩容线程数), 代表数组正在被多个线程扩容。但是其实不是这样的，当线程进行扩容时，会根据resizeStamp函数生成一个基数戳rs，然后((rs<<RESIZE_STAMP_SHIFT)+n+1)这才是表示n个线程在扩容。
     * 当table为null时, 代表要初始化的容量大小; 否则代表下次要扩容的容量
     */
    private transient volatile int sizeCtl;

    /**
     * 用于扩容过程中，指示原数组下一个分割区间的上界位置
     */
    private transient volatile int transferIndex;

    /**
     * Spinlock (locked via CAS) used when resizing and/or creating CounterCells.
     */
    private transient volatile int cellsBusy;

    /**
     * 计数器表，多线程更新{@baseCount}时竞争失败的值
     */
    private transient volatile CounterCell[] counterCells;
```

### 构造函数
```Java
    /**
     * 使用默认大小(16)和默认负载因子(0.75f)创建一个空ConcurrentHashMap,第一次put操作时初始化.
     */
    public ConcurrentHashMap() {
    }

    /**
     * 使用指定初始容量和默认负载因子(0.75)初始化一个空ConcurrentHashMap,第一次put操作时初始化
     */
    public ConcurrentHashMap(int initialCapacity) {
    	// 初始化容量检查
        if (initialCapacity < 0)
            throw new IllegalArgumentException();
        int cap = ((initialCapacity >= (MAXIMUM_CAPACITY >>> 1)) ?
                   MAXIMUM_CAPACITY :
                   tableSizeFor(initialCapacity + (initialCapacity >>> 1) + 1));
        this.sizeCtl = cap;
    }

    /**
     * 构造一个包含指定map中所有节点的ConcurrentHashMap
     */
    public ConcurrentHashMap(Map<? extends K, ? extends V> m) {
        this.sizeCtl = DEFAULT_CAPACITY;
        putAll(m);
    }

    /**
     * 使用指定初始容量和负载因子构造一个空ConcurrentHashMap
     */
    public ConcurrentHashMap(int initialCapacity, float loadFactor) {
        this(initialCapacity, loadFactor, 1);
    }

    /**
     * 使用指定的初始容量、负载因子和并发数构造一个空ConcurrentHashMap
     */
    public ConcurrentHashMap(int initialCapacity,
                             float loadFactor, int concurrencyLevel) {
    	// 参数检查
        if (!(loadFactor > 0.0f) || initialCapacity < 0 || concurrencyLevel <= 0)
            throw new IllegalArgumentException();
        if (initialCapacity < concurrencyLevel)   // Use at least as many bins
            initialCapacity = concurrencyLevel;   // as estimated threads
        long size = (long)(1.0 + (long)initialCapacity / loadFactor);
        int cap = (size >= (long)MAXIMUM_CAPACITY) ?
            MAXIMUM_CAPACITY : tableSizeFor((int)size);
        this.sizeCtl = cap;
    }
```
### 插入操作
```Java
    public V put(K key, V value) {
        return putVal(key, value, false);
    }

    /**
     * 插入键值对
     * 
     * 流程：
     * 步骤1. 参数检查，key和value不允许为null
     * 步骤2. 如果table数组未初始化，则初始化
     * 步骤3. 计算并找到欲插入的键值对在table数组中的位置
     * 步骤4. 如果hash桶中没有节点，则直接创建Node节点(无锁操作)
     * 步骤5. 如果有其他线程正在扩容，则协助扩容
     * 步骤6. hash桶中有节点，synchronized锁住头节点，并再次校验头结点hash，以防其他线程更新
     * 步骤7. 如果桶中是链表结构，则遍历链表，有相同key的节点(hashCode & equals)直接覆盖value，没有相同节点创建新节点插入链表尾部，判断链表节点数大于8转为红黑树或扩容一倍容量
     * 步骤8. 如果桶中是红黑树结构，则调用红黑树的方法追加节点
     * 步骤9. 增加baseCount计数，过程中有可能需要扩容
     */
    final V putVal(K key, V value, boolean onlyIfAbsent) {
    	// 步骤1. 参数非空检查，key或value如果为null，则抛出异常，否则执行步骤2
        if (key == null || value == null) throw new NullPointerException();
        // 步骤2. 计算key的hash值，完成后执行步骤3
        int hash = spread(key.hashCode());
        int binCount = 0;
        for (Node<K,V>[] tab = table;;) { //外层死循环
            Node<K,V> f; int n, i, fh;
            // 步骤3. 如果table还没有初始化，则初始化table数组，完成后执行步骤5，否则执行步骤4
            if (tab == null || (n = tab.length) == 0)
                tab = initTable(); // 初始化table数组 
            // 步骤4. 如果桶为空，则尝试CAS操作直接插入新节点
            // 根据hash值计算得到数组下标并查看对应可桶，如果为空创建一个新节点CAS尝试插入
            else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {
                if (casTabAt(tab, i, null,
                             new Node<K,V>(hash, key, value, null)))
                    break;                   // no lock when adding to empty bin
            }
            // 步骤5. 如果当前桶中头节点的hash值为MOVED，说明有其他线程正在扩容，则帮助扩容
            else if ((fh = f.hash) == MOVED)
                tab = helpTransfer(tab, f);
            else {
                V oldVal = null;
                // 采用synchronized方式加锁，锁住头结点
                synchronized (f) {
                	// 步骤6. 二次校验头结点是否相同
                	// 重新取出桶中的头结点与之前取出的头结点作比较
                    if (tabAt(tab, i) == f) {
                    	// 步骤7. 桶中存储的是链表结构
                    	// 节点hash值大于等于0时为链表结构
                        if (fh >= 0) {
                            binCount = 1;
                            // 遍历链表的每个节点
                            for (Node<K,V> e = f;; ++binCount) {
                                K ek;
                                // 如果节点的key与要插入的key相等(hashCode equals),则覆盖节点value并跳出循环
                                if (e.hash == hash &&
                                    ((ek = e.key) == key ||
                                     (ek != null && key.equals(ek)))) {
                                    oldVal = e.val;
                                    if (!onlyIfAbsent)
                                        e.val = value;
                                    break;
                                }
                                // 如果找到链表尾部没有找到有相同key，则创建一个Node节点插入链表的尾部并跳出循环
                                Node<K,V> pred = e;
                                if ((e = e.next) == null) {
                                    pred.next = new Node<K,V>(hash, key,
                                                              value, null);
                                    break;
                                }
                            }
                        }
                        // 步骤8. 桶中存储的为红黑树结构
                        else if (f instanceof TreeBin) {
                            Node<K,V> p;
                            binCount = 2;
                            // 调用putTreeVal方法插入键值对
                            if ((p = ((TreeBin<K,V>)f).putTreeVal(hash, key,
                                                           value)) != null) {
                                oldVal = p.val;
                                if (!onlyIfAbsent)
                                    p.val = value;
                            }
                        }
                    }
                }
                //步骤9. 如果桶一桶中节点数达到8个时，扩容或将链表转为红黑树
                if (binCount != 0) {
                    if (binCount >= TREEIFY_THRESHOLD)
                        treeifyBin(tab, i);
                    if (oldVal != null)
                        return oldVal;
                    break;
                }
            }
        }
        addCount(1L, binCount); // 计数
        return null;
    }
```

### 查找操作

### 扩容操作

## JDK1.8与JDK1.7的区别
	


