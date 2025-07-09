package com.rdn.prompt.util;

public class ApiErrorResponse extends ApiBaseResponse {
    private static final long serialVersionUID = 1L;

    private String msg;

    public ApiErrorResponse(Integer code, String msg) {
        this.timestmap = System.currentTimeMillis();
        this.code = code;
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
    public void setMsg(String msg) {
        this.msg = msg;
    }
}
