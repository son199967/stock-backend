package vn.com.hust.son.stockuserid.config.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/heath")
public class HeathCheckController {
    @GetMapping
    public String heathCheck(){
        return "userId 1 Oke";
    }
}
