package com.hust.thailq.wallet.reward;

import com.hust.thailq.wallet.dto.request.RedeemRequest;
import com.hust.thailq.wallet.dto.response.CommandResponse;
import com.hust.thailq.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rewards")
@RequiredArgsConstructor
public class RewardController {

    private final WalletService walletService;

    @PostMapping("/redeem")
    public ResponseEntity<CommandResponse> redeemReward(@Valid @RequestBody RedeemRequest request) {
        CommandResponse response = walletService.redeemReward(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
