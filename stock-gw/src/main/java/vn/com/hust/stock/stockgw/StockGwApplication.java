package vn.com.hust.stock.stockgw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
//@EnableEurekaServer
public class StockGwApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockGwApplication.class, args);
    }

}
