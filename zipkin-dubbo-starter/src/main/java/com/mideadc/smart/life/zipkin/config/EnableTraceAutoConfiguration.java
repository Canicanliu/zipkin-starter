package com.mideadc.smart.life.zipkin.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConditionalOnBean(annotation = EnableTraceAutoConfigurationProperties.class)
@AutoConfigureAfter(SpringBootConfiguration.class)
@EnableConfigurationProperties(TraceConfig.class)
@ComponentScan(basePackages = "com.mideadc.smart.life.zipkin")
public class EnableTraceAutoConfiguration {
}
