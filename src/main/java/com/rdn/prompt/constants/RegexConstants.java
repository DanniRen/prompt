package com.rdn.prompt.constants;

public class RegexConstants {
    public RegexConstants() {
        throw  new IllegalAccessError("RegexConstants class");
    }

    /**
     * 密码的格式
     */
    public static final String PASSWORD_REGEX = "^[a-zA-Z0-9@$!%*?&]{3,32}$";

    /**
     * 只能是字母、数字、下划线组成
     */
    public static final String NUM_WORD_REG = "^[A-Za-z0-9_]+$";

    /**
     * 邮箱格式
     */
    public static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z]{2,}(?:\\.[a-zA-Z]{2,})?$";

    public static final String VERSION_REGEX = "\\d+\\.\\d+\\.\\d+";
}
