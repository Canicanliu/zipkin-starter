package com.mideadc.smart.life.zipkin.filter;

import com.mideadc.smart.life.framework.log.MdcUtils;
import com.mideadc.smart.life.zipkin.agent.TraceAgent;
import com.mideadc.smart.life.zipkin.base.TraceContext;
import com.mideadc.smart.life.zipkin.utils.IdUtils;
//import com.mideadc.smart.life.zipkin.utils.MDCUtils;
import com.mideadc.smart.life.zipkin.utils.NetworkUtils;
import com.twitter.zipkin.gen.Annotation;
import com.twitter.zipkin.gen.BinaryAnnotation;
import com.twitter.zipkin.gen.Endpoint;
import com.twitter.zipkin.gen.Span;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: liutm
 * @Descripion:
 * @Date: Created in 15:06 2019-9-16
 */
@Slf4j
public abstract class BaseFilter {
    /**
     * 创建span信息
     */
    protected Span createSpan() {
        Span span = new Span();
        long id = IdUtils.getId();
        span.setId(id);

        Long traceId = TraceContext.getTraceId();
        // 首次调用
        if (traceId == null) {
            TraceContext.start();
            traceId = id;
            TraceContext.setTraceId(traceId);
        }
//        MdcUtils.put(MdcUtils.reuestId,traceId+"");
        span.setTrace_id(traceId);
        span.setName(TraceContext.getTraceConfig().getApplicationName());

        span.setParent_id(TraceContext.getSpanId());
        // 首次调用spanId和parentId相等
        if (TraceContext.getSpanId() == null) {
            span.setParent_id(span.getId());
        }
        TraceContext.setSpanId(span.getId());

        return span;
    }

    /**
     * 添加节点信息
     */
    public void addToAnnotations(Span span, String traceType, Long timeStamp) {
        span.addToAnnotations(
                Annotation.create(timeStamp, traceType,
                        Endpoint.create(TraceContext.getTraceConfig().getApplicationName(),
                                NetworkUtils.getIp(),
                                TraceContext.getTraceConfig().getServerPort()))
        );
    }

    /**
     * 增加接口信息
     */
    protected  void addToBinary_annotations(Span span, String key, String value){
        span.addToBinary_annotations(BinaryAnnotation.create(key, value,
                Endpoint.create(TraceContext.getTraceConfig().getApplicationName(),
                        NetworkUtils.getIp(),
                        TraceContext.getTraceConfig().getServerPort())));
    }

    /**
     * 结束调用链
     */
    public void endTrace(Span span, Long duration, String traceType,boolean clear) {
        addToAnnotations(span, traceType, System.currentTimeMillis() * 1000);
        span.setDuration(duration);
        TraceAgent.getTraceAgent().send(span);
        if (clear) {
            TraceContext.clear();
        }
    }




}
