package com.devsu.customer.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(
        basePackages = {"com.devsu.customer.usecase"},
        includeFilters = {@ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*[UseCase]")},
        useDefaultFilters = false)
class UseCaseConfig {}
