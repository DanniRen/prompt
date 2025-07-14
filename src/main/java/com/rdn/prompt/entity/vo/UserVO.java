package com.rdn.prompt.entity.vo;

import lombok.Data;

@Data
public class UserVO {
    private String id;

    private String username;

    private String email;

    private String password;

    private String role;

    private String token;
}
