# JVM 线上问题排查命令

## 分类   
| 功能             | 命令             | 描述                                                                |
| --------------- | ---------------:|:-------------------------------------------------------------------:|
| 线程             | jstack          | 查看Java程序的Java Stack和Native Stack信息，可以轻松得知当前线程的运行情况。|
| 内存             | jmap            | 查看运行Java程序的内存分配详细情况。例如 实例个数、大小等信息。               |
| 性能             | jstat           | 查看ClassLoader，Complier，GC相关信息，可实时监控资源和性能。             |    

## jps命令（查找JVM的进程ID）   
```shell
jps -opt pid

# 附带jvm参数信息
jps -v

# 只显示id
jps -q

# 输出main method的参数
jps -m

# 输出完全的包名，应用主类名，jar的完全路径
jps -l
```   
    
    
## jstat命令（实时监控资源和性能）   
```shell
jps  -opt  pid -h n (每隔多少行出现行头)interval(间隔多久)  count（多少次）

# 查询gc百分比
jstat -gcutil pid 1000

# 显示三代的使用量
jstat -gccapacity pid 1000

# 显示加载class的数量，及所占空间等信息
jstat -class pid

# 显示VM实时编译的数量等信息
jstat -compiler pid

# 可以显示gc的信息，查看gc的次数，及时间。其中最后五项，分别是young gc的次数，young gc的时间，full gc的次数，full gc的时间，gc的总时间。
jstat -gc pid

# 当前VM执行的信息
jstat -printcompilation pid

# 用于查看垃圾收集的统计情况（这个和-gcutil选项一样），如果有发生垃圾收集，它还会显示最后一次及当前正在发生垃圾收集的原因
jstat -gccause pid
```    

## jinfo命令（查看或修改运行时Java进程参数）   
```shell
jinfo -opt  pid

# 打印所有pid相关的vm配置
jinfo pid

# 只显示某个配置 例如永久代：MaxPermSize
jinfo -flag MaxPermSize pid

# -flag [+|-]< name >：设置或取消指定java虚拟机参数的布尔值
jinfo -flag +PrintGCDetails 2000

# jinfo虽然可以在java程序运行时动态地修改虚拟机参数，但并不是所有的参数都支持动态修改

```


## jmap命令（查看Java进程的内存分配信息）
```shell
 jmap  -opt  pid
 
# 使用hprof二进制形式,输出jvm的heap内容到文件=. live子选项是可选的，假如指定live选项,那么只输出活的对象到文件
jmap -dump:format=b,file=test.bin pid

# 打印正等候回收的对象的信息
jmap -finalizerinfo pid

# 打印heap的概要信息，GC使用的算法，heap的配置及wise heap的使用情况
jmap -heap pid

# 打印每个class的实例数目,内存占用,类全名信息. VM的内部类名字开头会加上前缀”*”. 如果live子参数加上后,只统计活的对象数量.
# 统计大对象的时候很实用 但是会触发一次full gc
jmap histo:live pid | head -n 23

# 打印classload和jvm heap长久层的信息. 包含每个classloader的名字,活泼性,地址,父classloader和加载的class数量. 另外,内部String的数量和占用内存数也会打印出来
jmap -permstat pid
```

## jstack命令（查看Java堆栈信息）
```shell
jstack -opt  pid

# 一般使用是将jstack的结果输出到文件中来查看
jstat pid > jstat01.log

# 或者直接使用grep查找
jstat pid |grep -C 10 pid(16进制转换之后)

# 或者直接搜索关键字 Deadlock 之类的
```

## jhat命令（转存dump浏览器，分析内存泄露）
```shell
jhat -opt

# 可浏览‘jmap -dump:live,format=b,file=../ pid’生成的dump文件
# -J参数可调整堆内存大小，默认的堆内存可能不足以加载整个dump文件
jhat -J-Xmx1024m /tmp/dump.bin
```


