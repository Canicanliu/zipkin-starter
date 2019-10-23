package com.mideadc.smart.life.zipkin.filter.dubbo;

import com.alibaba.dubbo.rpc.*;
import com.alibaba.fastjson.JSONObject;
import com.mideadc.smart.life.zipkin.base.TraceContext;
import com.mideadc.smart.life.zipkin.filter.BaseFilter;
import com.twitter.zipkin.gen.Span;

import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.Arrays;
import java.util.Map;

/**
 * @Author: liutm
 * @Descripion:
 * @Date: Created in 15:57 2019-9-19
 */
@Component
public class TraceConsumerFilter extends BaseFilter implements Filter {

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
        // 记录出参
        String resultStr="";
        if(result!=null&&result.getValue()!=null){
            resultStr=result.getValue().toString();
        }
        addToBinary_annotations(span,"consumerResults",resultStr);
        this.endTrace(span, stopWatch.getTotalTimeMillis() * 1000, TraceContext.ANNO_CR,false);
        return result;
    }

    protected Span startTrace(Invoker<?> invoker, Invocation invocation) {
        Span span = createSpan();

        Long timeStamp = System.currentTimeMillis() * 1000;
        span.setTimestamp(timeStamp);
        span.setName("dubboConsumer=>" + invoker.getInterface().getSimpleName() + "=>" + invocation.getMethodName());

        addToAnnotations(span, TraceContext.ANNO_CS, timeStamp);

        Map<String, String> attaches = invocation.getAttachments();
        attaches.put(TraceContext.TRACE_ID_KEY, String.valueOf(span.getTrace_id()));
        attaches.put(TraceContext.SPAN_ID_KEY, String.valueOf(span.getId()));

        // 记录入参
        addToBinary_annotations(span,"consumerParams", JSONObject.toJSONString(invocation.getArguments()));
        return span;
    }
}
