package com.hust.thailq.controller;

import com.hust.thailq.dto.request.TransactionRequest;
import com.hust.thailq.dto.request.WalletRequest;
import com.hust.thailq.dto.request.UpdateWalletStatusRequest;
import com.hust.thailq.dto.response.CommandResponse;
import com.hust.thailq.dto.response.WalletResponse;
import com.hust.thailq.service.WalletService;
import com.hust.thailq.service.IbanAnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    /**
     * Fetches a single wallet by the given id.
     *
     * @param id
     * @return WalletResponse wrapped by ResponseEntity<T>
     */
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_ACCOUNTANT', 'ROLE_CUSTOMER')")
    @GetMapping("/{id}")
    public ResponseEntity<WalletResponse> findById(@PathVariable long id) {
        final WalletResponse response = walletService.findById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Fetches a single wallet by the given iban.
     *
     * @param iban
     * @return WalletResponse wrapped by ResponseEntity<T>
     */
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_ACCOUNTANT', 'ROLE_CUSTOMER')")
    @GetMapping("/iban/{iban}")
    public ResponseEntity<WalletResponse> findByIban(@PathVariable String iban) {
        final WalletResponse response = walletService.findByIban(iban);
        return ResponseEntity.ok(response);
    }

    /**
     * Fetches a single wallet by the given userId.
     *
     * @param userId
     * @return WalletResponse wrapped by ResponseEntity<T>
     */
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_ACCOUNTANT', 'ROLE_CUSTOMER')")
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<WalletResponse>> findByUserId(@PathVariable long userId) {
        final List<WalletResponse> response = walletService.findByUserId(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Fetches all wallets based on the given paging and sorting parameters.
     *
     * @param pageable
     * @return List of WalletResponse wrapped by ResponseEntity<T>
     */
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_ACCOUNTANT', 'ROLE_CUSTOMER')")
    @GetMapping
    public ResponseEntity<Page<WalletResponse>> findAll(Pageable pageable) {
        final Page<WalletResponse> response = walletService.findAll(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Creates a new wallet using the given request parameters.
     *
     * @param request
     * @return id of the created wallet wrapped by ResponseEntity<T>
     */
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_ACCOUNTANT', 'ROLE_CUSTOMER')")
    @PostMapping
    public ResponseEntity<CommandResponse> create(@Valid @RequestBody WalletRequest request) {
        final CommandResponse response = walletService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Transfer funds between wallets.
     *
     * @param request
     * @return id of the created transaction wrapped by ResponseEntity<T>
     */
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_ACCOUNTANT', 'ROLE_CUSTOMER')")
    @PostMapping("/transfer")
    public ResponseEntity<CommandResponse> transferFunds(@Valid @RequestBody TransactionRequest request) {
        final CommandResponse response = walletService.transferFunds(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Adds funds to the given wallet.
     *
     * @param request
     * @return id of the created transaction wrapped by ResponseEntity<T>
     */
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_ACCOUNTANT', 'ROLE_CUSTOMER')")
    @PostMapping("/add")
    public ResponseEntity<CommandResponse>

    addFunds(@Valid @RequestBody TransactionRequest request) {
        final CommandResponse response = walletService.addFunds(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Withdraw funds from the given wallet.
     *
     * @param request
     * @return id of the created transaction wrapped by ResponseEntity<T>
     */
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_ACCOUNTANT', 'ROLE_CUSTOMER')")
    @PostMapping("/withdraw")
    public ResponseEntity<CommandResponse>

    withdrawFunds(@Valid @RequestBody TransactionRequest request) {
        final CommandResponse response = walletService.withdrawFunds(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Updates wallet using the given request parameters.
     *
     * @param request
     * @return id of the updated wallet wrapped by ResponseEntity<T>
     */
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_ACCOUNTANT', 'ROLE_CUSTOMER')")
    @PutMapping("/{id}")
    public ResponseEntity<CommandResponse>

    update(@PathVariable long id, @Valid @RequestBody WalletRequest request) {
        final CommandResponse response = walletService.update(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes wallet by the given id.
     *
     * @param id
     * @return ResponseEntity<Void>
     */
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_ACCOUNTANT', 'ROLE_CUSTOMER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void>

    deleteById(@PathVariable long id) {
        walletService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * Test endpoint to analyze IBAN (for debugging purposes).
     *
     * @param iban
     * @return BankInfo wrapped by ResponseEntity<T>
     */
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_ACCOUNTANT', 'ROLE_CUSTOMER')")
    @GetMapping("/test-iban/{iban}")
    public ResponseEntity<Map<String, String>> testIban(@PathVariable String iban) {
        Map<String, String> result = new HashMap<>();
        result.put("validation", walletService.getIbanAnalysisService().validateIban(iban));
        
        IbanAnalysisService.IbanAnalysisResult analysis = walletService.getIbanAnalysisService().analyzeIban(iban);
        result.put("analysis", analysis.getBankName() + " (" + analysis.getCountryName() + ")");
        result.put("bankName", analysis.getBankName());
        result.put("country", analysis.getCountryName());
        
        // Test with a valid Austrian IBAN for comparison
        String validAustrianIban = walletService.getIbanAnalysisService().createValidAustrianIban("32000");
        if (validAustrianIban != null) {
            result.put("validAustrianExample", validAustrianIban);
            IbanAnalysisService.IbanAnalysisResult validAnalysis = walletService.getIbanAnalysisService().analyzeIban(validAustrianIban);
            result.put("validAustrianAnalysis", validAnalysis.getBankName() + " (" + validAnalysis.getCountryName() + ")");
        }
        
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<Void> updateWalletStatus(@PathVariable long id, @RequestBody UpdateWalletStatusRequest request) {
        walletService.updateStatus(id, request.getStatus());
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole(T(com.hust.thailq.domain.enums.RoleType).ROLE_ADMIN)")
    @GetMapping("/search")
    public ResponseEntity<List<WalletResponse>> findByUsername(@RequestParam String username) {
        final List<WalletResponse> response = walletService.findByUsername(username);
        return ResponseEntity.ok(response);
    }
}
