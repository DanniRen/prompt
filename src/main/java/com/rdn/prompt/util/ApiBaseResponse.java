package com.rdn.prompt.util;

import com.rdn.prompt.common.ErrorCode;
import com.rdn.prompt.common.MessageConstant;

import java.io.Serializable;

/**
 * API的基础响应体
 * @author RDN
 * @since 2025/7/8
 */
public abstract class ApiBaseResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    // 当前时间戳
    public Long timestmap;

    // HTTP状态码
    public Integer code;

    /**
     * 创建成功的响应体，无数据
     * @return ApiSuccessResponse
     */
    public static ApiBaseResponse success() {
        return new ApiSuccessResponse(MessageConstant.SUCCESS);
    }

    /**
     * 创建成功的响应体，有数据
     * @param data 数据体
     * @return ApiSuccessResponse
     * @param <T>
     */
    public static <T> ApiBaseResponse success(T data) {
        return new ApiSuccessResponse<T>(data);
    }

    /**
     * 创建失败的响应体，自定义错误码和错误信息
     * @param code 错误码
     * @param msg 错误信息
     * @return
     */
    public static ApiBaseResponse error(Integer code, String msg) {
        return new ApiErrorResponse(code, msg);
    }


    public static ApiBaseResponse error(ErrorCode errorCode) {
        return new ApiErrorResponse(errorCode.getCode(), errorCode.getMessage());
    }

    public Integer getCode() {
        return code;
    }
}
