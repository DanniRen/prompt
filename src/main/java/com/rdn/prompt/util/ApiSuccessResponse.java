package com.rdn.prompt.util;

public class ApiSuccessResponse<T> extends ApiBaseResponse {

    private static final long serialVersionUID = 1L;

    private T data;

    public ApiSuccessResponse() {
        this(null);
    }

    public ApiSuccessResponse(T data) {
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
