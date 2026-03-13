package com.hust.thailq.wallet.dto.request;

import com.hust.thailq.wallet.domain.enums.WalletStatus;
import lombok.Data;

@Data
public class UpdateWalletStatusRequest {
    private WalletStatus status;
}
