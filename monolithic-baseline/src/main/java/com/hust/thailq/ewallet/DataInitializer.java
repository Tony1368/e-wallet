package com.hust.thailq.ewallet;

import com.hust.thailq.ewallet.entity.User;
import com.hust.thailq.ewallet.entity.Wallet;
import com.hust.thailq.ewallet.repository.UserRepository;
import com.hust.thailq.ewallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("johnd@e"));
            admin.setFirstName("Admin");
            admin.setLastName("User");
            userRepository.save(admin);

            Wallet w1 = new Wallet();
            w1.setIban("GB33BUKB20201555555555");
            w1.setName("Apple Wallet");
            w1.setBalance(new BigDecimal("5000000"));
            w1.setUserId(admin.getId());
            walletRepository.save(w1);

            Wallet w2 = new Wallet();
            w2.setIban("GB94BARC10201530093459");
            w2.setName("Samsung Wallet");
            w2.setBalance(new BigDecimal("3000000"));
            w2.setUserId(admin.getId());
            walletRepository.save(w2);
        }
    }
}
