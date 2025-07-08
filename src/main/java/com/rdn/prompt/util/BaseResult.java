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

    // 创建成功的响应体，无数据

    // 创建失败的
    // 创建失败的响应体

}
