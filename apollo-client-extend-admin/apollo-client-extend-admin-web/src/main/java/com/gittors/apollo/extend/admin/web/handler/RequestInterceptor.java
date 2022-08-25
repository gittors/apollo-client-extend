package com.gittors.apollo.extend.admin.web.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.AbstractMap;
import java.util.Map;

/**
 * @author zlliu
 * @date 2020/8/25 18:51
 */
@Slf4j
public class RequestInterceptor extends AbstractInterceptor {

    public static final String BEAN_NAME = "requestInterceptor";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (getHandlerPredicate().match(request)) {
            fail(response, "Request too Frequently!");
            return false;
        }
        Map.Entry<Boolean, String> check = check(request);
        if (!check.getKey()) {
            fail(response, check.getValue());
            return false;
        }
        return super.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        cacheManager.put(request.getRequestURI(), "A valid request");
    }

    private Map.Entry<Boolean, String> check(HttpServletRequest request) {
        //  检查 Token是否有效
        String token = request.getParameter("token");
        if (!checkToken(token)) {
            return new AbstractMap.SimpleEntry<>(Boolean.FALSE, "Token Invalid!");
        }
        return new AbstractMap.SimpleEntry<>(Boolean.TRUE, null);
    }

    private boolean checkToken(String token) {
        return cacheManager.get(token) != null;
    }

    private void fail(HttpServletResponse response, String msg) {
        response.setContentType("text/plain; charset=utf-8");
        try {
            PrintWriter out = response.getWriter();
            out.write(msg);
            out.flush();
            out.close();
        } catch (IOException e) {
            log.error("#fail: ", e);
        }
    }
}
