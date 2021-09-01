package vn.com.hust.stock.stockjob.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;

@Configuration
@EntityScan("vn.com.hust.stock")
public class AppConfig {
    @Bean
    HttpClient httpClient() {
        return HttpClient.newHttpClient();
    }

}
