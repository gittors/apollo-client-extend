package com.gittors.apollo.extend.binder.demo;

import com.gittors.apollo.extend.binder.annotation.BinderScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author zlliu
 * @date 2019/11/26 14:27
 */
@BinderScan
@SpringBootApplication
public class BinderDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(BinderDemoApplication.class, args);
    }
}

