package com.rdn.prompt.task.exception;

public class TaskRunException extends RuntimeException {
    static final long serialVersionUID = 2L;

    public TaskRunException() {
        super();
    }

    public TaskRunException(String message) {
        super(message);
    }

    public TaskRunException(String message, Throwable cause) {
        super(message, cause);
    }
}
