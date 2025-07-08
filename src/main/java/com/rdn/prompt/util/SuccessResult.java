package com.rdn.prompt.util;

public class SuccessResult<T> extends BaseResult{

    private static final long serialVersionUID = 1L;

    private T data;

    public SuccessResult() {
        this(null);
    }

    public SuccessResult(T data) {
        this.timestmap = System.currentTimeMillis();
        this.code = 200;
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
