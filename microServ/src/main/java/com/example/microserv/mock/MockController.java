package com.example.microserv.mock;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MockController {

    @Value("${server.port}")
    private String port;

    @GetMapping("/d")
    public String index(){
        return "microServ" + port;
    }
}
