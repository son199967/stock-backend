package vn.com.hust.stock.stockapp.config;

import org.modelmapper.ModelMapper;
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

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }


}
