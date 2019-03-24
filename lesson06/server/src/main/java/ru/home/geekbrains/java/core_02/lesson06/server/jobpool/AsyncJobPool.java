package ru.home.geekbrains.java.core_02.lesson06.server.jobpool;

import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;
import java.util.function.Supplier;


// =========================================================================================


/**
 * Async job pool dynamic size (non-blocking)
 * <br>
 */
public class AsyncJobPool<T> extends BaseJobPool {


    //private ThreadPoolExecutor threadPool;

    // On job done handler
    private Consumer<T> callback;



    /**
     * Pool of worker threads
     * @param callback handler onComplete event
     */
    public AsyncJobPool(Consumer<T> callback) {

        final CustomizableThreadFactory threadFactory = new CustomizableThreadFactory();

        threadFactory.setDaemon(true);
        //threadFactory.setThreadGroupName("AsyncPool");
        threadFactory.setThreadNamePrefix("AsyncPool-");

        threadPool = (ThreadPoolExecutor)Executors.newCachedThreadPool(threadFactory);
        this.callback = callback;
    }

    /**
     * Add job to execute
     * <br>
     * @param job Supplier
     */
    public void add(Supplier<T> job) {

        CompletableFuture.supplyAsync(job, threadPool)
                .handle(this::handle)
                .thenAccept(this::callback);
    }

    public void stop() {

        threadPool.shutdownNow();
    }


    // ----------------------------------------------------------------------------------------------------


    /**
     * WorkProcessable.work error handler
     */
    // Never should be called
    private T handle(T result, Throwable e) {

        //System.out.println(e.getMessage());
        return result;
    }


    /**
     * On job done
     * @param msg callback message
     */
    private void callback(T msg) {

        //System.out.println("callback");
        if (callback != null)
            callback.accept(msg);
    }


    public int size() {

        // warn .getActiveCount() not accurate
        return threadPool.getActiveCount();

    }



    // ----------------------------------------------------------------------------------------------------
}
