package com.devsu.bank.cliente.infrastructure.entry.points.rest.controller.config;

import com.devsu.bank.cliente.infrastructure.entry.points.rest.controller.ClienteController;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * URL path versioning (api-design.md §5.1): every controller of this adapter answers under /v1. The
 * contract declares the same base path (servers: /api/v1); springdoc and actuator stay unversioned.
 */
@Configuration(proxyBeanMethods = false)
public class ApiVersionConfig implements WebMvcConfigurer {

  public static final String V1 = "/v1";

  @Override
  public void configurePathMatch(PathMatchConfigurer configurer) {
    configurer.addPathPrefix(V1, HandlerTypePredicate.forBasePackageClass(ClienteController.class));
  }
}
