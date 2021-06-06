package com.zzx.gulimall.cart.interceptor;

import com.zzx.common.constant.AuthServerConstant;
import com.zzx.common.constant.CartConstant;
import com.zzx.common.vo.MemberResponseVO;
import com.zzx.gulimall.cart.to.UserInfoTO;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * 执行目标方法之前，判断用户的登陆状态，并封装传递给controller目标请求
 *
 * @author zzx
 * @date 2021-06-04 21:32
 */
@Component
public class CartInterceptor implements HandlerInterceptor {

    public static ThreadLocal<UserInfoTO> threadLocal = new ThreadLocal<UserInfoTO>();

    /**
     * 在目标方法之前执行
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {
        UserInfoTO userInfoTo = new UserInfoTO();
        HttpSession session = request.getSession();
        MemberResponseVO member = (MemberResponseVO) session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (member != null) {
            // 用户已经登陆
            userInfoTo.setUserId(member.getId());
        }
        // 用户没有登陆，获取cookie，判断是否已经有user-key
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (CartConstant.TEMP_USER_COOKIE_NAME.equals(cookie.getName())) {
                    // 如果已经存在user-key的cookie，将cookie的值存入userInfoTo
                    userInfoTo.setUserKey(cookie.getValue());
                    // 标识当前用户是临时用户
                    userInfoTo.setTempUser(true);
                    break;
                }
            }
        }


        // 给所有用户一个user-key
        if (StringUtils.isEmpty(userInfoTo.getUserKey())) {
            String userKey = UUID.randomUUID().toString().replace("-", "");
            userInfoTo.setUserKey(userKey);
        }

        // 将组织好的对象set到ThreadLocal中
        threadLocal.set(userInfoTo);
        return true;
    }

    /**
     * 在目标方法之后执行
     *
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 获取到UserInfoTO，记得ThreadLocal#remove()
        UserInfoTO userInfoTo = threadLocal.get();
        threadLocal.remove();
        if (!userInfoTo.getTempUser()) {
            // 如果不是临时用户，延长Cookie的时间
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoTo.getUserKey());
            cookie.setDomain("gulimall.com");
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }
    }
}
