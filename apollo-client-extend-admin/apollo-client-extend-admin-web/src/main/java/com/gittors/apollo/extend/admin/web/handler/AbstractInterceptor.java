package com.gittors.apollo.extend.admin.web.handler;

import com.gittors.apollo.extend.common.constant.ApolloExtendAdminConstant;
import com.gittors.apollo.extend.common.manager.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zlliu
 * @date 2022/8/25 15:18
 */
public class AbstractInterceptor extends HandlerInterceptorAdapter {
    /**
     * 限流开关
     */
    @Value("${request.limit.switch:false}")
    private boolean limitSwitch;

    @Autowired
    @Qualifier(ApolloExtendAdminConstant.EXTEND_ADMIN_CACHE_MANAGER)
    protected CacheManager cacheManager;

    public HandlerPredicate getHandlerPredicate() {
        //  限流，1分钟只能发起一次
        return (request) -> limitSwitch && cacheManager.get(request.getRequestURI()) != null;
    }

    @FunctionalInterface
    public interface HandlerPredicate {
        boolean match(HttpServletRequest request);
    }
}
