package com.zzx.gulimall.order.interceptor;

import com.zzx.common.constant.AuthServerConstant;
import com.zzx.common.vo.MemberResponseVO;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author zzx
 * @date 2021-06-09 15:52
 */
@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberResponseVO> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行ware微服务的远程调用，让其无需登陆
        boolean match = new AntPathMatcher().match("/order/order/status/**", request.getRequestURI());
        if (match) {
            return true;
        }


        MemberResponseVO attribute = (MemberResponseVO) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
        if (attribute != null) {
            loginUser.set(attribute);
            return true;
        } else {
            request.setAttribute("msg", "请先进行登录");
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return false;
        }
    }
}
