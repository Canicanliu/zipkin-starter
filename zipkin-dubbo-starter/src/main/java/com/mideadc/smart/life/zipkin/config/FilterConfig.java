package com.mideadc.smart.life.zipkin.config;


import com.mideadc.smart.life.zipkin.filter.resttemplate.RequestTemplateTrackInterceptor;
import com.mideadc.smart.life.zipkin.filter.servlet.TraceServletFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Configuration
@ConditionalOnBean(TraceConfig.class)
@ConditionalOnClass(RestTemplate.class)
public class FilterConfig {


    /**
     * httpClient客户端拦截器，需要clientRequestInterceptor,clientResponseInterceptor分别完成cs和cr操作
     *
     * @param brave
     * @return
     */

    @Autowired
    private RestTemplate restTemplate;

    @Bean
    RestTemplate template() {
        return new RestTemplate();
    }

    // 添加rest template拦截器
    @PostConstruct
    public void init() {
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<ClientHttpRequestInterceptor>(restTemplate.getInterceptors());
        interceptors.add(new RequestTemplateTrackInterceptor());
        restTemplate.setInterceptors(interceptors);
    }

    /**
     * servlet过滤器,自定义过滤器完成cs和cr
     *
     * @return
     */
    @Bean
    public FilterRegistrationBean callTrackingServletFilter() {
        TraceServletFilter filter = new TraceServletFilter();
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(filter);
        List<String> urlPatterns = new ArrayList();
        urlPatterns.add("/*");
        registrationBean.setUrlPatterns(urlPatterns);
        registrationBean.setOrder(1);
        return registrationBean;
    }
}
