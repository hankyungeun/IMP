package com.bootest.swagger;

import java.util.ArrayList;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket apiV1() {

        return new Docket(DocumentationType.SWAGGER_2)
                .useDefaultResponseMessages(true)
                .groupName("vrmApi")
                .select()
                .apis(RequestHandlerSelectors
                        .basePackage("com.bootest.controller"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo());

    }

    private ApiInfo apiInfo() {
        return new ApiInfo(
                "VRM Rest API",
                "Documentation of all operational API for VRM",
                "version 1.0",
                "N/A",
                new Contact("Contact us", "https://gytni.com/new_gytni/", "301082@skuniv.ac.kr"),
                "N/A",
                "N/A",
                new ArrayList<>());
    }

}
