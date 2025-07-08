package com.rdn.prompt.util;

public class ErrorResult extends BaseResult{
    private static final long serialVersionUID = 1L;

    private String msg;

    public ErrorResult(Integer code, String msg) {
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
