package com.gittors.apollo.extend.admin.web.handler;

import com.gittors.apollo.extend.common.manager.CacheManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author zlliu
 * @date 2020/8/25 21:43
 */
@Slf4j
public class TokenInterceptor extends HandlerInterceptorAdapter {

    public static final String BEAN_NAME = "tokenInterceptor";

    @Autowired
    @Qualifier("extendAdminCacheManager")
    private CacheManager cacheManager;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (cacheManager.get(request.getRequestURI()) != null) {
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
