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


### 重要属性

### 构造函数

### 插入操作

### 查找操作

### 扩容操作

## JDK1.8与JDK1.7的区别
	


