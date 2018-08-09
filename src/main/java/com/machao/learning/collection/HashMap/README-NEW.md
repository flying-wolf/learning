# HashMap源码解析 for JDK1.8

## 概述
> HashMap底层结构由数组+链表+红黑树实现
> HashMap允许null键和null值，计算哈希值时，null键hash值为0
> HashMap不保证键值对的顺序，键值的顺序可能会发生变化
> HashMap非线程安全，如果需要在并发场景下使用，可以考虑使用HashTable或ConcurrentHashMap

## 原理
> HashMap底层基于散列算法实现，其数据结构是由数组和链表(或红黑树结构)组成;
在进行增删改查操作时，首先要根据元素的hash值计算出bucket桶的位置(数组中的位置)，然后在bucket上继续查找元素在链表或树结构中的位置;
当hash值计算发生碰撞时将发生碰撞的元素作为一个链表存储在bucket中；
为了保证一个bucket上发生碰撞节点数较多时查找元素的性能，JDK1.8中引入了红黑树，当在同一个bucket中的链表的节点数超过8个时将链表转为红黑树，当节点数少于6个时将红黑树转为链表。

## 源码分析

### 重要常量

```Java
	/**
     * 默认初始容量，必须是2的幂次方，默认为16
     */
    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16

    /**
     * 最大容量
     */
    static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * 默认的加载因子
     */
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * 链表转换红黑树的计算阀值，当加入桶中链表的元素大于等于此值时将链表转换为红黑树
     */
    static final int TREEIFY_THRESHOLD = 8;

    /**
     * 红黑树转链表的计算阀值，当红黑树中的元素数小于等于此值时将红黑树转为链表
     */
    static final int UNTREEIFY_THRESHOLD = 6;

    /**
     * 最小结构转换容量，即当HashMap容量大于64时，TREEIFY_THRESHOLD和UNTREEIFY_THRESHOLD才起作用
     */
    static final int MIN_TREEIFY_CAPACITY = 64;
```

### 重要属性
```Java
	/**
     * 储存Node节点的数组
     */
    transient Node<K,V>[] table;

    /**
     * 储存HashMap中所有键值对的集合
     */
    transient Set<Map.Entry<K,V>> entrySet;

    /**
     * HashMap中元素数量
     */
    transient int size;

    /**
     * 结构性修改计数器
     */
    transient int modCount;

    /**
     * 扩容阀值，当HashMap中的节点数大于等于此值时要进行扩容操作.
     */
    int threshold;

    /**
     * 加载因子，用于计算扩容阀值
     */
    final float loadFactor;
```

### 构造函数

```Java
    /**
     * 使用指定的初始容量和加载因子构造一个空的 HashMap 。
     * 
     * 第一次put操作时会做扩容操作。
     * 
     */
    public HashMap(int initialCapacity, float loadFactor) {
    	// 参数检查
    	if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " +
                                               initialCapacity);
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " +
                                               loadFactor);
        // 初始化加载因子
        this.loadFactor = loadFactor;
        // 计算初始化扩容阀值，根据给定的初始容量计算出最小2次幂
        this.threshold = tableSizeFor(initialCapacity);
    }

    /**
     * 使用指定的初始容量和默认加载因子（0.75）构造一个空的 HashMap 。
     */
    public HashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * 使用默认初始容量（16）和默认加载因子（0.75）构造一个空的 HashMap 。
     */
    public HashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR; // all other fields defaulted
    }

    /**
     * 构造一个包含给定Map中所有元素的HashMap，默认加载因子为（0.75）。
     */
    public HashMap(Map<? extends K, ? extends V> m) {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        putMapEntries(m, false);
    }

```

### 插入操作
![Image ./HashMap-PutValue.png]

- 1. 如果HashMap没有初始化，则调用resize()方法初始化
- 2. 根据要插入元素的hash值计算找出bucket位置(数组中的位置)
	-- 1). 如果bucket中没有数据，直接创建新节点插入bucket，返回null
	-- 2). 如果bucket中存储的是红黑树,调用树的插入/更新操作
	-- 3). 如果bucket中存储的是链表，遍历链表，节点存在则更新节点数据并返回原数据，如果节点不存在则生成新节点数据插入此链表尾部，返回null
- 3. 插入成功后判断当前存储的键值对总数大于扩容阀值时，调用扩容方法扩容

```Java
    /**
     * 将指定的值与此映射中的指定键相关联。如果映射先前包含键的映射，则替换旧值
     * 调用putVal()方法实现
     * 流程：
     * 
     */
    public V put(K key, V value) {
        return putVal(hash(key), key, value, false, true);
    }

    /**
     * 向HashMap中插入元素
     * 1. 判断table数组为空，则需要进行初始化，调用resize()方法扩容
     * 2. 根据键值key计算hash值得到要插入的数组索引i，并找到要插入的桶p，如果桶为null则直接插入新节点并执行步骤6，如果桶不为null则执行步骤3
     * 3. 判断如果桶中第一个元素的key和要插入元素的key是否相同(hashCode和equals)，如果相同直接覆盖value，否则执行步骤4
     * 4. 判断如果桶中存放的是一颗红黑树，则直接在红黑树中插入键值对，否则执行步骤5
     * 5. 此时桶中存放的是个链表，遍历链表，如果key在链表中已存在则直接覆盖value，否则在链表尾部插入新节点并检查链表长度，如果链表长度大于8时，数组容量小于64执行扩容操作，数组容量大于64将链表转为红黑树
     * 6. 插入成功后，判断实际存在的键值对数量size超过了扩容阀值threshold时进行扩容
     */
    final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
        Node<K,V>[] tab; Node<K,V> p; int n, i;
        // 步骤1. tab为空则创建
        // table未初始化或者长度为0，进行扩容
        if ((tab = table) == null || (n = tab.length) == 0)
            n = (tab = resize()).length;
        // 步骤2. 计算index，并对null值做处理
        // (n - 1) & hash 计算出元素需要放在哪个桶中，并赋值给i
        // 获取当前桶并赋值给p
        // 如果桶为空，则直接创建新节点放入桶中
        if ((p = tab[i = (n - 1) & hash]) == null)//如果hash所在的桶为空直接put
            tab[i] = newNode(hash, key, value, null);
        else {// 如果桶不为空
            Node<K,V> e; K k;
            // 步骤3. 如果节点key存在，直接覆盖value
            // 比较桶中第一个元素(数组中的节点)的hash值相等，key相等
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
                e = p;// 将第一个元素赋值给e，用e来记录
            // 步骤4. 判断当前桶上存的是红黑树
            // hash值不相等，即key不相等；为红黑树结点
            else if (p instanceof TreeNode)
            	//放入树中
                e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
            // 步骤5. 当前桶中为链表结构
            else {
            	// 在链表尾部插入节点
                for (int binCount = 0; ; ++binCount) {
                	// 找到链表尾部
                    if ((e = p.next) == null) {
                    	// 在尾部插入新节点
                        p.next = newNode(hash, key, value, null);
                        // 如果链表节点数大于等于阀值8，链表转红黑树
                        if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                            treeifyBin(tab, hash);// 如果table容量大于64，链表转红黑树，如果table容量小于64则扩容
                        break;// 跳出循环
                    }
                    // 链表中节点的hash、key值和要插入的节点hash、key值都相等，此时节点存在需要更新
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        break;// 相等，跳出循环
                    // 用于遍历桶中的链表，与前面的e = p.next组合，可以遍历链表
                    p = e;
                }
            }
            // 当前节点e = p.next不为null，表示链表中原本存在相同的key
            if (e != null) { // existing mapping for key
            	// 记录e的value
                V oldValue = e.value;
                // 当onlyIfAbsent为false或者旧值为null时决定key相同节点的值需要替换
                if (!onlyIfAbsent || oldValue == null)
                    e.value = value;// 替换旧值
                afterNodeAccess(e);// 回调函数
                return oldValue;// 返回旧值
            }
        }
        // 结构性修改计数累加
        ++modCount;
        // 步骤6. 超过最大容量需要扩容
        // 实际大小超过扩容阀值时，调用resize方法扩容
        if (++size > threshold)
            resize();
        // 插入后的回调函数
        afterNodeInsertion(evict);
        return null;
    }
```

### 查找操作
- 1. 如果当前HashMap没有初始化或为空返回null
- 2. 根据要查找数据的hash值计算出在桶的位置(table数组的索引位置)
- 3. 如果桶中第一个节点的key和要查找的key相同(hashCode && equals)则返回第一个节点的数据, 如果key不同且桶中只有一个节点返回null
- 4. 如果桶中存储的是红黑树，则查找红黑树
- 5. 如果桶中存储的是链表，则遍历此链表查找key相同的节点返回节点数据，如果节点不存在返回null

```Java
    public V get(Object key) {
        Node<K,V> e;
        return (e = getNode(hash(key), key)) == null ? null : e.value;
    }

    /**
     * 根据hash和key查找节点
     * 流程：
     * 1. 如果table未初始化或根据hash值找到的bucket为空则返回null，否则执行步骤2
     * 2. 如果bucket中储存的第一个节点的key和要查找节点的key相同(hashCode && equals),返回bucket的第一个节点，否则执行步骤3
     * 3. 如果bucket中只有一个节点，返回null，否则执行步骤4
     * 4. 如果bucket中存储的是红黑树，则调用红黑树的getTreeNode()方法查找并返回，否则执行步骤5
     * 5. bucket中存储的是链表，遍历链表找到key和要查找的key相同(hashCode && equals)的节点并返回
     */
    final Node<K,V> getNode(int hash, Object key) {
        Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
        //  判断table数组已经初始化，且根据hash值找到的桶(bucket)不为空
        if ((tab = table) != null && (n = tab.length) > 0 &&
            (first = tab[(n - 1) & hash]) != null) {
        	// 判断bucket中第一个节点的hash与key和要查找的hash与key相等，则返回此节点
            if (first.hash == hash && // always check first node
                ((k = first.key) == key || (key != null && key.equals(k))))
                return first;
            // 如果bucket中不止一个节点
            if ((e = first.next) != null) {
            	// 判断如果bucket上存的是红黑树，则查找红黑树
                if (first instanceof TreeNode)
                    return ((TreeNode<K,V>)first).getTreeNode(hash, key);
                // 如果bucket上存的是链表，则查找链表
                do {
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        return e;
                } while ((e = e.next) != null);
            }
        }
        return null;
    }
```


