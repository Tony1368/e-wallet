package com.hust.thailq.payment.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RedeemRequest {

    @NotNull(message = "Reward ID is required")
    private Long rewardId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Wallet ID is required")
    private Long walletId;
}
