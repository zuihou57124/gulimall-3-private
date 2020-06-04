package com.project.gulimallorder.order;

import com.alibaba.fastjson.JSONObject;
import com.project.gulimallorder.order.constant.OrderConst;
import io.renren.common.vo.MemberRespVo;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class OrderInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberRespVo> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        MemberRespVo memberRespVo = (MemberRespVo) request.getSession().getAttribute(OrderConst.LOGIN_USER);
        if(memberRespVo==null){
            request.getSession().setAttribute("msg","请先登录");
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return false;
        }
        threadLocal.set(memberRespVo);
        return true;
    }
}
