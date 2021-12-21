package com.gittors.apollo.extend.admin.web.endpoint;

import com.gittors.apollo.extend.common.constant.ApolloExtendAdminConstant;
import com.gittors.apollo.extend.common.utils.EncryptUtils;
import com.gittors.apollo.extend.common.manager.CacheManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zlliu
 * @date 2020/8/25 21:32
 */
@Slf4j
@RestController
@RequestMapping(path = "/token")
@Api(tags = { "Token接口" })
public class AdminTokenEndpoint {
    @Autowired
    @Qualifier(ApolloExtendAdminConstant.EXTEND_ADMIN_CACHE_MANAGER)
    private CacheManager cacheManager;

    @RequestMapping(path = "/get", method = RequestMethod.GET)
    @ApiOperation(value = "获得访问Token", notes = "", response = ResponseEntity.class, httpMethod = "GET")
    @ResponseBody
    public ResponseEntity<?> getToken(HttpServletRequest request) {
        try {
            String token = EncryptUtils.encrypt(request.getRequestURI());
            cacheManager.put(token, "PUT Token");
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            log.error("#getToken failed:", e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Error!");
    }

}
