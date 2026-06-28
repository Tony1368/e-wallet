package com.hust.thailq.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.hust.thailq")
@EntityScan(basePackages = "com.hust.thailq")
@EnableJpaRepositories(basePackages = "com.hust.thailq")
public class PaymentManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentManagementApplication.class, args);
    }
}
