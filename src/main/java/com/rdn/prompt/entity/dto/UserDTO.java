package com.rdn.prompt.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserDTO {
    private String id;

    private String username;

    private String email;

    private String password;

    private String role;

    private Boolean sex;
}
