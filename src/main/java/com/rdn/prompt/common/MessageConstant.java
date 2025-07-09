package com.rdn.prompt.common;

public class MessageConstant {

    private MessageConstant() {
        throw new IllegalStateException("MessageConstant class");
    }

    public static final String PARAMS_NOT_NULL = "参数是必需的！";
    public static final String PARAMS_LENGTH_REQUIRED = "参数的长度必须符合要求！";
    public static final String PARAMS_FORMAT_ERROR = "参数格式错误！";
    public static final String PARAMS_TYPE_ERROR = "类型转换错误";
    public static final String DATA_ALREADY_EXIST = "数据已经存在！";
    public static final String DATA_IS_NULL = "数据为空！";
    public static final String FORMAT_ERROR = "格式不支持！";
    public static final String DATA_DUPLICATE = "该数据已经存在！";
    public static final String REQUEST_METHOD_ERROR = "请求方法不对！";
    public static final String SUCCESS = "Success";
}
