package com.hxq.smart_campus.config;

import com.hxq.smart_campus.interceptor.RoleBasedAccessInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final RoleBasedAccessInterceptor roleBasedAccessInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(roleBasedAccessInterceptor)
                .addPathPatterns("/api/admin/**", "/api/teacher/**", "/api/user/**", "/api/ai/**")
                .excludePathPatterns("/api/auth/**", "/api/common/**", "/api/file/**");
    }
}