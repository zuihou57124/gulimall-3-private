package com.project.gulimallorder.order.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.project.gulimallorder.order.constant.OrderConst;
import io.renren.common.vo.MemberRespVo;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class OrderInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberRespVo> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        AntPathMatcher match = new AntPathMatcher();
        Boolean m1 = match.match("/order/order/status/**",request.getRequestURI());
        Boolean m2 = match.match("/payed/**",request.getRequestURI());

        if(m1 || m2){
            return true;
        }

        MemberRespVo memberRespVo = null;
        try {
            memberRespVo = (MemberRespVo) request.getSession().getAttribute(OrderConst.LOGIN_USER);
        }catch (Exception e){
            Thread.sleep(200);
            memberRespVo = (MemberRespVo) request.getSession().getAttribute(OrderConst.LOGIN_USER);
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
