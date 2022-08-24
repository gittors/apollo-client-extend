package com.gittors.apollo.extend.admin.web.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author zlliu
 * @date 2020/8/24 16:27
 */
@Configuration
@ConditionalOnProperty(
        name = "knife4j.swagger.enable",
        havingValue = "true",
        matchIfMissing = true
)
@EnableSwagger2
@EnableKnife4j
public class SwaggerConfiguration {
    /**
     * Swagger 扫描路径
     */
    public static final String BASE_PACKAGE = "com.gittors.apollo.extend.admin.web.endpoint";

    @Value("${spring.application.name}")
    private String serviceName;

    @Value("${swagger.service.description:Apollo Extend Admin Center Restful APIs}")
    private String description;

    @Value("${swagger.service.version:1.0.0}")
    private String version;

    @Value("${swagger.service.license.name:Apache License 2.0}")
    private String license;

    @Value("${swagger.service.license.url:http://www.apache.org/licenses/LICENSE-2.0}")
    private String licenseUrl;

    @Value("${swagger.service.contact.name:zlliu}")
    private String contactName;

    @Value("${swagger.service.contact.url:https://github.com/gittors}")
    private String contactUrl;

    @Value("${swagger.service.contact.email:}")
    private String contactEmail;

    @Value("${swagger.service.termsOfServiceUrl:}")
    private String termsOfServiceUrl;

    /**
     * 依赖 swaggerbootstrapUI
     * 访问地址：http://ip:port/doc.html
     * @return
     */
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("apollo-client-extend")
                .select()
                .apis(RequestHandlerSelectors.basePackage(BASE_PACKAGE))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title(serviceName)
                .description(description)
                .version(version)
                .license(license)
                .licenseUrl(licenseUrl)
                .contact(new Contact(contactName, contactUrl, contactEmail))
                .termsOfServiceUrl(termsOfServiceUrl)
                .build();
    }

}