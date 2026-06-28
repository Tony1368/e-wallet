package com.hust.thailq.wallet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.hust.thailq")
@EntityScan(basePackages = "com.hust.thailq")
@EnableJpaRepositories(basePackages = "com.hust.thailq")
@EnableScheduling
public class WalletManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(WalletManagementApplication.class, args);
    }
}
