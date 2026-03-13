package com.hust.thailq.dto.request;

import com.hust.thailq.domain.enums.WalletStatus;
import lombok.Data;

@Data
public class UpdateWalletStatusRequest {
    private WalletStatus status;
} 