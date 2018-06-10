package com.ssm.interceptor;

import com.ssm.controller.LoginController;
import com.ssm.service.UserRegisterService;
import com.ssm.utils.DesUtil;
import com.ssm.utils.GetBeanAndRandomSelect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginInterceptor extends HandlerInterceptorAdapter {

    private final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (request.getRequestURI().contains("land")) {
            logger.info("URL IS {}", request.getRequestURI());
            return true;
        }

        Cookie[] cookies = request.getCookies();
        for (Cookie c : cookies) {
            logger.info("cookie_name={},cookie_value={}", c.getName(), c.getValue());
            if (c.getName().equals("token")) {
                String decrypted = DesUtil.decrypt(c.getValue());
                String[] strKey = decrypted.split("\\.");
                long expired_time = System.currentTimeMillis() - Long.parseLong(strKey[1]);
                logger.info("expired_time : {}", expired_time);
                //随机访问
                UserRegisterService userRegisterService =
                        (UserRegisterService) GetBeanAndRandomSelect.randomSelect("myRMIClientC12", "myRMIClientC22");
                if (userRegisterService.getByName(strKey[0]) != null && expired_time < 1000 * 60 * 30) {
                    return true;
                }
            }
        }

        request.setAttribute("msg","尚未登录，请重新登录");
        request.getRequestDispatcher("/WEB-INF/jsp/loginPage.jsp").forward(request, response);

        return false;
    }
}