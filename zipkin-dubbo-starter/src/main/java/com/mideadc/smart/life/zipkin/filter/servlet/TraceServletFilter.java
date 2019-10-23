package com.mideadc.smart.life.zipkin.filter.servlet;


import com.alibaba.fastjson.JSONObject;
import com.mideadc.smart.life.zipkin.base.TraceContext;
import com.mideadc.smart.life.zipkin.filter.BaseFilter;
import com.mideadc.smart.life.zipkin.utils.MDCUtils;
import com.mideadc.smart.life.zipkin.utils.NetworkUtils;
import com.twitter.zipkin.gen.BinaryAnnotation;
import com.twitter.zipkin.gen.Endpoint;
import com.twitter.zipkin.gen.Span;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author: liutm
 * @Descripion:
 * @Date: Created in 15:57 2019-9-19
 */
@Slf4j
@Component
public class TraceServletFilter extends BaseFilter implements Filter {

    public TraceServletFilter() {
    }

    public void destroy() {
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) req;

        HttpServletResponse response = (HttpServletResponse) resp;
        ByteArrayResponseWrapper wrapper = new ByteArrayResponseWrapper(response);


        BufferedHttpRequestWrapper newReq = new BufferedHttpRequestWrapper(httpReq);
        Span span = this.startTrace(newReq);
//        Span span = this.startTrace(httpReq);

        Long timeStamp = System.currentTimeMillis() * 1000;
        span.setTimestamp(timeStamp);
        addToAnnotations(span, TraceContext.ANNO_SR, timeStamp);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            chain.doFilter(newReq, wrapper);
//            chain.doFilter(httpReq, resp);
            stopWatch.stop();
            String result=wrapper.getResponseData(resp.getCharacterEncoding());
            addToBinary_annotations(span, "results:",result);
            ServletOutputStream out = resp.getOutputStream();
            out.write(result.getBytes(resp.getCharacterEncoding()));
            out.flush();
            out.close();
        } catch (Throwable var15) {
            log.error("异常",var15);
            span.addToBinary_annotations(BinaryAnnotation.create("error", var15.getMessage(),
                    Endpoint.create(TraceContext.getTraceConfig().getApplicationName(),
                            NetworkUtils.getIp(),
                            TraceContext.getTraceConfig().getServerPort())));
            this.endTrace(span, stopWatch.getTotalTimeMillis() * 1000, TraceContext.ANNO_SS,true);
            throw var15;
        } finally {
            HttpServletResponse var12 = (HttpServletResponse) resp;
            var12.setHeader("trace_id", String.valueOf(span.getTrace_id()));
            var12.setHeader("span_id", String.valueOf(span.getId()));
            this.endTrace(span, stopWatch.getTotalTimeMillis() * 1000, TraceContext.ANNO_SS,true);
        }

    }


    public Span startTrace(HttpServletRequest httpReq) {
        // 处理HTTP头部trace信息
        getTraceHttpHeader(httpReq);
        Span span = createSpan();
        //MDCUtils.put(MDCUtils.requestId,span.getTrace_id()+"");
        span.setName("HTTP:" + NetworkUtils.ip + ":" + TraceContext.getTraceConfig().getServerPort() + httpReq.getRequestURI());

        // cookies
        // addToBinary_annotations(span,"cookies",Arrays.toString(httpReq.getCookies()));
        if(httpReq instanceof BufferedHttpRequestWrapper) {
            BufferedHttpRequestWrapper temp=(BufferedHttpRequestWrapper)httpReq;
            addToBinary_annotations(span, "params", temp.getRequestBody());
        }else {
            addToBinary_annotations(span, "paramMaps", JSONObject.toJSONString(httpReq.getParameterMap()));
        }
        return span;
    }

    protected void getTraceHttpHeader(HttpServletRequest httpReq) {

        String traceId = httpReq.getHeader("trace_id");
        String spanId = httpReq.getHeader("span_id");

        if(StringUtils.isNotBlank(traceId)){
            TraceContext.setTraceId(Long.parseLong(traceId));
        }

        if(StringUtils.isNotBlank(spanId)){
            TraceContext.setSpanId(Long.parseLong(spanId));
        }
    }

}

