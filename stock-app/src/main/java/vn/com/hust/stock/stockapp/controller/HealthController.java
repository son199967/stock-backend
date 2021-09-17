package vn.com.hust.stock.stockapp.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {

    @Value("${abc}")
    private String abc;
    @RequestMapping
    public String healthCheck(){
        return "Ready  App "+abc;
    }
}
