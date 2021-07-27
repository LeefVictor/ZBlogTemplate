package com.example.simpleweb.serv;

import java.util.concurrent.CompletableFuture;
import org.springframework.stereotype.Service;

@Service
public class ServD {

    public String getD(){
        CompletableFuture.supplyAsync(()->{
            System.out.println(11);
            return "done";
        });
        return "D";
    }
}
