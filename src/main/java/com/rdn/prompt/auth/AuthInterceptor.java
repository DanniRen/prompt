package com.rdn.prompt.auth;


import com.rdn.prompt.enums.ErrorCode;
import com.rdn.prompt.entity.User;
import com.rdn.prompt.service.UserService;
import com.rdn.prompt.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.MediaType;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Description 权限校验拦截器
 */
public class AuthInterceptor implements HandlerInterceptor {

    private final UserService userService;

    public AuthInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 1.检查注解，看当前请求是否有权限信息
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();

        Permission permissionClass = AnnotationUtils.findAnnotation(HandlerMethod.class, Permission.class);
        Permission permissionMethod = AnnotationUtils.findAnnotation(method, Permission.class);
        if (permissionClass == null && permissionMethod == null) {
            // 不需要校验直接放行
            return true;
        }

        // 2.token解析与用户验证
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            sendErrorResponse(response, ErrorCode.UNAUTHORIZED);
            return false;
        }
        String token = authorization.substring(7);
        Claims claims = JwtUtil.verifyToken(token);
        if (claims == null) {
            sendErrorResponse(response, ErrorCode.UNAUTHORIZED);
        }
        String userId = claims.getSubject();
        User user = userService.getById(userId);
        if(user == null || user.getRole() == null){
            sendErrorResponse(response, ErrorCode.USER_NOT_FOUND);
        }

        // 3.权限校验
        RoleEnum[] permissions;
        if (permissionClass != null && permissionMethod == null) {
            // 类注解不为空，方法注解为空，使用类注解
            permissions = permissionClass.role();
        } else if (permissionClass == null) {
            // 类注解为空，使用方法注解
            permissions = permissionMethod.role();
        } else {
            // 都不为空，使用方法注解
            permissions = permissionMethod.role();
        }

        if(!checkPermission(user, permissions)){
            sendErrorResponse(response, ErrorCode.UNAUTHORIZED);
            return false;
        }

        // 4.校验通过，放行
        request.setAttribute("userId", user.getId());
        return true;
    }

    private boolean checkPermission(User user, RoleEnum[] permissions) {
        Set<RoleEnum> set = Arrays.stream(permissions).collect(Collectors.toSet());
        return set.contains(user.getRole());
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(HttpServletResponse response, ErrorCode errorCode) {
        try {
            response.setStatus(errorCode.getCode());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            // 返回JSON格式错误信息
            String errorJson = String.format("{\"code\": %d, \"message\": \"%s\"}", errorCode.getCode(), errorCode.getMessage());
            response.getWriter().write(errorJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}