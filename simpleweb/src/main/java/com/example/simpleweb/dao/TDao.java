package com.example.simpleweb.dao;

import org.springframework.stereotype.Component;

@Component
public class TDao {

    public String getT(){
        try {
            Thread.sleep(2*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "T";
    }
}
