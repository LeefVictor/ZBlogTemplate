package com.example.gateway.hystrix;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RefreshScope
@RestController
public class FallbackController {

    @Autowired
    private Environment env;

    @RequestMapping("fallback")
    public String fallback(){
        return "服务不可用" ;
    }
}
