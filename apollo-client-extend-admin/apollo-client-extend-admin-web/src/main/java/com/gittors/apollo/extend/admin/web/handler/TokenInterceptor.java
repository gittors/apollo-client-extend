package com.gittors.apollo.extend.admin.web.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author zlliu
 * @date 2020/8/25 21:43
 */
@Slf4j
public class TokenInterceptor extends AbstractInterceptor {

    public static final String BEAN_NAME = "tokenInterceptor";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (getHandlerPredicate().match(request)) {
            fail(response, "Request too Frequently!");
            return false;
        }
        return super.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        cacheManager.put(request.getRequestURI(), "GET Token");
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
