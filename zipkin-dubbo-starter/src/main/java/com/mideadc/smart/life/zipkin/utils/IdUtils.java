package com.mideadc.smart.life.zipkin.utils;

import java.util.Random;


public class IdUtils {
    public static Long getId() {
        return NetworkUtils.getIp() + System.currentTimeMillis()+getRandom();
    }

    public static long getRandom(){
        Random random=new Random();
        return random.nextLong();
    }

    public static void main(String[] args) {
        System.out.println("id：" + new Random().nextLong());
    }
}
