package com.mideadc.smart.life.zipkin.config;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.EmptySpanCollectorMetricsHandler;
import com.github.kristofa.brave.Sampler;
import com.github.kristofa.brave.SpanCollector;
import com.github.kristofa.brave.http.HttpSpanCollector;
import com.github.kristofa.brave.http.HttpSpanCollector.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnBean(TraceConfig.class)
public class ZipkinConfig {

    @Autowired
    TraceConfig traceConfig;

    /**
     * 配置收集器
     *
     * @return
     */
    @Bean("httpSpanCollector")
    public HttpSpanCollector spanCollector() {
        Config config = Config.builder().compressionEnabled(traceConfig.isCompressionEnabled()).connectTimeout(traceConfig.getConnectTimeout())
                .flushInterval(traceConfig.getFlushInterval()).readTimeout(traceConfig.getReadTimeout()).build();
        return HttpSpanCollector.create(traceConfig.getZipkinUrl(), config, new EmptySpanCollectorMetricsHandler());
    }

    /**
     * Brave各工具类的封装
     *
     * @param spanCollector
     * @return
     */
    @Bean
    public Brave brave(SpanCollector spanCollector) {
        Brave.Builder builder = new Brave.Builder(traceConfig.getApplicationName());// 指定serviceName
        builder.spanCollector(spanCollector);
        builder.traceSampler(Sampler.create(1));// 采集率
        return builder.build();
    }


}