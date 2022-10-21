package io.seata.demo.order;

import io.seata.demo.order.config.FeignSupportConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@Import({FeignSupportConfig.class})
@EnableFeignClients
@SpringBootApplication
@EnableDiscoveryClient
public class OrderDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderDemoApplication.class, args);
    }

}
