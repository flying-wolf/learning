# ThreadPoolExecutor

## 线程池状态
> RUNNING
>> 状态说明：线程池处在RUNNING状态时，会接收新任务，会处理任务队列中的任务。
>> 状态切换：RUNNING状态为线程池的初始状态，也就是说，线程池一旦被创建就处于RUNNING状态，且线程池中的任务数为0。
> SHUTDOWN
>> 状态说明：线程池处于SHUTDOWN状态时，不会接收新任务，但会处理任务队列中的任务。
>> 状态切换：调用了线程池得shutdown()方法时，线程池的状态由RUNNING ——> SHUTDOWN。
> STOP
>> 状态说明：线程池处于STOP状态时，不会接收新任务，也不会处理任务队列中的任务。
>> 状态切换：调用了线程池的shutdownNow()方法时，线程池的状态由(RUNNING or SHUTDOWN) ——> STOP。
> TIDYING
>> 状态说明：所有任务已终止，ctl记录的任务数量为0，线程池会变为TIDYING状态。当线程池变为TIDYING状态时，会执行钩子函数terminated()。
>> 状态切换：当线程池在SHUTDOWN状态下，任务队列为空并且线程池中执行的任务也为空时，就会由SHUTDOWN ——> TIDYING；当线程池在STOP状态下时，线程池中执行的任务为空时，就会由STOP ——> TIDYING。
> TERMINATED
>> 状态说明：线程池彻底终止，就会变成TERMINATED状态。
>> 状态切换：线程池处在TIDYING状态时，执行完terminated()之后，就会由TIDYING ——> TERMINATED。

## 任务的执行
> 1. 使用ThreadPoolExecutor类创建线程池，它的构造函数参数如下：

>> codePoolSize:核心线程数

>> maximumPoolSize:最大线程数

>> keepAliveTime:空闲线程存活时间

>> unit:keepAliveTime的时间单位

>> workQueue:任务队列，默认使用LinkedBlockingQueue
>>> ArrayBlockingQueue:一个基于数组的有界的阻塞队列，按FIFO(先进先出)排序
>>> LinkedBlockingQueue:一个基于链表的可选是否有界的阻塞队列，按FIFO(先进先出)排序
>>> SynchronousQueue:一个不储存元素的阻塞队列，任何入队操作都要等待其它线程出队操作
>>> PriorityBlockingQueue:一个基于数组结构并且具有优先级的无界阻塞

>> threadFactory:创建线程的工厂，默认DefaultThreadFactory

>> handler:饱和策略，默认策略为AbortPolicy
>>> AbortPolicy:直接抛出异常
>>> CallerRunsPolicy:使用调用者所在的线程来处理任务
>>> DiscardOldestPolicy:丢弃任务队列最靠前的任务，并执行当前任务
>>> DiscardPolicy:直接丢弃任务
>>> RejectedExecutionHandler:自定义饱和策略

> 2. 调用submit或者execute方法提交任务，具体执行过程如下：
>>> 正在执行的线程数小于codePoolSize时，创建新线程来执行新提交的任务
>>> 如果正在执行得线程数大于等于codePoolSize时，将新提交的任务放入workQueue中等待执行
>>> 如果workQueue已满，且正在执行的线程小于maximumPoolSize时，创建新线程执行任务
>>> 如果workQueue已满，且正在执行的线程大于等于maximumPoolSize时，执行饱和策略

## 线程池中的线程初始化

## 任务缓存队列及排队策略

## 任务拒绝策略

## 线程池的关闭

## 线程池容量的动态调整