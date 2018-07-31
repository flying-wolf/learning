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

## 参数详解
> corePoolSize: 保持在池中的线程数（PS:即使它们处于空闲状态）

> maximumPoolSize: 池中允许的最大线程数

> keepAliveTime: 当线程数超过核心线程数时，超出的线程最大生存时间

> unit: keepAliveTime的单位

> workQueue: 维护待处理任务的队列，任务队列如下几种类型：
>> ArrayBlockingQueue: 一个基于数组结构的有界阻塞队列，元素按照先进先出(FIFO)原则排序；
>> LinkedBlockingQueue: 一个基于链表结构的可选是否有界的阻塞队列，元素按照先进先出(FIFO)原则排序，吞吐量通常要高于ArrayBlockingQueue；
>> SynchronousQueue: 一个不存储元素的阻塞队列，每次入队操作需等待其他线程的出队操作，否则操作会一直阻塞，反之相同，吞吐量通常要高于LinkedBlockingQueue；
>> PriorityBlockingQueue: 一个基于数组结构且具有优先级的阻塞队列；

> threadFactory: 创建线程的工厂,默认为DefaultThreadFactory

> handler: 处理饱和任务的策略，常用策略如下：
>> AbortPolicy: 直接抛出异常（默认策略）；
>> CallerRunsPolicy: 使用调用者所在的线程执行任务；
>> DiscardOldestPolicy: 丢弃任务队列中最靠前的任务，并执行当前任务；
>> DiscardPolicy: 直接丢弃任务；
>> RejectedExecutionHanlder: 实现此接口自定义处理策略；



## 线程池的工作过程

> 默认情况下，创建线程池之后，线程池中是没有线程的，需要提交任务后才会创建线程。
当调用execute()方法添加一个线程时，线程池会做如下判断：
	>> 1. 如果正在运行的线程数小于corePoolSize，则创建新的线程执行任务。
	>> 2. 如果正在运行的线程数大于等于corePoolSize，则将这个任务放入任务队列中等待执行。
	>> 3. 如果任务队列满了，而且正在运行的线程数小于maximumPoolSize，则再创建非核心线程执行任务。
	>> 4. 如果任务队列满了，而且正在运行的线程数大于等于maximumPoolSize，则线程池会执行饱和策略（默认策略是直接抛出异常RejectExecutionException）。   
> 当一个线程完成任务时，它会从任务队列中取下一个任务来执行。
> 当一个线程空闲且当前运行的线程数大于corePoolSize时，超过一定的时间(keepAliveTime)时，这个线程就会被停掉。


## 线程池的创建和使用

1. SingleThreadExecutor: 单个后台线程(其任务队列是无界的)。
```Java
    public static ExecutorService newSingleThreadExecutor() {
        return new FinalizableDelegatedExecutorService
            (new ThreadPoolExecutor(1, 1,
                                    0L, TimeUnit.MILLISECONDS,
                                    new LinkedBlockingQueue<Runnable>()));
    }
```    
  
> 创建一个单线程的线程池。这个线程池只有一个核心线程在工作，相当于带线程串行执行所有任务。
如果这个唯一的线程因为异常结束，那么会有一个新的线程来替代它。
此线程池保证索引任务的执行顺序按照任务的提交顺序执行。   

2. FixedThreadPool: 只有核心线程的线程池，大小固定(其任务队列是无界的)。
```Java
    public static ExecutorService newFixedThreadPool(int nThreads) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                                      0L, TimeUnit.MILLISECONDS,
                                      new LinkedBlockingQueue<Runnable>());
    }
```

> 创建固定大小的线程池，每次提交一个任务就创建一个线程，直到线程池达到它的最大容量。
线程池的大小一旦达到最大值就会保持不变，如果某个线程因为异常而结束，那么线程池会补充一个新线程。   

3. CachedThreadPool: 无界线程池，可以进行自动线程回收。
```Java
    public static ExecutorService newCachedThreadPool() {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                      60L, TimeUnit.SECONDS,
                                      new SynchronousQueue<Runnable>());
    }
```

> 如果线程池的大小超过了处理任务所需的线程，那么就会回收部分空闲(60秒不执行任务)的线程，当任务数增加时，此线程从又可以智能的添加新线程来处理任务。
此线程池不会堆线程池大小做限制，线程池大小完全依赖于操作系统（或者说JVM）能够创建的最大线程大小。
SynchronousQueue是一个缓冲任务区为1的阻塞队列。   

4. ScheduledThreadPool: 核心线程池固定，大小无限的线程池。此线程池支持定时以及周期性执行任务的需求。
```Java

    public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
        return new ScheduledThreadPoolExecutor(corePoolSize);
    }

    public ScheduledThreadPoolExecutor(int corePoolSize) {
        super(corePoolSize, Integer.MAX_VALUE, 0, NANOSECONDS,
              new DelayedWorkQueue());
    }
```    


## 线程池的实现原理
- 在ThreadPoolExecutor中主要由Worker类来控制线程的复用。
- Worker本身实现了Runnable接口，同时拥有一个thread，这个thread就是要开启的线程。
- 在新建Worker对象时同时新建一个Thread对象，同时将Worker自己作为参数传入Thread，这样Thread的start()方法调用时，运行的实际上是Worker的run()方法，run()方法调用runWorker()方法。
- runWorker()中，有个while循环，一直调用getTask()方法从workQueue(任务队列)中取出Runnable任务。
- 因为workQueue是个阻塞队列，workQueue.take()得到如果时空，则进入等待状态直到workQueue有新的任务被加入时唤醒阻塞的线程；所以一般情况下Thread的run()方法就不会结束，而时不断执行从workQueue里取出的任务，这就达到了线程复用的原理。
