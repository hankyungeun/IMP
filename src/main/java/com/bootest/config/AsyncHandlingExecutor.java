package com.bootest.config;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.springframework.core.task.AsyncTaskExecutor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AsyncHandlingExecutor implements AsyncTaskExecutor {

    private AsyncTaskExecutor executor;

    public AsyncHandlingExecutor(AsyncTaskExecutor executor) {
        this.executor = executor;
    }

    @Override
    public void execute(Runnable task) {
        executor.execute(task);
    }

    @Override
    public void execute(Runnable task, long startTimeOut) {
        executor.execute(createWrappedRunnable(task), startTimeOut);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return executor.submit(createWrappedRunnable(task));
    }

    @Override
    public <T> Future<T> submit(final Callable<T> task) {
        return executor.submit(createCallable(task));
    }

    private <T> Callable<T> createCallable(final Callable<T> task) {
        return new Callable<T>() {
            @Override
            public T call() throws Exception {
                try {
                    return task.call();
                } catch (Exception e) {
                    handle(e);
                    throw e;
                }
            }
        };
    }

    private Runnable createWrappedRunnable(final Runnable task) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    task.run();
                } catch (Exception e) {
                    handle(e);
                }
            }
        };
    }

    private void handle(Exception e) {
        log.info("Execute Task Request Failed: {}", e.getMessage(), e);
    }

}
