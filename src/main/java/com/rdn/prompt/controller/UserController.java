package com.rdn.prompt.controller;

import com.rdn.prompt.auth.Permission;
import com.rdn.prompt.auth.RoleEnum;
import com.rdn.prompt.enums.ErrorCode;
import com.rdn.prompt.entity.dto.RegistryDTO;
import com.rdn.prompt.service.UserService;
import com.rdn.prompt.util.ApiBaseResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @Description 关于用户的所有Api请求
 * @Author rdn
 * @Date 2025/7/10
 */

@RestController
@Api(tags = "用户模块")
@Slf4j
@RequestMapping("/api/user")
public class UserController {
    @Resource
    private UserService userService;

    @ApiOperation(value = "用户登录", notes = "用户登录")
    @PostMapping("/login")
    public ApiBaseResponse login(@RequestBody @Valid RegistryDTO userDTO) {
        return userService.login(userDTO);
    }

    @ApiOperation(value = "用户注册", notes = "用户注册")
    @PostMapping("/registry")
    public ApiBaseResponse registry(@RequestBody @Valid RegistryDTO userDTO) {
        return userService.register(userDTO);
    }

    @Permission(RoleEnum.ADMIN)
    @ApiOperation(value = "根据id删除用户", notes = "根据id删除用户，仅有管理员有权限，并且不能删除自己")
    @DeleteMapping("/delete")
    public ApiBaseResponse deleteById(@RequestParam String id, HttpServletRequest request) {
        String userId = request.getAttribute("userId").toString();
        if(userId == null || id.equals(userId)){
            return ApiBaseResponse.error(ErrorCode.USER_DELETE_NOT_ALLOWED);
        }
        return userService.deleteByUserId(id);
    }


}
