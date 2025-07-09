package com.rdn.prompt.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.StringUtils;

@Getter
@AllArgsConstructor
public enum RoleEnum {

    /**
     * 用户
     */
    USER(1, "用户"),
    /**
     * 管理员
     */
    ADMIN(2, "管理员"),

    /**
     * 普通访客
     */
    VISITOR(-99999, "普通访客"),
    ;

    private final Integer code;

    private final String name;

    public static RoleEnum getRoleByName(String name){
        if(StringUtils.isEmpty(name)){
            return null;
        }

        for (RoleEnum role : RoleEnum.values()) {
            if(role.name().equals(name)){
                return role;
            }
        }
        return null;
    }
}
