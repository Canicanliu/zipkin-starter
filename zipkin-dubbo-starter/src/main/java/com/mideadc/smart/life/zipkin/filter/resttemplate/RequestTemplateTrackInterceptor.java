package com.mideadc.smart.life.zipkin.filter.resttemplate;


import com.mideadc.smart.life.zipkin.base.TraceContext;
import com.mideadc.smart.life.zipkin.filter.BaseFilter;
import com.mideadc.smart.life.zipkin.utils.MDCUtils;
import com.mideadc.smart.life.zipkin.utils.NetworkUtils;
import com.twitter.zipkin.gen.BinaryAnnotation;
import com.twitter.zipkin.gen.Endpoint;
import com.twitter.zipkin.gen.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StopWatch;

import java.io.IOException;
/**
 * @Author: liutm
 * @Descripion:
 * @Date: Created in 15:57 2019-9-19
 */
public class RequestTemplateTrackInterceptor extends BaseFilter implements ClientHttpRequestInterceptor {

    private static final Logger logger= LoggerFactory.getLogger(RequestTemplateTrackInterceptor.class);

    @Override
    public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        // 开启调用链
        ClientHttpResponse response = null;
        Span span = this.startTrace(httpRequest);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            // 内部请求头可以携带trace信息，外部请求改行代码注释掉
            this.setTraceToHttpHeader(httpRequest, span);
            // 保证请求继续被执行
            response = execution.execute(httpRequest, body);

            // 结束调用链
            this.endTrace(span, stopWatch.getTotalTimeMillis() * 1000, TraceContext.ANNO_CR,true);
        } catch (Exception e) {
            // 异常记录到调用链
            span.addToBinary_annotations(BinaryAnnotation.create("error", e.getMessage(),
                    Endpoint.create(TraceContext.getTraceConfig().getApplicationName(),
                            NetworkUtils.getIp(),
                            TraceContext.getTraceConfig().getServerPort())));
            this.endTrace(span, stopWatch.getTotalTimeMillis() * 1000, TraceContext.ANNO_CR,true);
            logger.error("RequestTemplate 请求异常",e);
        }

        return response;
    }

    private Span startTrace(HttpRequest httpRequest) {
        Span span = createSpan();

        Long timeStamp = System.currentTimeMillis() * 1000;
        span.setTimestamp(timeStamp);
        span.setName("restTemplate:" + httpRequest.getURI() + ":" + httpRequest.getMethod());
        addToAnnotations(span, TraceContext.ANNO_CS, timeStamp);
        return span;
    }

    protected void setTraceToHttpHeader(HttpRequest httpRequest , Span span){
        // 内部请求可以携带trace信息，外部请求改行代码注释掉
        httpRequest.getHeaders().set("trace_id", String.valueOf(span.getTrace_id()));
        httpRequest.getHeaders().set("span_id", String.valueOf(span.getId()));
    }

}
