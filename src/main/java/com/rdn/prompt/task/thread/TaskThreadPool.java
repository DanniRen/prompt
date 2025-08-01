package com.rdn.prompt.task.thread;

import com.google.common.util.concurrent.*;
import org.modelmapper.internal.util.Callable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TaskThreadPool {
    private final ListeningExecutorService listeningExecutorService;

    private static final TaskThreadPool INSTANCE = new TaskThreadPool(2,"Task_Thread_%d");

    private List<MainTask> mainTasks;

    private TaskThreadPool(int threadCount, String threadNameFormat) {
        // 设置线程的名称
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat(threadNameFormat) // 设置线程名称格式
                .setDaemon(true) // 设置成守护线程，当主线程退出时自动终止
                .build();
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                threadCount, // 核心线程数
                threadCount, // 最大线程数
                60L, // 线程空闲时间
                TimeUnit.MICROSECONDS, // 时间单位
                new LinkedBlockingQueue<>(512), // 任务队列
                threadFactory, // 线程工厂
                new ThreadPoolExecutor.AbortPolicy()); // 拒绝策略
        threadPoolExecutor.allowCoreThreadTimeOut(true);
        listeningExecutorService = MoreExecutors.listeningDecorator(threadPoolExecutor);
        mainTasks = new LinkedList<>();
    }

    public static TaskThreadPool getInstance() {
        return INSTANCE;
    }

    public <V> void submit(MainTask mainTask) {
        mainTasks.add(mainTask);
        // 使用线程池执行一个任务
        ListenableFuture<V> future = (ListenableFuture<V>) this.listeningExecutorService.submit(mainTask);

        FutureCallback<V> futureCallback = new FutureCallback<>() {
            @Override
            public void onSuccess(Object result) {
                mainTask.success();
                mainTasks.remove(mainTask);
            }

            @Override
            public void onFailure(Throwable t) {
                mainTask.failure(t);
                mainTask.fallback();
                mainTasks.remove(mainTask);
            }
        };

        Futures.addCallback(future, futureCallback, this.listeningExecutorService);
    }
}
