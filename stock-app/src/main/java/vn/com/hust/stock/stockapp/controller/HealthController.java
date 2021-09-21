package vn.com.hust.stock.stockapp.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {

    @RequestMapping
    public ResponseEntity<String> healthCheck(){

        return ResponseEntity.status(HttpStatus.OK).body("ready app");
    }
}
