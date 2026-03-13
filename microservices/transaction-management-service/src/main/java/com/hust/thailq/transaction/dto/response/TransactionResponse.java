package com.hust.thailq.transaction.dto.response;

import com.hust.thailq.transaction.domain.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {
    private Long id;
    private BigDecimal amount;
    private String description;
    private String createdAt;
    private UUID referenceNumber;
    private Status status;
    private Long fromWalletId;
    private Long toWalletId;
    private String fromWalletIban;
    private String toWalletIban;
    private String typeName;
    private WalletInfo fromWallet;
    private WalletInfo toWallet;
    private TypeInfo type;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WalletInfo {
        private Long id;
        private String iban;
        private String name;
        private UserInfo user;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfo {
        private Long id;
        private String firstName;
        private String lastName;
        private String username;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TypeInfo {
        private Long id;
        private String name;
    }
}
