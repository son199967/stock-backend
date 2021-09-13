package vn.com.hust.son.stockuserid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class StockUseridApplication {

	public static void main(String[] args) {
		SpringApplication.run(StockUseridApplication.class, args);
	}

}
