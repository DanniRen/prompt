package com.rdn.prompt.util;

import java.io.Serializable;

/**
 * API的基础响应体
 * @author RDN
 * @since 2025/7/8
 */
public abstract class BaseResult implements Serializable {
    private static final long serialVersionUID = 1L;

    // 当前时间戳
    public Long timestmap;

    // HTTP状态码
    public Integer code;

    /**
     * 创建成功的响应体，无数据
     * @return SuccessResult
     */
    public static BaseResult success() {
        return new SuccessResult();
    }

    /**
     * 创建成功的响应体，有数据
     * @param data 数据体
     * @return SuccessResult
     * @param <T>
     */
    public static <T> BaseResult success(T data) {
        return new SuccessResult<T>(data);
    }

    /**
     * 创建失败的响应体
     * @param code 错误码
     * @param msg 错误信息
     * @return
     */
    public static BaseResult error(Integer code, String msg) {
        return new ErrorResult(code, msg);
    }

    public Integer getCode() {
        return code;
    }
}
