package com.cgzd;


import com.cgzd.common.util.ApplicationMainStartStyleUtil;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class PayApplication {

    public static void main(String[] args) {
        ApplicationMainStartStyleUtil.run(PayApplication.class, args);
    }

}
