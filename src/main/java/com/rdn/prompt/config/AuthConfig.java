package com.rdn.prompt.config;

import com.rdn.prompt.auth.AuthInterceptor;
import com.rdn.prompt.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
public class AuthConfig extends WebMvcConfigurationSupport {

    @Resource
    UserService userService;

    /**
     * @Description 添加拦截器
     * @param registry
     */
    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor(userService))
                .addPathPatterns("/**");
    }

    /**
     * @Description 解决swagger UI页面和拦截器的冲突
     * @param registry
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html", "doc.html").addResourceLocations(
                "classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations(
                "classpath:/META-INF/resources/webjars/");
        super.addResourceHandlers(registry);
    }
}
