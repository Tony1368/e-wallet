package com.hust.thailq.wallet.dto.request;

import jakarta.validation.constraints.NotBlank;
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

    @NotNull(message = "{validation.field.amount.required}")
    private BigDecimal amount;

    @Size(max = 50, message = "{validation.field.description.length}")
    private String description;

    private String fromWalletIban;

    private String toWalletIban;

    private Long fromWalletId;

    private Long toWalletId;

    private Long typeId;
}
