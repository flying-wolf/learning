package com.machao.learning.concurrent.ThreadPoolExecutor;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadPoolExecutor extends AbstractExecutorService {
	// ctl是用一个AtomicInteger变量存放两个字段，一共32位
	// workerCount线程池线程个数。低29位表示。以后如果线程池支持的线程数量变多，可以改成AtomicLong。
	// runState线程池状态，高3位表示。
	// 默认是RUNNING状态，线程个数为0
	private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));
	// 线程个数掩码位数
	private static final int COUNT_BITS = Integer.SIZE - 3;
	// 程最大个数(低29位)00011111111111111111111111111111
	private static final int CAPACITY = (1 << COUNT_BITS) - 1;

	// （高3位）：111：接受新任务并且处理任务队列里的任务
	private static final int RUNNING = -1 << COUNT_BITS;
	// （高3位）：000：不接受新任务但是处理任务队列里的任务
	private static final int SHUTDOWN = 0 << COUNT_BITS;
	// （高3位）：001：不接受新任务并且抛弃任务队列里的任务同时会中断正在处理的任务
	private static final int STOP = 1 << COUNT_BITS;
	// （高3位）：010：所有任务都执行完（包含任务队列任务）当前线程池活动线程为0，将要调用terminated方法
	private static final int TIDYING = 2 << COUNT_BITS;
	// （高3位）：011：终止状态。terminated方法调用完成以后的状态
	private static final int TERMINATED = 3 << COUNT_BITS;

	// 获取高3位 线程状态
    private static int runStateOf(int c)     { return c & ~CAPACITY; }
    // 获取低29位 线程个数
    private static int workerCountOf(int c)  { return c & CAPACITY; }
    // 拼装ctl新值，线程状态与线程个数
    private static int ctlOf(int rs, int wc) { return rs | wc; }

    /*
     * Bit field accessors that don't require unpacking ctl.
     * These depend on the bit layout and on workerCount being never negative.
     */

    private static boolean runStateLessThan(int c, int s) {
        return c < s;
    }

    private static boolean runStateAtLeast(int c, int s) {
        return c >= s;
    }

    private static boolean isRunning(int c) {
        return c < SHUTDOWN;
    }

    /**
     * Attempts to CAS-increment the workerCount field of ctl.
     */
    private boolean compareAndIncrementWorkerCount(int expect) {
        return ctl.compareAndSet(expect, expect + 1);
    }

    /**
     * Attempts to CAS-decrement the workerCount field of ctl.
     */
    private boolean compareAndDecrementWorkerCount(int expect) {
        return ctl.compareAndSet(expect, expect - 1);
    }

    /**
     * CAS操作线程数-1
     */
    private void decrementWorkerCount() {
        do {} while (! compareAndDecrementWorkerCount(ctl.get()));
    }

    /**
     * The queue used for holding tasks and handing off to worker
     * threads.  We do not require that workQueue.poll() returning
     * null necessarily means that workQueue.isEmpty(), so rely
     * solely on isEmpty to see if the queue is empty (which we must
     * do for example when deciding whether to transition from
     * SHUTDOWN to TIDYING).  This accommodates special-purpose
     * queues such as DelayQueues for which poll() is allowed to
     * return null even if it may later return non-null when delays
     * expire.
     */
    private final BlockingQueue<Runnable> workQueue;

    /**
     * Lock held on access to workers set and related bookkeeping.
     * While we could use a concurrent set of some sort, it turns out
     * to be generally preferable to use a lock. Among the reasons is
     * that this serializes interruptIdleWorkers, which avoids
     * unnecessary interrupt storms, especially during shutdown.
     * Otherwise exiting threads would concurrently interrupt those
     * that have not yet interrupted. It also simplifies some of the
     * associated statistics bookkeeping of largestPoolSize etc. We
     * also hold mainLock on shutdown and shutdownNow, for the sake of
     * ensuring workers set is stable while separately checking
     * permission to interrupt and actually interrupting.
     */
    private final ReentrantLock mainLock = new ReentrantLock();

    /**
     * 工作线程集合
     */
    private final HashSet<Worker> workers = new HashSet<Worker>();

    /**
     * Wait condition to support awaitTermination
     */
    private final Condition termination = mainLock.newCondition();

    /**
     * 记录最大池的大小.
     */
    private int largestPoolSize;

    /**
     * 完成任务的计数器。
     * 仅在终止工作线程时更新。
     * 更新时必须取得全局锁。
     */
    private long completedTaskCount;

    /*
     * All user control parameters are declared as volatiles so that
     * ongoing actions are based on freshest values, but without need
     * for locking, since no internal invariants depend on them
     * changing synchronously with respect to other actions.
     */

    /**
     * Factory for new threads. All threads are created using this
     * factory (via method addWorker).  All callers must be prepared
     * for addWorker to fail, which may reflect a system or user's
     * policy limiting the number of threads.  Even though it is not
     * treated as an error, failure to create threads may result in
     * new tasks being rejected or existing ones remaining stuck in
     * the queue.
     *
     * We go further and preserve pool invariants even in the face of
     * errors such as OutOfMemoryError, that might be thrown while
     * trying to create threads.  Such errors are rather common due to
     * the need to allocate a native stack in Thread.start, and users
     * will want to perform clean pool shutdown to clean up.  There
     * will likely be enough memory available for the cleanup code to
     * complete without encountering yet another OutOfMemoryError.
     */
    private volatile ThreadFactory threadFactory;

    /**
     * Handler called when saturated or shutdown in execute.
     */
    private volatile RejectedExecutionHandler handler;

    /**
     * Timeout in nanoseconds for idle threads waiting for work.
     * Threads use this timeout when there are more than corePoolSize
     * present or if allowCoreThreadTimeOut. Otherwise they wait
     * forever for new work.
     */
    private volatile long keepAliveTime;

    /**
     * 是否允许核心线程超时(默认为false)
     * 如果为false，则核心线程即使空闲时仍保持存活
     * 如果为true，则核心线程使用keepAliveTime时间超时等待工作
     */
    private volatile boolean allowCoreThreadTimeOut;

    /**
     * Core pool size is the minimum number of workers to keep alive
     * (and not allow to time out etc) unless allowCoreThreadTimeOut
     * is set, in which case the minimum is zero.
     */
    private volatile int corePoolSize;

    /**
     * Maximum pool size. Note that the actual maximum is internally
     * bounded by CAPACITY.
     */
    private volatile int maximumPoolSize;

    /**
     * The default rejected execution handler
     */
    private static final RejectedExecutionHandler defaultHandler =
        new AbortPolicy();

    /**
     * Permission required for callers of shutdown and shutdownNow.
     * We additionally require (see checkShutdownAccess) that callers
     * have permission to actually interrupt threads in the worker set
     * (as governed by Thread.interrupt, which relies on
     * ThreadGroup.checkAccess, which in turn relies on
     * SecurityManager.checkAccess). Shutdowns are attempted only if
     * these checks pass.
     *
     * All actual invocations of Thread.interrupt (see
     * interruptIdleWorkers and interruptWorkers) ignore
     * SecurityExceptions, meaning that the attempted interrupts
     * silently fail. In the case of shutdown, they should not fail
     * unless the SecurityManager has inconsistent policies, sometimes
     * allowing access to a thread and sometimes not. In such cases,
     * failure to actually interrupt threads may disable or delay full
     * termination. Other uses of interruptIdleWorkers are advisory,
     * and failure to actually interrupt will merely delay response to
     * configuration changes so is not handled exceptionally.
     */
    private static final RuntimePermission shutdownPerm =
        new RuntimePermission("modifyThread");

    /* The context to be used when executing the finalizer, or null. */
    private final AccessControlContext acc;

    /**
     * 工作线程类
     */
    private final class Worker
        extends AbstractQueuedSynchronizer
        implements Runnable
    {
        /**
         * This class will never be serialized, but we provide a
         * serialVersionUID to suppress a javac warning.
         */
        private static final long serialVersionUID = 6138294804551838833L;

        /** 当前正在运行的工作线程，如果ThreadFactory创建线程失败，则为null. */
        final Thread thread;
        /** 外部传入的初始线程，可能为null. */
        Runnable firstTask;
        /** 完成任务的计数器 */
        volatile long completedTasks;

        /**
         * Creates with given first task and thread from ThreadFactory.
         * @param firstTask the first task (null if none)
         */
        Worker(Runnable firstTask) {
            setState(-1); // runWorker之前禁止中断
            this.firstTask = firstTask;// 外部提交的任务
            this.thread = getThreadFactory().newThread(this);// 真实执行的线程
        }

        /** 委托给runWorker执行  */
        public void run() {
            runWorker(this);
        }

        // Lock methods
        //
        // The value 0 represents the unlocked state.
        // The value 1 represents the locked state.

        // 是否被独占，0-未被独占，1-被独占
        protected boolean isHeldExclusively() {
            return getState() != 0;
        }

        //尝试获取独占锁，成功返回true
        protected boolean tryAcquire(int unused) {
            if (compareAndSetState(0, 1)) {// CAS尝试更新状态
            	// 如果状态更新成功，设置当前线程为独占线程
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        // 尝试释放独占锁
        protected boolean tryRelease(int unused) {
            setExclusiveOwnerThread(null);//将独占线程置为null
            setState(0);// 设置状态为0
            return true;
        }
        // 获取锁
        public void lock()        { acquire(1); }
        // 尝试获取锁
        public boolean tryLock()  { return tryAcquire(1); }
        // 释放锁
        public void unlock()      { release(1); }
        // 是否被独占
        public boolean isLocked() { return isHeldExclusively(); }

        /**
         * 中断已启动的线程
         */
        void interruptIfStarted() {
            Thread t;
            // 校验当前执行的工作线程是否可以被中断
            // 状态>=0时(状态为-1时说明还没有执行runWorker()不可中断)并且当前线程没有被中断
            if (getState() >= 0 && (t = thread) != null && !t.isInterrupted()) {
                try {
                    t.interrupt();// 中断当前工作线程
                } catch (SecurityException ignore) {
                }
            }
        }
    }

    /*
     * Methods for setting control state
     */

    /**
     * Transitions runState to given target, or leaves it alone if
     * already at least the given target.
     *
     * @param targetState the desired state, either SHUTDOWN or STOP
     *        (but not TIDYING or TERMINATED -- use tryTerminate for that)
     */
    private void advanceRunState(int targetState) {
        for (;;) {
            int c = ctl.get();
            if (runStateAtLeast(c, targetState) ||
                ctl.compareAndSet(c, ctlOf(targetState, workerCountOf(c))))
                break;
        }
    }

    /**
     * 尝试终止线程池
     * 成功终止后会调用terminated()
     * 然后状态置为TERMINATED
     */
    final void tryTerminate() {
        for (;;) {// 自旋
            int c = ctl.get();// 获取ctl
            /*
             * 校验，以下几种场景时退出当前方法
             * 1.当前状态为RUNNING时
             * 2.当前状态为TIDYING或TERMINATED时
             * 3.当前状态为SHUTDOWN但工作队列中还有未执行的任务时
             * 4.当前状态为SHUTDOWN且工作队列为空，但是任务数不为0时，中断一个空闲工作线程后退出
             */
            if (isRunning(c) ||
                runStateAtLeast(c, TIDYING) ||
                (runStateOf(c) == SHUTDOWN && ! workQueue.isEmpty()))
                return;
            if (workerCountOf(c) != 0) { // Eligible to terminate
                interruptIdleWorkers(ONLY_ONE);
                return;
            }

            // 全局锁
            final ReentrantLock mainLock = this.mainLock;
            mainLock.lock(); // 获取锁
            try {
                if (ctl.compareAndSet(c, ctlOf(TIDYING, 0))) {// CAS方式尝试更新当前状态为TIDYING
                    try {
                        terminated();// 调用terminated方法，默认该方法为空，由子类实现
                    } finally {
                        ctl.set(ctlOf(TERMINATED, 0));// 设置状态为TERMINATED
                        termination.signalAll();// 唤醒所有termination条件上等待的线程
                    }
                    return;
                }
            } finally {
                mainLock.unlock();// 释放锁
            }
            // 如果走到这里说明CAS更新状态失败，可能是其他线程修改了状态，此时需要重新进入下一次循环
        }
    }

    /*
     * Methods for controlling interrupts to worker threads.
     */

    /**
     * If there is a security manager, makes sure caller has
     * permission to shut down threads in general (see shutdownPerm).
     * If this passes, additionally makes sure the caller is allowed
     * to interrupt each worker thread. This might not be true even if
     * first check passed, if the SecurityManager treats some threads
     * specially.
     */
    private void checkShutdownAccess() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(shutdownPerm);
            final ReentrantLock mainLock = this.mainLock;
            mainLock.lock();
            try {
                for (Worker w : workers)
                    security.checkAccess(w.thread);
            } finally {
                mainLock.unlock();
            }
        }
    }

    /**
     * Interrupts all threads, even if active. Ignores SecurityExceptions
     * (in which case some threads may remain uninterrupted).
     */
    private void interruptWorkers() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            for (Worker w : workers)
                w.interruptIfStarted();
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 中断正在等待任务的空闲工作线程
     */
    private void interruptIdleWorkers(boolean onlyOne) {
    	// 全局可重入锁
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock(); //获取锁
        try {
        	// 遍历工作集中所有工作线程
            for (Worker w : workers) {
                Thread t = w.thread;// 获取工作线程
                // 当前工作线程没有被中断且尝试获取独占线程锁成功
                // 如果独占锁获取失败可能有两种情况
                // 1.worker初始化时状态为-1，不可中断
                // 2.worker当前任务正在执行中，状态为1表示已被独占，不可中断
                if (!t.isInterrupted() && w.tryLock()) {
                    try {
                        t.interrupt(); //中断当前工作线程
                    } catch (SecurityException ignore) {
                    } finally {//释放独占线程锁
                        w.unlock();
                    }
                }
                if (onlyOne) // onlyOne==true时只尝试中断第一个工作线程
                    break;
            }
        } finally {
            mainLock.unlock();// 释放锁
        }
    }

    /**
     * 中断所有空闲的工作线程
     */
    private void interruptIdleWorkers() {
        interruptIdleWorkers(false);
    }

    private static final boolean ONLY_ONE = true;

    /*
     * Misc utilities, most of which are also exported to
     * ScheduledThreadPoolExecutor
     */

    /**
     * Invokes the rejected execution handler for the given command.
     * Package-protected for use by ScheduledThreadPoolExecutor.
     */
    final void reject(Runnable command) {
    	// TODO 本地类将此处注释
        //handler.rejectedExecution(command, this);
    }

    /**
     * Performs any further cleanup following run state transition on
     * invocation of shutdown.  A no-op here, but used by
     * ScheduledThreadPoolExecutor to cancel delayed tasks.
     */
    void onShutdown() {
    }

    /**
     * State check needed by ScheduledThreadPoolExecutor to
     * enable running tasks during shutdown.
     *
     * @param shutdownOK true if should return true if SHUTDOWN
     */
    final boolean isRunningOrShutdown(boolean shutdownOK) {
        int rs = runStateOf(ctl.get());
        return rs == RUNNING || (rs == SHUTDOWN && shutdownOK);
    }

    /**
     * Drains the task queue into a new list, normally using
     * drainTo. But if the queue is a DelayQueue or any other kind of
     * queue for which poll or drainTo may fail to remove some
     * elements, it deletes them one by one.
     */
    private List<Runnable> drainQueue() {
        BlockingQueue<Runnable> q = workQueue;
        ArrayList<Runnable> taskList = new ArrayList<Runnable>();
        q.drainTo(taskList);
        if (!q.isEmpty()) {
            for (Runnable r : q.toArray(new Runnable[0])) {
                if (q.remove(r))
                    taskList.add(r);
            }
        }
        return taskList;
    }

    /*
     * Methods for creating, running and cleaning up after workers
     */
    
    /**
     * 添加一个worker,worker包含一个线程和一个任务，由这个线程来执行该任务
     */
    private boolean addWorker(Runnable firstTask, boolean core) {
    	// 第一步，CAS操作更新当前线程数量
        retry:
        for (;;) { // 外层无限循环
        	// 获取线程池控制状态
            int c = ctl.get();
            int rs = runStateOf(c);

            /*
             * 如果线程池停止或有资格关闭，则此方法返回false
             * 如果线程工厂被询问时未能创建线程，则此方法返回false
             * 包含下列五种情况：
             * 1.状态为RUNNABLE，校验通过
             * 2.状态为STOP或TIDYING或TERMINATED时，返回false
             * (STOP、TIDYING、TERMINATED：已经停止进入最后清理终止，不接受新任务且不处理任务队列中的任务)
             * 3.状态为SHUTDOWN且提交的任务不为空，返回false
             * (SHUTDOWN：不接受任务但是处理队列任务，因此任务不为空返回false)
             * 4.状态为SHUTDOWN，提交的任务为空，并且任务队列为空，返回false
             * (状态为SHUTDOWN、提交的任务为空、任务队列为空，则线程池有资格关闭，直接返回false)
             * 5.状态为SHUTDOWN，提交的任务为空，并且任务队列不为空，通过校验
             * (状态为SHUTDOWN时不处理新提交的任务但是处理任务队列中任务)
             */
            if (rs >= SHUTDOWN &&
                ! (rs == SHUTDOWN && 
                   firstTask == null && 
                   ! workQueue.isEmpty())) 
                return false;

            for (;;) {// 内层无限循环
                int wc = workerCountOf(c);// 获取有效线程数
                // 校验有效的线程数是否超过阈值
                if (wc >= CAPACITY ||
                    wc >= (core ? corePoolSize : maximumPoolSize))
                    return false;
                // 使用CAS将workerCount+1, 修改成功则跳出循环，否则进入下面的状态判断
                if (compareAndIncrementWorkerCount(c))
                    break retry;
                c = ctl.get();  // 重新读取ctl
                // 如果当前状态与上次获取的状态不一致，则说明有其它线程更新了状态，跳转到外层循环重新开始
                if (runStateOf(c) != rs)
                    continue retry;
                // 走到这里说明compareAndIncrementWorkerCount(c)执行失败了
                // 重试内部循环（状态没变，则继续内部循环，尝试使用CAS修改workerCount）
            }
        }

    	// 第二步，创建worker
        boolean workerStarted = false;// 任务启动标识
        boolean workerAdded = false; // 任务添加标识
        Worker w = null;
        try {
        	// 初始化任务
            w = new Worker(firstTask);
            //获取任务线程
            final Thread t = w.thread;
            if (t != null) {// 线程不为null
            	// 线程池全局锁
                final ReentrantLock mainLock = this.mainLock;
                mainLock.lock(); // 获取锁
                try {
                    // Recheck while holding lock.
                    // Back out on ThreadFactory failure or if
                    // shut down before lock acquired.
                    int rs = runStateOf(ctl.get()); // 获取线程池控制状态

                    // 如果当前的运行状态为RUNNING，
                    // 或者当前的运行状态为SHUTDOWN并且firstTask为空，则通过校验
                    if (rs < SHUTDOWN ||
                        (rs == SHUTDOWN && firstTask == null)) {
                    	// 校验线程是否可启动,如果不能启动抛出异常
                        if (t.isAlive()) // precheck that t is startable
                            throw new IllegalThreadStateException();
                        workers.add(w);// 添加任务至核心池
                        // 重新设置largestPoolSize
                        int s = workers.size();
                        if (s > largestPoolSize)
                            largestPoolSize = s;
                        workerAdded = true;// 设置任务添加标识为true
                    }
                } finally {// 释放锁
                    mainLock.unlock();
                }
                if (workerAdded) { // 如果任务已添加
                    t.start(); // 启动任务线程
                    workerStarted = true; // 设置任务启动标识为true
                }
            }
        } finally {
            if (! workerStarted) // 如果任务启动失败
                addWorkerFailed(w); // 则进行回滚, 移除之前添加的Worker
        }
        return workerStarted; // 返回任务启动状态
    }

    /**
     * Rolls back the worker thread creation.
     * - removes worker from workers, if present
     * - decrements worker count
     * - rechecks for termination, in case the existence of this
     *   worker was holding up termination
     */
    private void addWorkerFailed(Worker w) {
    	// 线程池全局锁
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock(); //获取锁
        try {
        	// 将任务从工作线程集合中移除
            if (w != null)
                workers.remove(w);
            // 线程数-1 CAS操作
            decrementWorkerCount();
            // 有worker线程移除，可能是最后一个线程退出需要尝试终止线程池
            tryTerminate();
        } finally {//释放锁
            mainLock.unlock();
        }
    }

    /**
     * 根据是否中断了空闲线程来确定是否减少workerCount的值，并且将worker从workers集合中移除并且会尝试终止线程池
     */
    private void processWorkerExit(Worker w, boolean completedAbruptly) {
        if (completedAbruptly) // 如果被中断，则需要减少workCount
            decrementWorkerCount();// 任务数-1

        // 线程池全局可重入锁
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock(); // 获取锁
        try {
        	// 累加总的完成任务数
            completedTaskCount += w.completedTasks;
            workers.remove(w);// 将当前工作线程从工作集移除
        } finally {
            mainLock.unlock(); //释放锁
        }
        // 调用最后清理工作，尝试终止线程池
        tryTerminate();

        int c = ctl.get();// 获取当前ctl
        if (runStateLessThan(c, STOP)) { // 小于STOP的运行状态
            if (!completedAbruptly) {
                int min = allowCoreThreadTimeOut ? 0 : corePoolSize;
                if (min == 0 && ! workQueue.isEmpty())// 允许核心超时并且workQueue任务队列不为空
                    min = 1;
                if (workerCountOf(c) >= min) // workerCount大于等于min
                	// 直接返回
                    return; // replacement not needed
            }
            // 添加worker
            addWorker(null, false);
        }
    }

    /**
     * 从任务队列中取任务，如果队列为空则根据设置阻塞等待或这超时等待
     * 
     * 下面为返回null的场景：
     * 1.任务数大于最大任务数
     * 2.线程池已经停止(状态为STOP或TIDYING或TERMINATED)
     * 3.线程池状态为SHUTDOWN且任务队列为空
     * 4.等待一个任务时间超过了keepAliveTime的时间
     */
    private Runnable getTask() {
        boolean timedOut = false; // poll()方法取任务是否超时

        for (;;) {
        	// 获取有效状态
            int c = ctl.get();
            int rs = runStateOf(c);

            // Check if queue empty only if necessary.
            // 如果当前线程池状态已经停止或者状态SHUTDOWN并且任务队列为空时返回null
            // 1.状态为STOP、TIDYING、TERMINATED时，线程池已经停止进入最后清理工作，不在接受新任务，不在处理任务队列中的任务
            // 2.状态为SHUTDOWN时，不在接受新任务但是会处理任务队列中的任务，当任务队列为空时不在等待新任务
            // 上面两种场景线程池有资格关闭
            if (rs >= SHUTDOWN && (rs >= STOP || workQueue.isEmpty())) {
                decrementWorkerCount(); //CAS 线程数-1
                return null;
            }

            int wc = workerCountOf(c); // 获取任务数

            // 空闲线程是否在keepAliveTime的时间内被销毁，只在任务数大于核心池大小或者allowCoreThreadTimeOut==true时有存活时间限制
            // 默认情况下keepAliveTime只在线程数大于corePoolSize时才起作用，allowCoreThreadTimeOut==true时，在线程池中的线程数不大于corePoolSize时，该参数也会起作用
            boolean timed = allowCoreThreadTimeOut || wc > corePoolSize;

            // 当任务队列为空且等待时间已经超过keepAliveTime的时间，或任务数大于最大任务数限制时返回null
            if ((wc > maximumPoolSize || (timed && timedOut))
                && (wc > 1 || workQueue.isEmpty())) {
            	// CAS操作线程数-1
            	// 如果线程数更新成功则返回null，否则说明有其它线程改变ctl，需要重新循环当前逻辑
                if (compareAndDecrementWorkerCount(c)) 
                    return null; 
                continue;
            }

            try {
            	// 从任务队列中取出一个任务
            	// 如果空闲线程有存活时间限制，则使用poll方法获取任务，超出keepAliveTime的时间则返回null
            	// 否则使用take方法，直到可以取出任务或被中断
                Runnable r = timed ?
                    workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS) :
                    workQueue.take();
                if (r != null)
                    return r; // 成功取出了任务直接返回
                timedOut = true; // 取任务超时了，进入下一次循环
            } catch (InterruptedException retry) {
                timedOut = false; // 线程被中断 , 进入下一次循环
            }
        }
    }

    /**
     * 循环执行任务
     * 首先执行给定的任务，当给定任务执行完成后会不断从任务队列中取任务执行
     * 直到任务队列为空(即任务全部完成)，或调用了线程池的shutDown()或shutDownNow()方法
     */
    final void runWorker(Worker w) {
    	// 获取当前工作线程
        Thread wt = Thread.currentThread();
        // 获取worker中的初始任务
        Runnable task = w.firstTask;
        w.firstTask = null;
        w.unlock(); // allow interrupts
        boolean completedAbruptly = true;
        try {
            while (task != null || (task = getTask()) != null) {// 如果worker中的初始任务为null则从任务队列中取出一个任务
                w.lock(); // 获取worker的锁
                /*
                 * 检查线程池状态，如果状态为>=STOP(STOP之后在执行任务)，则中断当前任务线程
                 * 如果当前线程已被中断则检查线程池状态是否>=STOP
                 * 最后如果当前线程未被中断则中断当前任务线程
                 */
                if ((runStateAtLeast(ctl.get(), STOP) ||
                     (Thread.interrupted() &&
                      runStateAtLeast(ctl.get(), STOP))) &&
                    !wt.isInterrupted())
                    wt.interrupt();
                try {
                	//执行run之前调用回调函数，空方法留给子类实现
                    beforeExecute(wt, task);
                    Throwable thrown = null;
                    try {
                        task.run();// 执行任务，通过try-catch来保证异常不会影响线程池本身的功能
                    } catch (RuntimeException x) {
                        thrown = x; throw x;
                    } catch (Error x) {
                        thrown = x; throw x;
                    } catch (Throwable x) {
                        thrown = x; throw new Error(x);
                    } finally {// run执行之后调用的回调函数，空方法留给子类实现
                        afterExecute(task, thrown);
                    }
                } finally {
                    task = null;
                    w.completedTasks++;// 已完成任务量统计
                    w.unlock();
                }
            }
            // 如果执行到这里代表非核心线程在keepAliveTime内无法获取任务而退出
            completedAbruptly = false;
        } finally {
        	/**
             * 从上面可以看出如果实际业务(外部提交的Runnable)出现异常会导致当前worker终止
             * completedAbruptly 此时为true意味着worker是突然完成，不是正常退出
             */
            processWorkerExit(w, completedAbruptly);// 执行worker退出收尾工作
        }
    }

    // Public constructors and methods

    /**
     * Creates a new {@code ThreadPoolExecutor} with the given initial
     * parameters and default thread factory and rejected execution handler.
     * It may be more convenient to use one of the {@link Executors} factory
     * methods instead of this general purpose constructor.
     *
     * @param corePoolSize the number of threads to keep in the pool, even
     *        if they are idle, unless {@code allowCoreThreadTimeOut} is set
     * @param maximumPoolSize the maximum number of threads to allow in the
     *        pool
     * @param keepAliveTime when the number of threads is greater than
     *        the core, this is the maximum time that excess idle threads
     *        will wait for new tasks before terminating.
     * @param unit the time unit for the {@code keepAliveTime} argument
     * @param workQueue the queue to use for holding tasks before they are
     *        executed.  This queue will hold only the {@code Runnable}
     *        tasks submitted by the {@code execute} method.
     * @throws IllegalArgumentException if one of the following holds:<br>
     *         {@code corePoolSize < 0}<br>
     *         {@code keepAliveTime < 0}<br>
     *         {@code maximumPoolSize <= 0}<br>
     *         {@code maximumPoolSize < corePoolSize}
     * @throws NullPointerException if {@code workQueue} is null
     */
    public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
             Executors.defaultThreadFactory(), defaultHandler);
    }

    /**
     * Creates a new {@code ThreadPoolExecutor} with the given initial
     * parameters and default rejected execution handler.
     *
     * @param corePoolSize the number of threads to keep in the pool, even
     *        if they are idle, unless {@code allowCoreThreadTimeOut} is set
     * @param maximumPoolSize the maximum number of threads to allow in the
     *        pool
     * @param keepAliveTime when the number of threads is greater than
     *        the core, this is the maximum time that excess idle threads
     *        will wait for new tasks before terminating.
     * @param unit the time unit for the {@code keepAliveTime} argument
     * @param workQueue the queue to use for holding tasks before they are
     *        executed.  This queue will hold only the {@code Runnable}
     *        tasks submitted by the {@code execute} method.
     * @param threadFactory the factory to use when the executor
     *        creates a new thread
     * @throws IllegalArgumentException if one of the following holds:<br>
     *         {@code corePoolSize < 0}<br>
     *         {@code keepAliveTime < 0}<br>
     *         {@code maximumPoolSize <= 0}<br>
     *         {@code maximumPoolSize < corePoolSize}
     * @throws NullPointerException if {@code workQueue}
     *         or {@code threadFactory} is null
     */
    public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue,
                              ThreadFactory threadFactory) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
             threadFactory, defaultHandler);
    }

    /**
     * Creates a new {@code ThreadPoolExecutor} with the given initial
     * parameters and default thread factory.
     *
     * @param corePoolSize the number of threads to keep in the pool, even
     *        if they are idle, unless {@code allowCoreThreadTimeOut} is set
     * @param maximumPoolSize the maximum number of threads to allow in the
     *        pool
     * @param keepAliveTime when the number of threads is greater than
     *        the core, this is the maximum time that excess idle threads
     *        will wait for new tasks before terminating.
     * @param unit the time unit for the {@code keepAliveTime} argument
     * @param workQueue the queue to use for holding tasks before they are
     *        executed.  This queue will hold only the {@code Runnable}
     *        tasks submitted by the {@code execute} method.
     * @param handler the handler to use when execution is blocked
     *        because the thread bounds and queue capacities are reached
     * @throws IllegalArgumentException if one of the following holds:<br>
     *         {@code corePoolSize < 0}<br>
     *         {@code keepAliveTime < 0}<br>
     *         {@code maximumPoolSize <= 0}<br>
     *         {@code maximumPoolSize < corePoolSize}
     * @throws NullPointerException if {@code workQueue}
     *         or {@code handler} is null
     */
    public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue,
                              RejectedExecutionHandler handler) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
             Executors.defaultThreadFactory(), handler);
    }

	/**
	 * 根据给定参数创建一个ThreadPoolExecutor
	 * 
	 * @param corePoolSize
	 *            线程池中的核心线程数
	 * 
	 *            默认情况下创建了线程池后，线程池中的线程数为0，当提交一个任务时，
	 *            线程池创建一个新线程执行任务，直到当前线程数等于corePoolSize， 即使有其他空闲线程能够执行新来的任务，也会继续创建线程；
	 * 
	 *            如果当前线程数为corePoolSize，继续提交的任务被放入任务队列中，
	 *            等待被执行；如果调用了线程池的prestartAllCoreThreads()方法， 线程池会提取创建并启动所有核心线程。
	 * 
	 * @param maximumPoolSize
	 *            线程池中允许的最大线程数
	 * 
	 *            如果当前任务队列已满，且继续提交任务，则创建新的线程执行任务， 前提是当前线程数小于maximumPoolSize；
	 * 
	 *            当任务队列是无界队列，则maximumPoolSize不起作用， 因为无法提交至核心线程池的线程会不断的放入任务队列。
	 * 
	 * @param keepAliveTime
	 *            空闲线程的存活时间
	 * 
	 *            即当线程没有任务执行时，该线程继续存活的时间；
	 * 
	 *            默认情况下，该参数只在线程数大于corePoolSize时才起作用，超过这个时间的空闲线程将被终止，直到线程数不大于corePoolSize；
	 * 
	 *            如果调用了allowCoreThreadTimeOut(boolean)方法，在线程池中的线程数不大于corePoolSize时，该参数也会起作用，直到线程池中的线程数为0。
	 * @param unit
	 *            keepAliveTime的时间单位
	 * @param workQueue
	 *            任务队列
	 * 
	 *            添加策略： 
	 *            		正在执行的线程小于corePoolSize，创新新线程；
	 *            		正在执行的线程大于等于corePoolSize，把任务添加到任务队列；
	 *            		任务队列满了，且正在执行的线程小于maximumPoolSize，创建新线程； 
	 *            		否则拒绝任务；
	 * 
	 *            任务队列类型： 
	 *            		ArrayBlockingQueue 基于数组结构的有界任务队列，按照FIFO排序任务；
	 *            		LinkedBlockingQueue 基于单向链表结构的可选是否有界的任务队列，按照FIFO排序任务，吞吐量通常要高于ArrayBlockingQueue；
	 *            		SynchronousQueue 一个不存储元素的任务队列，每个入队操作必须等到另一个线程调用出队操作，否则入队操作一直处于阻塞状态，吞吐量通常要高于LinkedBlockingQueue；
	 *            		PriorityBlockingQueue 一个基于数组结构且具有优先级的无界任务队列；
	 * @param threadFactory
	 *            创建线程的工厂 
	 *            
	 *            通过自定义线程工厂可以给每个新建的线程设置一个具有识别度的线程名称，默认为DefaultThreadFactory。
	 * @param handler
	 *            线程池的饱和策略 
	 *            
	 *            当任务队列满了，且没有空闲的工作线程，如果继续提交任务，采取的处理策略；
	 * 
	 *            线程池提供4中策略： 
	 *            		AbortPolicy 直接抛出异常，默认策略； 
	 *            		CallerRunsPolicy 用调用者所在的线程来执行任务； 
	 *            		DiscardOldestPolicy 丢弃任务队列中最靠前的任务，并执行当前任务；
	 *            		DiscardPolicy 直接丢弃任务； 
	 *            		还可以实现RejectedExecutionHandler接口，自定义饱和策略；
	 */
    public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue,
                              ThreadFactory threadFactory,
                              RejectedExecutionHandler handler) {
        if (corePoolSize < 0 ||
            maximumPoolSize <= 0 ||
            maximumPoolSize < corePoolSize ||
            keepAliveTime < 0)
            throw new IllegalArgumentException();
        if (workQueue == null || threadFactory == null || handler == null)
            throw new NullPointerException();
        this.acc = System.getSecurityManager() == null ?
                null :
                AccessController.getContext();
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.workQueue = workQueue;
        this.keepAliveTime = unit.toNanos(keepAliveTime);
        this.threadFactory = threadFactory;
        this.handler = handler;
    }

    /**
     * Executes the given task sometime in the future.  The task
     * may execute in a new thread or in an existing pooled thread.
     *
     * If the task cannot be submitted for execution, either because this
     * executor has been shutdown or because its capacity has been reached,
     * the task is handled by the current {@code RejectedExecutionHandler}.
     *
     * @param command the task to execute
     * @throws RejectedExecutionException at discretion of
     *         {@code RejectedExecutionHandler}, if the task
     *         cannot be accepted for execution
     * @throws NullPointerException if {@code command} is null
     */
    public void execute(Runnable command) {
    	// NULL检查，线程池不允许提交NULL任务
        if (command == null)
            throw new NullPointerException();
        /*
         * Proceed in 3 steps:
         *
         * 1. If fewer than corePoolSize threads are running, try to
         * start a new thread with the given command as its first
         * task.  The call to addWorker atomically checks runState and
         * workerCount, and so prevents false alarms that would add
         * threads when it shouldn't, by returning false.
         *
         * 2. If a task can be successfully queued, then we still need
         * to double-check whether we should have added a thread
         * (because existing ones died since last checking) or that
         * the pool shut down since entry into this method. So we
         * recheck state and if necessary roll back the enqueuing if
         * stopped, or start a new thread if there are none.
         *
         * 3. If we cannot queue task, then we try to add a new
         * thread.  If it fails, we know we are shut down or saturated
         * and so reject the task.
         */
        int c = ctl.get();// 获取当前的clt，AtomicInteger类型保证线程安全
        if (workerCountOf(c) < corePoolSize) {//如果当前运行的线程数小于核心线程数
            if (addWorker(command, true))//如果添加核心线程数成功则方法返回
                return;
            c = ctl.get();//执行到这里必定是添加核心线程失败，重新读取最新的clt
        }
        
        /**
         * 这里分析一下添加核心态worker失败的几种场景：
         * 1、线程池为shutdown以上的状态
         * 2、当前线程池中运行的worker的数量超过其本身最大限制（2^29  -1 ）
         * 3、当前线程池中运行的worker的数量超过corePoolSize
         */
        // 如果线程池处于running状态，则将当前提交的任务提交到内部的阻塞队列进行排队等待worker处理
        if (isRunning(c) && workQueue.offer(command)) {
            int recheck = ctl.get();
            /**
             * double check是否线程池仍在运行中
             * 如果线程池不在running状态则将刚才进行排队的任务移除，并拒绝此次提交的任务
             * 如果此时在线程池中运行的worker数量减少到0（corePoolSize为0的线程池在并发的情况下会出现此场景）
             * 则添加一个不携带任何任务的非核心态的worker去处理刚才排队成功的任务
             */
            if (! isRunning(recheck) && remove(command))
                reject(command);
            else if (workerCountOf(recheck) == 0)
                addWorker(null, false);
        }
        else if (!addWorker(command, false))//如果排队失败（有界的阻塞队列）则添加一个非核心态的worker
        	//添加失败：当前运行的worker数量超过maximumPoolSize或者本身最大的限制；线程池状态在shutdown以上
            reject(command);
    }

    /**
     * 关闭线程池，线程状态置为SHUTDOWN，不在接受新任务，之前提交的任务将会被执行。
     * 关闭之后调用tryTerminate方法进行最后的清理工作。
     */
    public void shutdown() {
    	// 全局可重入锁
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock(); // 利用排它锁进行上锁，保证只有一个线程执行关闭流程
        try {
        	// 安全检查
            checkShutdownAccess();
            // 内部通过自旋+CAS修改线程池状态为SHUTDOWN
            advanceRunState(SHUTDOWN);
            // 遍历所有的worker，进行线程中断通知
            interruptIdleWorkers();
            onShutdown(); // ScheduledThreadPoolExecutor的回调函数，空方法需要子类实现
        } finally {
            mainLock.unlock();// 释放锁
        }
        // 进行最后的整理工作
        tryTerminate();
    }

    /**
     * 尝试停止所有正在执行的任务，停止处理任务队列，并返回任务队列中等待执行的任务列表
     */
    public List<Runnable> shutdownNow() {
        List<Runnable> tasks;
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
        	// 安全检查
            checkShutdownAccess();
            // 内部通过自旋+CAS修改线程池状态为STOP
            advanceRunState(STOP);
            // 遍历所有的worker，进行线程中断通知
            interruptWorkers();
            // 取出任务队列中所有任务
            tasks = drainQueue();
        } finally {
            mainLock.unlock();
        }
        // 执行最后清理
        tryTerminate();
        return tasks;
    }

    public boolean isShutdown() {
        return ! isRunning(ctl.get());
    }

    /**
     * Returns true if this executor is in the process of terminating
     * after {@link #shutdown} or {@link #shutdownNow} but has not
     * completely terminated.  This method may be useful for
     * debugging. A return of {@code true} reported a sufficient
     * period after shutdown may indicate that submitted tasks have
     * ignored or suppressed interruption, causing this executor not
     * to properly terminate.
     *
     * @return {@code true} if terminating but not yet terminated
     */
    public boolean isTerminating() {
        int c = ctl.get();
        return ! isRunning(c) && runStateLessThan(c, TERMINATED);
    }

    public boolean isTerminated() {
        return runStateAtLeast(ctl.get(), TERMINATED);
    }

    public boolean awaitTermination(long timeout, TimeUnit unit)
        throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            for (;;) {
                if (runStateAtLeast(ctl.get(), TERMINATED))
                    return true;
                if (nanos <= 0)
                    return false;
                nanos = termination.awaitNanos(nanos);
            }
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * Invokes {@code shutdown} when this executor is no longer
     * referenced and it has no threads.
     */
    protected void finalize() {
        SecurityManager sm = System.getSecurityManager();
        if (sm == null || acc == null) {
            shutdown();
        } else {
            PrivilegedAction<Void> pa = () -> { shutdown(); return null; };
            AccessController.doPrivileged(pa, acc);
        }
    }

    /**
     * Sets the thread factory used to create new threads.
     *
     * @param threadFactory the new thread factory
     * @throws NullPointerException if threadFactory is null
     * @see #getThreadFactory
     */
    public void setThreadFactory(ThreadFactory threadFactory) {
        if (threadFactory == null)
            throw new NullPointerException();
        this.threadFactory = threadFactory;
    }

    /**
     * Returns the thread factory used to create new threads.
     *
     * @return the current thread factory
     * @see #setThreadFactory(ThreadFactory)
     */
    public ThreadFactory getThreadFactory() {
        return threadFactory;
    }

    /**
     * Sets a new handler for unexecutable tasks.
     *
     * @param handler the new handler
     * @throws NullPointerException if handler is null
     * @see #getRejectedExecutionHandler
     */
    public void setRejectedExecutionHandler(RejectedExecutionHandler handler) {
        if (handler == null)
            throw new NullPointerException();
        this.handler = handler;
    }

    /**
     * Returns the current handler for unexecutable tasks.
     *
     * @return the current handler
     * @see #setRejectedExecutionHandler(RejectedExecutionHandler)
     */
    public RejectedExecutionHandler getRejectedExecutionHandler() {
        return handler;
    }

    /**
     * Sets the core number of threads.  This overrides any value set
     * in the constructor.  If the new value is smaller than the
     * current value, excess existing threads will be terminated when
     * they next become idle.  If larger, new threads will, if needed,
     * be started to execute any queued tasks.
     *
     * @param corePoolSize the new core size
     * @throws IllegalArgumentException if {@code corePoolSize < 0}
     * @see #getCorePoolSize
     */
    public void setCorePoolSize(int corePoolSize) {
        if (corePoolSize < 0)
            throw new IllegalArgumentException();
        int delta = corePoolSize - this.corePoolSize;
        this.corePoolSize = corePoolSize;
        if (workerCountOf(ctl.get()) > corePoolSize)
            interruptIdleWorkers();
        else if (delta > 0) {
            // We don't really know how many new threads are "needed".
            // As a heuristic, prestart enough new workers (up to new
            // core size) to handle the current number of tasks in
            // queue, but stop if queue becomes empty while doing so.
            int k = Math.min(delta, workQueue.size());
            while (k-- > 0 && addWorker(null, true)) {
                if (workQueue.isEmpty())
                    break;
            }
        }
    }

    /**
     * Returns the core number of threads.
     *
     * @return the core number of threads
     * @see #setCorePoolSize
     */
    public int getCorePoolSize() {
        return corePoolSize;
    }

    /**
     * Starts a core thread, causing it to idly wait for work. This
     * overrides the default policy of starting core threads only when
     * new tasks are executed. This method will return {@code false}
     * if all core threads have already been started.
     *
     * @return {@code true} if a thread was started
     */
    public boolean prestartCoreThread() {
        return workerCountOf(ctl.get()) < corePoolSize &&
            addWorker(null, true);
    }

    /**
     * Same as prestartCoreThread except arranges that at least one
     * thread is started even if corePoolSize is 0.
     */
    void ensurePrestart() {
        int wc = workerCountOf(ctl.get());
        if (wc < corePoolSize)
            addWorker(null, true);
        else if (wc == 0)
            addWorker(null, false);
    }

    /**
     * Starts all core threads, causing them to idly wait for work. This
     * overrides the default policy of starting core threads only when
     * new tasks are executed.
     *
     * @return the number of threads started
     */
    public int prestartAllCoreThreads() {
        int n = 0;
        while (addWorker(null, true))
            ++n;
        return n;
    }

    /**
     * Returns true if this pool allows core threads to time out and
     * terminate if no tasks arrive within the keepAlive time, being
     * replaced if needed when new tasks arrive. When true, the same
     * keep-alive policy applying to non-core threads applies also to
     * core threads. When false (the default), core threads are never
     * terminated due to lack of incoming tasks.
     *
     * @return {@code true} if core threads are allowed to time out,
     *         else {@code false}
     *
     * @since 1.6
     */
    public boolean allowsCoreThreadTimeOut() {
        return allowCoreThreadTimeOut;
    }

    /**
     * Sets the policy governing whether core threads may time out and
     * terminate if no tasks arrive within the keep-alive time, being
     * replaced if needed when new tasks arrive. When false, core
     * threads are never terminated due to lack of incoming
     * tasks. When true, the same keep-alive policy applying to
     * non-core threads applies also to core threads. To avoid
     * continual thread replacement, the keep-alive time must be
     * greater than zero when setting {@code true}. This method
     * should in general be called before the pool is actively used.
     *
     * @param value {@code true} if should time out, else {@code false}
     * @throws IllegalArgumentException if value is {@code true}
     *         and the current keep-alive time is not greater than zero
     *
     * @since 1.6
     */
    public void allowCoreThreadTimeOut(boolean value) {
        if (value && keepAliveTime <= 0)
            throw new IllegalArgumentException("Core threads must have nonzero keep alive times");
        if (value != allowCoreThreadTimeOut) {
            allowCoreThreadTimeOut = value;
            if (value)
                interruptIdleWorkers();
        }
    }

    /**
     * Sets the maximum allowed number of threads. This overrides any
     * value set in the constructor. If the new value is smaller than
     * the current value, excess existing threads will be
     * terminated when they next become idle.
     *
     * @param maximumPoolSize the new maximum
     * @throws IllegalArgumentException if the new maximum is
     *         less than or equal to zero, or
     *         less than the {@linkplain #getCorePoolSize core pool size}
     * @see #getMaximumPoolSize
     */
    public void setMaximumPoolSize(int maximumPoolSize) {
        if (maximumPoolSize <= 0 || maximumPoolSize < corePoolSize)
            throw new IllegalArgumentException();
        this.maximumPoolSize = maximumPoolSize;
        if (workerCountOf(ctl.get()) > maximumPoolSize)
            interruptIdleWorkers();
    }

    /**
     * Returns the maximum allowed number of threads.
     *
     * @return the maximum allowed number of threads
     * @see #setMaximumPoolSize
     */
    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    /**
     * Sets the time limit for which threads may remain idle before
     * being terminated.  If there are more than the core number of
     * threads currently in the pool, after waiting this amount of
     * time without processing a task, excess threads will be
     * terminated.  This overrides any value set in the constructor.
     *
     * @param time the time to wait.  A time value of zero will cause
     *        excess threads to terminate immediately after executing tasks.
     * @param unit the time unit of the {@code time} argument
     * @throws IllegalArgumentException if {@code time} less than zero or
     *         if {@code time} is zero and {@code allowsCoreThreadTimeOut}
     * @see #getKeepAliveTime(TimeUnit)
     */
    public void setKeepAliveTime(long time, TimeUnit unit) {
        if (time < 0)
            throw new IllegalArgumentException();
        if (time == 0 && allowsCoreThreadTimeOut())
            throw new IllegalArgumentException("Core threads must have nonzero keep alive times");
        long keepAliveTime = unit.toNanos(time);
        long delta = keepAliveTime - this.keepAliveTime;
        this.keepAliveTime = keepAliveTime;
        if (delta < 0)
            interruptIdleWorkers();
    }

    /**
     * Returns the thread keep-alive time, which is the amount of time
     * that threads in excess of the core pool size may remain
     * idle before being terminated.
     *
     * @param unit the desired time unit of the result
     * @return the time limit
     * @see #setKeepAliveTime(long, TimeUnit)
     */
    public long getKeepAliveTime(TimeUnit unit) {
        return unit.convert(keepAliveTime, TimeUnit.NANOSECONDS);
    }

    /* User-level queue utilities */

    /**
     * Returns the task queue used by this executor. Access to the
     * task queue is intended primarily for debugging and monitoring.
     * This queue may be in active use.  Retrieving the task queue
     * does not prevent queued tasks from executing.
     *
     * @return the task queue
     */
    public BlockingQueue<Runnable> getQueue() {
        return workQueue;
    }

    /**
     * Removes this task from the executor's internal queue if it is
     * present, thus causing it not to be run if it has not already
     * started.
     *
     * <p>This method may be useful as one part of a cancellation
     * scheme.  It may fail to remove tasks that have been converted
     * into other forms before being placed on the internal queue. For
     * example, a task entered using {@code submit} might be
     * converted into a form that maintains {@code Future} status.
     * However, in such cases, method {@link #purge} may be used to
     * remove those Futures that have been cancelled.
     *
     * @param task the task to remove
     * @return {@code true} if the task was removed
     */
    public boolean remove(Runnable task) {
        boolean removed = workQueue.remove(task);
        tryTerminate(); // In case SHUTDOWN and now empty
        return removed;
    }

    /**
     * Tries to remove from the work queue all {@link Future}
     * tasks that have been cancelled. This method can be useful as a
     * storage reclamation operation, that has no other impact on
     * functionality. Cancelled tasks are never executed, but may
     * accumulate in work queues until worker threads can actively
     * remove them. Invoking this method instead tries to remove them now.
     * However, this method may fail to remove tasks in
     * the presence of interference by other threads.
     */
    public void purge() {
        final BlockingQueue<Runnable> q = workQueue;
        try {
            Iterator<Runnable> it = q.iterator();
            while (it.hasNext()) {
                Runnable r = it.next();
                if (r instanceof Future<?> && ((Future<?>)r).isCancelled())
                    it.remove();
            }
        } catch (ConcurrentModificationException fallThrough) {
            // Take slow path if we encounter interference during traversal.
            // Make copy for traversal and call remove for cancelled entries.
            // The slow path is more likely to be O(N*N).
            for (Object r : q.toArray())
                if (r instanceof Future<?> && ((Future<?>)r).isCancelled())
                    q.remove(r);
        }

        tryTerminate(); // In case SHUTDOWN and now empty
    }

    /* Statistics */

    /**
     * Returns the current number of threads in the pool.
     *
     * @return the number of threads
     */
    public int getPoolSize() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            // Remove rare and surprising possibility of
            // isTerminated() && getPoolSize() > 0
            return runStateAtLeast(ctl.get(), TIDYING) ? 0
                : workers.size();
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * Returns the approximate number of threads that are actively
     * executing tasks.
     *
     * @return the number of threads
     */
    public int getActiveCount() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            int n = 0;
            for (Worker w : workers)
                if (w.isLocked())
                    ++n;
            return n;
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * Returns the largest number of threads that have ever
     * simultaneously been in the pool.
     *
     * @return the number of threads
     */
    public int getLargestPoolSize() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            return largestPoolSize;
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * Returns the approximate total number of tasks that have ever been
     * scheduled for execution. Because the states of tasks and
     * threads may change dynamically during computation, the returned
     * value is only an approximation.
     *
     * @return the number of tasks
     */
    public long getTaskCount() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            long n = completedTaskCount;
            for (Worker w : workers) {
                n += w.completedTasks;
                if (w.isLocked())
                    ++n;
            }
            return n + workQueue.size();
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * Returns the approximate total number of tasks that have
     * completed execution. Because the states of tasks and threads
     * may change dynamically during computation, the returned value
     * is only an approximation, but one that does not ever decrease
     * across successive calls.
     *
     * @return the number of tasks
     */
    public long getCompletedTaskCount() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            long n = completedTaskCount;
            for (Worker w : workers)
                n += w.completedTasks;
            return n;
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * Returns a string identifying this pool, as well as its state,
     * including indications of run state and estimated worker and
     * task counts.
     *
     * @return a string identifying this pool, as well as its state
     */
    public String toString() {
        long ncompleted;
        int nworkers, nactive;
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            ncompleted = completedTaskCount;
            nactive = 0;
            nworkers = workers.size();
            for (Worker w : workers) {
                ncompleted += w.completedTasks;
                if (w.isLocked())
                    ++nactive;
            }
        } finally {
            mainLock.unlock();
        }
        int c = ctl.get();
        String rs = (runStateLessThan(c, SHUTDOWN) ? "Running" :
                     (runStateAtLeast(c, TERMINATED) ? "Terminated" :
                      "Shutting down"));
        return super.toString() +
            "[" + rs +
            ", pool size = " + nworkers +
            ", active threads = " + nactive +
            ", queued tasks = " + workQueue.size() +
            ", completed tasks = " + ncompleted +
            "]";
    }

    /* Extension hooks */

    /**
     * Method invoked prior to executing the given Runnable in the
     * given thread.  This method is invoked by thread {@code t} that
     * will execute task {@code r}, and may be used to re-initialize
     * ThreadLocals, or to perform logging.
     *
     * <p>This implementation does nothing, but may be customized in
     * subclasses. Note: To properly nest multiple overridings, subclasses
     * should generally invoke {@code super.beforeExecute} at the end of
     * this method.
     *
     * @param t the thread that will run task {@code r}
     * @param r the task that will be executed
     */
    protected void beforeExecute(Thread t, Runnable r) { }

    /**
     * Method invoked upon completion of execution of the given Runnable.
     * This method is invoked by the thread that executed the task. If
     * non-null, the Throwable is the uncaught {@code RuntimeException}
     * or {@code Error} that caused execution to terminate abruptly.
     *
     * <p>This implementation does nothing, but may be customized in
     * subclasses. Note: To properly nest multiple overridings, subclasses
     * should generally invoke {@code super.afterExecute} at the
     * beginning of this method.
     *
     * <p><b>Note:</b> When actions are enclosed in tasks (such as
     * {@link FutureTask}) either explicitly or via methods such as
     * {@code submit}, these task objects catch and maintain
     * computational exceptions, and so they do not cause abrupt
     * termination, and the internal exceptions are <em>not</em>
     * passed to this method. If you would like to trap both kinds of
     * failures in this method, you can further probe for such cases,
     * as in this sample subclass that prints either the direct cause
     * or the underlying exception if a task has been aborted:
     *
     *  <pre> {@code
     * class ExtendedExecutor extends ThreadPoolExecutor {
     *   // ...
     *   protected void afterExecute(Runnable r, Throwable t) {
     *     super.afterExecute(r, t);
     *     if (t == null && r instanceof Future<?>) {
     *       try {
     *         Object result = ((Future<?>) r).get();
     *       } catch (CancellationException ce) {
     *           t = ce;
     *       } catch (ExecutionException ee) {
     *           t = ee.getCause();
     *       } catch (InterruptedException ie) {
     *           Thread.currentThread().interrupt(); // ignore/reset
     *       }
     *     }
     *     if (t != null)
     *       System.out.println(t);
     *   }
     * }}</pre>
     *
     * @param r the runnable that has completed
     * @param t the exception that caused termination, or null if
     * execution completed normally
     */
    protected void afterExecute(Runnable r, Throwable t) { }

    /**
     * Method invoked when the Executor has terminated.  Default
     * implementation does nothing. Note: To properly nest multiple
     * overridings, subclasses should generally invoke
     * {@code super.terminated} within this method.
     */
    protected void terminated() { }

    /* Predefined RejectedExecutionHandlers */

    /**
     * A handler for rejected tasks that runs the rejected task
     * directly in the calling thread of the {@code execute} method,
     * unless the executor has been shut down, in which case the task
     * is discarded.
     */
    public static class CallerRunsPolicy implements RejectedExecutionHandler {
        /**
         * Creates a {@code CallerRunsPolicy}.
         */
        public CallerRunsPolicy() { }

        /**
         * Executes task r in the caller's thread, unless the executor
         * has been shut down, in which case the task is discarded.
         *
         * @param r the runnable task requested to be executed
         * @param e the executor attempting to execute this task
         */
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            if (!e.isShutdown()) {
                r.run();
            }
        }

		@Override
		public void rejectedExecution(Runnable r, java.util.concurrent.ThreadPoolExecutor executor) {
			// TODO Auto-generated method stub
			
		}
    }

    /**
     * A handler for rejected tasks that throws a
     * {@code RejectedExecutionException}.
     */
    public static class AbortPolicy implements RejectedExecutionHandler {
        /**
         * Creates an {@code AbortPolicy}.
         */
        public AbortPolicy() { }

        /**
         * Always throws RejectedExecutionException.
         *
         * @param r the runnable task requested to be executed
         * @param e the executor attempting to execute this task
         * @throws RejectedExecutionException always
         */
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            throw new RejectedExecutionException("Task " + r.toString() +
                                                 " rejected from " +
                                                 e.toString());
        }

		@Override
		public void rejectedExecution(Runnable r, java.util.concurrent.ThreadPoolExecutor executor) {
			// TODO Auto-generated method stub
			
		}
    }

    /**
     * A handler for rejected tasks that silently discards the
     * rejected task.
     */
    public static class DiscardPolicy implements RejectedExecutionHandler {
        /**
         * Creates a {@code DiscardPolicy}.
         */
        public DiscardPolicy() { }

        /**
         * Does nothing, which has the effect of discarding task r.
         *
         * @param r the runnable task requested to be executed
         * @param e the executor attempting to execute this task
         */
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        }

		@Override
		public void rejectedExecution(Runnable r, java.util.concurrent.ThreadPoolExecutor executor) {
			// TODO Auto-generated method stub
			
		}
    }

    /**
     * A handler for rejected tasks that discards the oldest unhandled
     * request and then retries {@code execute}, unless the executor
     * is shut down, in which case the task is discarded.
     */
    public static class DiscardOldestPolicy implements RejectedExecutionHandler {
        /**
         * Creates a {@code DiscardOldestPolicy} for the given executor.
         */
        public DiscardOldestPolicy() { }

        /**
         * Obtains and ignores the next task that the executor
         * would otherwise execute, if one is immediately available,
         * and then retries execution of task r, unless the executor
         * is shut down, in which case task r is instead discarded.
         *
         * @param r the runnable task requested to be executed
         * @param e the executor attempting to execute this task
         */
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            if (!e.isShutdown()) {
                e.getQueue().poll();
                e.execute(r);
            }
        }

		@Override
		public void rejectedExecution(Runnable r, java.util.concurrent.ThreadPoolExecutor executor) {
			// TODO Auto-generated method stub
			
		}
    }
}
