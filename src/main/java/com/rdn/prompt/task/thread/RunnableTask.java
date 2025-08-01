package com.rdn.prompt.task.thread;

public interface RunnableTask extends Runnable {

    /**
     * 任务执行成功后的方法
     */
    void success();


    /**
     * 任务执行失败以后的方法
     */
    void failure(Throwable t);


    /**
     * 任务执行失败以后的fallback机制
     */
    void fallback();

}
