package com.hust.thailq.wallet.dto.response;

import com.hust.thailq.wallet.domain.enums.WalletStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletResponse {

    private Long id;
    private String iban;
    private String name;
    private BigDecimal balance;
    private Long userId;
    private String createdAt;
    private String bankInfo;
    private WalletStatus status;
    private Long branchId;
}
