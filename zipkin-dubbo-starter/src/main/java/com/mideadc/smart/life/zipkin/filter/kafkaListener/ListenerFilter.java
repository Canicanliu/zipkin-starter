package com.mideadc.smart.life.zipkin.filter.kafkaListener;

import com.alibaba.fastjson.JSONObject;
import com.mideadc.smart.life.zipkin.base.TraceContext;
import com.mideadc.smart.life.zipkin.filter.BaseFilter;
import com.mideadc.smart.life.zipkin.filter.servlet.BufferedHttpRequestWrapper;
import com.mideadc.smart.life.zipkin.utils.NetworkUtils;
import com.twitter.zipkin.gen.BinaryAnnotation;
import com.twitter.zipkin.gen.Endpoint;
import com.twitter.zipkin.gen.Span;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.lang.reflect.Method;

@Aspect
@Component
@ConditionalOnClass(KafkaListener.class)
public class ListenerFilter extends BaseFilter {

    private Logger logger = LoggerFactory.getLogger(ListenerFilter.class);

    @Pointcut(value = "@annotation(org.springframework.kafka.annotation.KafkaListener)")
    public void kafkaPointcut() {
    }

//    @Before(value = "fileControllerPointCut()")
//    public void before(ProceedingJoinPoint pjp) throws Throwable {
//        Signature signature = pjp.getSignature();
//        MethodSignature methodSignature = (MethodSignature) signature;
//
//        Method method = methodSignature.getMethod();
//        Class<?> targetClass = method.getDeclaringClass();
//        String classAndMethod = targetClass.getName().substring(targetClass.getName().lastIndexOf(".")+1) + "#" + method.getName();
//        Span span= startTrace(pjp,classAndMethod);
//        Long timeStamp = System.currentTimeMillis() * 1000;
//        span.setTimestamp(timeStamp);
//        addToAnnotations(span, TraceContext.ANNO_SR, timeStamp);
//
//    }

//    @AfterReturning(returning="rvt",value = "fileControllerPointCut()")
//    public void after(ProceedingJoinPoint pjp,Object rvt) throws Throwable {
//        this.endTrace(span, stopWatch.getTotalTimeMillis() * 1000, TraceContext.ANNO_SS,true);
//        addToBinary_annotations(span, "results:",result==null?"":JSONObject.toJSONString(result));
//    }


    @Around(value = "kafkaPointcut()")
    public Object kafkaPointcutAround(ProceedingJoinPoint pjp) throws Throwable {
        Signature signature = pjp.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;

        Method method = methodSignature.getMethod();
        Class<?> targetClass = method.getDeclaringClass();
        String classAndMethod = targetClass.getName().substring(targetClass.getName().lastIndexOf(".") + 1) + "#" + method.getName();
        Span span = startTrace(pjp, classAndMethod);
        Long timeStamp = System.currentTimeMillis() * 1000;
        span.setTimestamp(timeStamp);
        addToAnnotations(span, TraceContext.ANNO_SR, timeStamp);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            logger.info("进入listenerFilter:{}",classAndMethod);
            Object result = pjp.proceed(pjp.getArgs());
            stopWatch.stop();
            this.endTrace(span, stopWatch.getTotalTimeMillis() * 1000, TraceContext.ANNO_SS, true);
            addToBinary_annotations(span, "results:", result == null ? "" : JSONObject.toJSONString(result));
            return result;
        }catch (Exception e){
            logger.error("异常",e);
            span.addToBinary_annotations(BinaryAnnotation.create("error", e.getMessage(),
                    Endpoint.create(TraceContext.getTraceConfig().getApplicationName(),
                            NetworkUtils.getIp(),
                            TraceContext.getTraceConfig().getServerPort())));
            this.endTrace(span, stopWatch.getTotalTimeMillis() * 1000, TraceContext.ANNO_SS,true);
            throw e;
        }
    }

    public Span startTrace(ProceedingJoinPoint pjp,String classMethod) {
        Span span = createSpan();
        //MDCUtils.put(MDCUtils.requestId,span.getTrace_id()+"");
        span.setName("kafkaConsumer=>" + NetworkUtils.ip + ":" + TraceContext.getTraceConfig().getServerPort() +"=>"+ classMethod);

        addToBinary_annotations(span, "params", JSONObject.toJSONString(pjp.getArgs()));
        return span;
    }
}
