# ArrayList

## 数据结构
> 底层为数组结构，ArrayList相当于动态数组；

## 怎么保证并发
> ArrayList是非线程安全的，如需要在多线程环境下使用需在外部加同步；

## 效率
> 尽量估算出要放入ArrayList的数据数量，初始化时调用ArrayList的ArrayList(int initialCapacity)构造函数初始化一个指定大小的ArrayList，避免频繁扩容带来的性能消耗；

## 重要的属性
```Java
	transient Object[] elementData;//存放元素的数组
	private int size;//ArrayList的大小，包含的元素数量；
```

## 重要的方法
> ArrayList(int initialCapacity);//构造函数
>> 1. 校验initialCapacity
>> 2. 如果initialCapacity为0初始化一个空数组，否则初始化一个大小为initialCapacity的数组；

> ensureCapacityInternal(int minCapacity);//扩容方法   
调用grow(minCapacity)方法扩容elementData数组； minCapacity为扩容的最小容量； minCapacity必须大于10，如果小于10则minCapacity=10；

> grow(int minCapacity);//扩容方法
>> 1. 计算elementData数组扩容后的容量，新容量是原有容量的1.5倍（newCapacity=oldCapacity+(oldCapacity>>1)）；
>> 2. 新的容量小于minCapacity则新容量为minCapacity； 3.初始化一个空数组，大小为扩容后的容量，调用System.arrayCopy方法将原数组的内容复制给新数组；

> size();//ArrayList的大小,返回size，包含的元素数量

> isEmpty();//是否为空
>> 如果ArrayList的size==0返回true，否则返回false；

> indexOf(Object o);//查找返回o在ArrayList中的位置
>> 1. 如果o为null时,遍历elementData数组元素，用==查找null在数组中的位置并返回；
>> 2. 如果o不为null，遍历elementData数组元素，用equals方法找到值为o的数组元素位置并返回；
>> 3. 如果以上遍历都位找到则返回-1；

> get(int index);
>> 1. 检查index是否大于0
>> 2. 返回elementData数组index位置的元素

> set(int index, E element);
>> 1. 检查index是否大于0
>> 2. 获取elementData数组index位置的元素；
>> 3. 替换elementData数组index位置元素为element；
>> 4. 返回2.获取的元素；

> add(E e);
>> 1. 如果elementData数组容量小于size+1则对数组进行扩容操作；
>> 2. 在elemenetData的size位置插入e；

> add(int index, E element);
>> 1. 检查iindex
>> 2. 如果数组容量小于size+1则扩容；
>> 3. 调用System.arrayCopy方法将elementData数组index位置之后的元素向后挪动一位；
>> 4. 在elementData数组的index位置插入element；
>> 5. 数组大小计数+1；

> remove(int index)
>> 1. 检查index
>> 2. 得到elementData数组index位置的元素；
>> 3. 调用System.arrayCopy方法将elementData数组index位置之后的元素全部向前挪动一位；
>> 4. 将elementData数组的最后一个元素赐null值，然后包含元素容量计数-1；
>> 5. 返回2.得到的元素；

> remove(Object o)
>> 1. 遍历elementData数组元素，如果o为null则使用==查找数组元素为null的索引位置，如果o不为null则用equals方法查找数组元素值为o的索引位置；
>> 2. 后续步骤与remove(int index)方法类似；

> clear()
>> 遍历elementData数组，将每个元素置为null，修改size为0；