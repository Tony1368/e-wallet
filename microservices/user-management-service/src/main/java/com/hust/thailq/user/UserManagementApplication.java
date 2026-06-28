package com.hust.thailq.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.hust.thailq.user", "com.hust.thailq.common"})
@EntityScan(basePackages = "com.hust.thailq")
@EnableJpaRepositories(basePackages = "com.hust.thailq")
public class UserManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserManagementApplication.class, args);
    }
}
