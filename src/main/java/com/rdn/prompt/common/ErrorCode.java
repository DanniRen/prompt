package com.rdn.prompt.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // HTTP状态码
    // 客户端错误
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    // 服务器错误
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),

    // 业务错误(1000-9999)
    // 用户相关(1000-1999)
    USER_NOT_FOUND(1001, "用户不存在"),
    INVALID_PASSWORD(1002, "密码错误"),
    EMAIL_ALREADY_EXISTS(1003, "邮箱已存在"),
    PHONE_ALREADY_EXISTS(1004, "手机号已存在"),
    USER_DISABLED(1005, "用户已禁用"),
    USER_HAS_EXIST(1006, "用户已存在"),
    USER_DELETE_NOT_ALLOWED(1007, "不允许删除该用户"),


    // 提示词相关(2000-2999)
    PROMPT_NOT_FOUND(2001, "提示词不存在"),
    PROMPT_DUPLICATE_TITLE(2002, "提示词标题已存在"),
    PROMPT_EMPTY_CONTENT(2003, "提示词内容不能为空"),
    PROMPT_VERSION_NOT_FOUND(2004, "提示词版本不存在"),
    PROMPT_CATEGORY_NOT_FOUND(2005, "提示词分类不存在"),
    PROMPT_TAG_NOT_FOUND(2006, "提示词标签不存在"),
    PROMPT_ACCESS_DENIED(2007, "无权限访问该提示词"),
    PROMPT_EXPORT_FAILED(2008, "提示词导出失败"),
    PROMPT_IMPORT_FAILED(2009, "提示词导入失败"),
    PROMPT_UPDATE_FAILED(2009, "提示词更新失败"),

    // 大模型相关(3000-3999)
    MODEL_API_KEY_MISSING(3001, "大模型API密钥缺失"),
    MODEL_API_CALL_FAILED(3002, "大模型API调用失败"),
    MODEL_API_TIMEOUT(3003, "大模型API调用超时"),
    MODEL_API_RATE_LIMIT(3004, "大模型API调用超出频率限制"),
    MODEL_NOT_SUPPORTED(3005, "不支持的大模型"),
    MODEL_PARAMETER_ERROR(3006, "大模型参数错误"),
    MODEL_RESPONSE_PARSING_ERROR(3007, "大模型响应解析错误"),

    // 向量数据库相关(4000-4999)
    VECTOR_DB_CONNECTION_FAILED(4001, "向量数据库连接失败"),
    VECTOR_EMBEDDING_FAILED(4002, "向量嵌入失败"),
    VECTOR_SEARCH_FAILED(4003, "向量搜索失败"),
    VECTOR_INDEX_CREATION_FAILED(4004, "向量索引创建失败"),

    // 团队协作相关(5000-5999)
    TEAM_NOT_FOUND(5001, "团队不存在"),
    TEAM_MEMBER_NOT_FOUND(5002, "团队成员不存在"),
    TEAM_MEMBER_ALREADY_EXISTS(5003, "团队成员已存在"),
    TEAM_INVITATION_NOT_FOUND(5004, "团队邀请不存在"),
    TEAM_PERMISSION_DENIED(5005, "团队权限不足"),
    TEAM_SPACE_QUOTA_EXCEEDED(5006, "团队空间配额已超出"),

    // 支付相关(6000-6999)
    PLAN_NOT_FOUND(6001, "套餐不存在"),
    PAYMENT_FAILED(6002, "支付失败"),
    SUBSCRIPTION_NOT_FOUND(6003, "订阅不存在"),
    SUBSCRIPTION_EXPIRED(6004, "订阅已过期"),
    INSUFFICIENT_CREDITS(6005, "积分不足"),

    // 文件相关(7000-7999)
    FILE_UPLOAD_FAILED(7001, "文件上传失败"),
    FILE_DOWNLOAD_FAILED(7002, "文件下载失败"),
    FILE_NOT_FOUND(7003, "文件不存在"),
    FILE_TYPE_NOT_SUPPORTED(7004, "文件类型不支持"),
    FILE_SIZE_EXCEEDED(7005, "文件大小超出限制"),

    // 系统配置相关(8000-8999)
    CONFIG_NOT_FOUND(8001, "配置项不存在"),
    CONFIG_UPDATE_FAILED(8002, "配置更新失败"),
    CONFIG_VALIDATION_ERROR(8003, "配置验证错误"),

    // 第三方服务相关(9000-9999)
    THIRD_PARTY_SERVICE_ERROR(9001, "第三方服务错误"),
    THIRD_PARTY_AUTH_FAILED(9002, "第三方认证失败"),
    THIRD_PARTY_API_LIMIT(9003, "第三方API调用限制"),
    THIRD_PARTY_SERVICE_UNAVAILABLE(9004, "第三方服务不可用");

    private final Integer code;
    private final String message;

}
