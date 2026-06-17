package com.hust.thailq.accounting.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCompletedEvent {

    private UUID transactionId;
    private BigDecimal amount;
    private Long fromWalletId;
    private Long toWalletId;
    private String description;
    private Long typeId;
}
