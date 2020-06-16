package com.easyetz.modules.tools.threads;

import com.easyetz.utils.MyLog;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/*
* Singleton class for default executor supplier
*/
public class ETZExecutor implements RejectedExecutionHandler {
    private static final String TAG = ETZExecutor.class.getName();
    /*
    * Number of cores to decide the number of threads
    */
    public static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    /*
    * thread pool executor for background tasks
    */
    private final ThreadPoolExecutor mForBackgroundTasks;
    /*
    * thread pool executor for light weight background tasks
    */
    private final ThreadPoolExecutor mForLightWeightBackgroundTasks;
    //    /*
//    * thread pool executor for serialized tasks (one at the time)
//    */
//    private final ThreadPoolExecutor mForSerializedTasks;
    /*
    * thread pool executor for main thread tasks
    */
    private final Executor mMainThreadExecutor;
    /*
    * an instance of BRExecutor
    */
    private static ETZExecutor sInstance;

    /*
    * returns the instance of BRExecutor
    */
    public static ETZExecutor getInstance() {
        if (sInstance == null) {
            synchronized (ETZExecutor.class) {
                sInstance = new ETZExecutor();
            }
        }
        return sInstance;

    }


    /*
    * constructor for  BRExecutor
    */
    private ETZExecutor() {

        ThreadFactory backgroundPriorityThreadFactory = new
                PriorityThreadFactory(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        // setting the thread pool executor for mForBackgroundTasks;
        mForBackgroundTasks = new ThreadPoolExecutor(
                NUMBER_OF_CORES * 8,
                NUMBER_OF_CORES * 16,
                30,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                backgroundPriorityThreadFactory,
                this
        );

        // setting the thread pool executor for mForLightWeightBackgroundTasks;
        mForLightWeightBackgroundTasks = new ThreadPoolExecutor(
                NUMBER_OF_CORES * 16,
                NUMBER_OF_CORES * 32,
                20,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                backgroundPriorityThreadFactory,
                this
        );
//        // setting the thread pool executor for mForLightWeightBackgroundTasks;
//        mForSerializedTasks = new ThreadPoolExecutor(
//                1,
//                1,
//                30L,
//                TimeUnit.SECONDS,
//                new LinkedBlockingQueue<Runnable>(),
//                backgroundPriorityThreadFactory,
//                this
//        );

        // setting the thread pool executor for mMainThreadExecutor;
        mMainThreadExecutor = new MainThreadExecutor();
    }

    /*
    * returns the thread pool executor for background task
    */

    public ThreadPoolExecutor forBackgroundTasks() {
        return mForBackgroundTasks;
    }

    /*
    * returns the thread pool executor for light weight background task
    */
    public ThreadPoolExecutor forLightWeightBackgroundTasks() {
        return mForLightWeightBackgroundTasks;
    }

//    /*
//    * returns the thread pool executor for serialized task
//    */
//    public ThreadPoolExecutor forSerializedTasks() {
//        return mForSerializedTasks;
//    }

    /*
    * returns the thread pool executor for main thread task
    */
    public Executor forMainThreadTasks() {
        return mMainThreadExecutor;
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        MyLog.e( "rejectedExecution: ");
    }
}