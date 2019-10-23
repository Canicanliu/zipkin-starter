package com.mideadc.smart.life.zipkin.utils;

import org.slf4j.MDC;

/**
 * @Author: liutm
 * @Descripion:
 * @Date: Created in 15:57 2019-9-19
 */
public class MDCUtils {

    public static final String requestId="requestId";

    public static void put(String key,String value){
        MDC.put(key,value);
    }

    public static String get(String key){
        return MDC.get(key);
    }

}
