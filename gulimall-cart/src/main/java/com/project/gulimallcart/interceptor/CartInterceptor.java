package com.project.gulimallcart.interceptor;


import com.project.gulimallcart.constant.CartConst;
import com.project.gulimallcart.vo.UserInfoTo;
import io.renren.common.vo.MemberRespVo;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

public class CartInterceptor implements HandlerInterceptor {

    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();

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
                    userInfo.setIsTemp(false);
                }
            }
        }
        //如果id和userkey都不存在，则新建一个userkey
        if (StringUtils.isEmpty(userInfo.getUserKey())){
            String userKey = UUID.randomUUID().toString();
            userInfo.setUserKey(userKey);

        }

        threadLocal.set(userInfo);

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        UserInfoTo userInfoTo = threadLocal.get();
        if(userInfoTo.getIsTemp()){
            Cookie userCookie = new Cookie(CartConst.TEMP_USER_COOKIE_NAME, userInfoTo.getUserKey());
            userCookie.setDomain("gulimall.com");
            userCookie.setMaxAge(CartConst.USER_COOKIE_MAX_AGE);
            response.addCookie(userCookie);
        }

    }
}
