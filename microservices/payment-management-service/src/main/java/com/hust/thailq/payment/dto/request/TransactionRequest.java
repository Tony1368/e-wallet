package com.hust.thailq.payment.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequest {

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @Size(max = 50)
    private String description;

    private String fromWalletIban;
    private String toWalletIban;
    private Long fromWalletId;
    private Long toWalletId;
    private Long typeId;
}
