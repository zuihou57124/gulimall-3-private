package com.project.gulimallmember.member.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class MyFeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor(){
        return requestTemplate -> {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if(requestAttributes!=null){
                HttpServletRequest request = requestAttributes.getRequest();
                String cookie = request.getHeader("Cookie");
                requestTemplate.header("Cookie",cookie);
            }
        };
    }

}
