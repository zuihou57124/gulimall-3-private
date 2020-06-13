package com.project.gulimallmember.member.interceptor;

import com.project.gulimallmember.member.constant.*;
import io.renren.common.vo.MemberRespVo;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberRespVo> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        /*boolean match = new AntPathMatcher().match("/order/order/status/**",request.getRequestURI());
        if(match){
            return true;
        }*/

        MemberRespVo memberRespVo = null;
        try {
            memberRespVo = (MemberRespVo) request.getSession().getAttribute(MemberConst.LOGIN_USER);
        }catch (Exception e){
            Thread.sleep(200);
            memberRespVo = (MemberRespVo) request.getSession().getAttribute(MemberConst.LOGIN_USER);
        }
        if(memberRespVo==null){
            request.getSession().setAttribute("msg","请先登录");
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return false;
        }
        threadLocal.set(memberRespVo);

        return true;
    }
}
