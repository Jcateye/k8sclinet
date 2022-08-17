package com.nxtele.k8s.client.configuration;

import com.google.common.collect.Lists;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangjincheng
 */
@EnableSwagger2
@Configuration
@Profile({"dev", "test"})
public class SwaggerConfig {

    @Bean
    public Docket cloudApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.nxtele.app.whatsapp.cloud.controller"))
                .paths(PathSelectors.any())
                .build()
//                .globalOperationParameters(getParameters())
                // 权限验证
                .securitySchemes(new ArrayList<>(unifiedAuth()))
                .groupName("Cloud API");
    }

    @Bean
    public Docket onPremisesApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.nxtele.app.whatsapp.onpremises.controller"))
                .paths(PathSelectors.any())
                .build()
//                .globalOperationParameters(getParameters())
                // 权限验证
                .securitySchemes(new ArrayList<>(unifiedAuth()))
                .groupName("On-Premises API");
    }

    @Bean
    public Docket businessManagementApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.nxtele.app.whatsapp.bma.controller"))
                .paths(PathSelectors.any())
                .build()
//                .globalOperationParameters(getParameters())
                // 权限验证
                .securitySchemes(new ArrayList<>(unifiedAuth()))
                .groupName("Business Management API");
    }

    @Bean
    public Docket common() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.nxtele.app.controller"))
                .paths(PathSelectors.any())
                .build()
//                .globalOperationParameters(getParameters())
                // 权限验证
                .securitySchemes(new ArrayList<>(unifiedAuth()))
                .groupName("Common API");
    }

    /**
     * <h2>Swagger 的描述信息</h2>
     */
    public ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("WhatsApp API")
                .description("WhatsApp API对接, 包括: Cloud API 、On-Premises API 、Business Management API")
                .contact(new Contact(
                        "nxtele", "https://www.nxcloud.com", "Zhangjincheng@nxcloiud.com"
                ))
                .version("1.0")
                .build();
    }

    private List<Parameter> getParameters() {
        ParameterBuilder aParameterBuilder = new ParameterBuilder();
        aParameterBuilder
                .name("Authorization")
                .description("Authorization")
                .modelRef(new ModelRef("string"))
                .parameterType("header")
                .build();
        List<Parameter> aParameters = Lists.newArrayList();
        aParameters.add(aParameterBuilder.build());
        return aParameters;
    }

    /**
     * 全站统一认证设置
     *
     * @return
     */
    private static List<ApiKey> unifiedAuth() {
        List<ApiKey> arrayList = new ArrayList();
        arrayList.add(new ApiKey("Authorization", "Authorization", "header"));
        return arrayList;
    }
}
