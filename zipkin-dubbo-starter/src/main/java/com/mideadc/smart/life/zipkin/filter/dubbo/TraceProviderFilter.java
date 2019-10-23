package com.mideadc.smart.life.zipkin.filter.dubbo;

import com.alibaba.dubbo.rpc.*;
import com.alibaba.fastjson.JSONObject;
import com.mideadc.smart.life.zipkin.base.TraceContext;
import com.mideadc.smart.life.zipkin.filter.BaseFilter;
import com.mideadc.smart.life.zipkin.utils.MDCUtils;
import com.twitter.zipkin.gen.Span;

import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

import java.util.Arrays;

/**
 * @Author: liutm
 * @Descripion:
 * @Date: Created in 15:57 2019-9-19
 */
@Component
public class TraceProviderFilter extends BaseFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {

        // 开启调用链
        Span span = this.startTrace(invoker, invocation);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // 远程调用
        Result result = invoker.invoke(invocation);

        // 结束调用链
        stopWatch.stop();
        String resultStr="";
        if(result!=null&&result.getValue()!=null){
            resultStr=result.getValue().toString();
        }
        // 记录出参
        this.addToBinary_annotations(span,"providerResults",resultStr);
        this.endTrace(span, stopWatch.getTotalTimeMillis() * 1000, TraceContext.ANNO_SS,true);

        return result;
    }

    protected Span startTrace(Invoker<?> invoker, Invocation invocation) {
        Long traceId = Long.valueOf(StringUtils.isEmpty(invocation.getAttachment(TraceContext.TRACE_ID_KEY))?"0":invocation.getAttachment(TraceContext.TRACE_ID_KEY));
        Long spanId = Long.valueOf(StringUtils.isEmpty(invocation.getAttachment(TraceContext.SPAN_ID_KEY))?"0":invocation.getAttachment(TraceContext.SPAN_ID_KEY));

        TraceContext.setTraceId(traceId);
        TraceContext.setSpanId(spanId);

        Span span = createSpan();

        //MDCUtils.put(MDCUtils.requestId,span.getTrace_id()+"");

        Long timeStamp = System.currentTimeMillis() * 1000;
        span.setTimestamp(timeStamp);
        addToAnnotations(span, TraceContext.ANNO_SR, timeStamp);

        span.setName("dubboProvider=>" + invoker.getInterface().getSimpleName() + "=>" + invocation.getMethodName());

        // 记录入参
        addToBinary_annotations(span,"providerParams", JSONObject.toJSONString(invocation.getArguments()));

        return span;
    }

}
