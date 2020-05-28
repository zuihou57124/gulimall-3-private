package com.project.gulimallcart;

import com.project.gulimallcart.constant.CartConst;
import com.project.gulimallcart.vo.UserInfoTo;
import io.renren.common.vo.MemberRespVo;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import reactor.core.Fuseable;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
public class interceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        UserInfoTo userInfo = new UserInfoTo();
        HttpSession session = request.getSession();
        MemberRespVo member = (MemberRespVo) session.getAttribute(CartConst.LOGIN_USER);
        if(member!=null){
            userInfo.setUserId(member.getId());
        }
        Cookie[] cookies = request.getCookies();
        if(cookies!=null){
            for (Cookie cookie : cookies) {
                if(cookie.getName().equalsIgnoreCase(CartConst.TEMP_USER_COOKIE_NAME)){
                    userInfo.setUserKey(cookie.getValue());
                }
            }
        }

        return true;
    }
}
