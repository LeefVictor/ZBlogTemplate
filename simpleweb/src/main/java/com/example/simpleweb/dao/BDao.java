package com.example.simpleweb.dao;

import org.springframework.stereotype.Component;

@Component
public class BDao {

    public String getA(){
        try {
            Thread.sleep(5*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "a";
    }


    public String getB(){
        return "b";
    }
}
