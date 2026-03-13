package com.hust.thailq.transaction.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequest {

    @NotNull
    private BigDecimal amount;

    private String description;

    private String fromWalletIban;

    private String toWalletIban;

    private Long fromWalletId;

    private Long toWalletId;

    private Long typeId;
}
