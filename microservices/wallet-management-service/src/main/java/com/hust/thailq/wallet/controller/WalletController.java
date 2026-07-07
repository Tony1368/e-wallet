package com.hust.thailq.wallet.controller;

import com.hust.thailq.wallet.dto.request.UpdateWalletStatusRequest;
import com.hust.thailq.wallet.dto.request.WalletRequest;
import com.hust.thailq.wallet.dto.response.CommandResponse;
import com.hust.thailq.wallet.dto.response.WalletResponse;
import com.hust.thailq.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final RestTemplate restTemplate;

    @Value("${services.transaction.url:http://localhost:8083}")
    private String transactionServiceUrl;

    @GetMapping("/{id}")
    public ResponseEntity<WalletResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(walletService.findById(id));
    }

    @GetMapping("/iban/{iban}")
    public ResponseEntity<WalletResponse> findByIban(@PathVariable String iban) {
        return ResponseEntity.ok(walletService.findByIban(iban));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<WalletResponse>> findByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(walletService.findByUserId(userId));
    }

    @GetMapping
    public ResponseEntity<Page<WalletResponse>> findAll(Pageable pageable) {
        return ResponseEntity.ok(walletService.findAll(pageable));
    }

    @PostMapping
    public ResponseEntity<CommandResponse> create(@Valid @RequestBody WalletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(walletService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommandResponse> update(@PathVariable Long id, @Valid @RequestBody WalletRequest request) {
        return ResponseEntity.ok(walletService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        walletService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<Void> updateWalletStatus(@PathVariable Long id, @RequestBody UpdateWalletStatusRequest request) {
        walletService.updateStatus(id, request.getStatus());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/balance")
    public ResponseEntity<Void> updateBalance(@PathVariable Long id, @RequestBody Map<String, BigDecimal> body) {
        walletService.updateBalance(id, body.get("balance"));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/debit")
    public ResponseEntity<Void> debit(@PathVariable Long id, @RequestBody Map<String, BigDecimal> body) {
        walletService.debit(id, body.get("amount"));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/credit")
    public ResponseEntity<Void> credit(@PathVariable Long id, @RequestBody Map<String, BigDecimal> body) {
        walletService.credit(id, body.get("amount"));
        return ResponseEntity.noContent().build();
    }

    /**
     * Read balance directly from PostgreSQL (bypass Redis cache).
     * Used for Write-Behind Data Drift testing.
     */
    @GetMapping("/{id}/db-balance")
    public ResponseEntity<Map<String, Object>> getDbBalance(@PathVariable Long id) {
        return ResponseEntity.ok(walletService.getDbBalance(id));
    }

    /**
     * Read all wallet balances from both Redis and PostgreSQL.
     * Used for Write-Behind Data Drift testing.
     */
    @GetMapping("/drift-check")
    public ResponseEntity<Map<String, Object>> driftCheck() {
        return ResponseEntity.ok(walletService.checkDrift());
    }

    @PostMapping("/batch-credit")
    public ResponseEntity<Map<String, Object>> batchCredit(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            String filename = file.getOriginalFilename();
            List<String[]> rows = new java.util.ArrayList<>();

            if (filename != null && (filename.endsWith(".xlsx") || filename.endsWith(".xls"))) {
                // Parse Excel
                org.apache.poi.ss.usermodel.Workbook workbook = org.apache.poi.ss.usermodel.WorkbookFactory.create(file.getInputStream());
                org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
                    if (row == null) continue;
                    String iban = getCellString(row.getCell(0));
                    String amount = getCellString(row.getCell(1));
                    if (!iban.isEmpty() && !amount.isEmpty()) {
                        rows.add(new String[]{iban, amount});
                    }
                }
                workbook.close();
            } else {
                // Parse CSV
                java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(file.getInputStream()));
                reader.readLine(); // skip header
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 2) {
                        rows.add(new String[]{parts[0].trim(), parts[1].trim()});
                    }
                }
                reader.close();
            }

            int processed = 0;
            int failed = 0;
            for (String[] row : rows) {
                try {
                    WalletResponse wallet = walletService.findByIban(row[0]);
                    walletService.credit(wallet.getId(), new BigDecimal(row[1]));
                    // Create transaction record → triggers Kafka → accounting
                    try {
                        Map<String, Object> txBody = Map.of(
                                "amount", new BigDecimal(row[1]),
                                "description", "Cấp điểm batch",
                                "fromWalletId", wallet.getId(),
                                "toWalletId", wallet.getId(),
                                "typeId", 8
                        );
                        restTemplate.postForObject(transactionServiceUrl + "/api/v1/transactions", txBody, Map.class);
                    } catch (Exception ignored) {}
                    processed++;
                } catch (Exception e) {
                    failed++;
                }
            }
            return ResponseEntity.ok(Map.of(
                    "processedCount", processed,
                    "failedCount", failed,
                    "totalRows", rows.size(),
                    "message", "Batch credit completed"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error processing file: " + e.getMessage()));
        }
    }

    @GetMapping("/batch-credit/template")
    public void downloadTemplate(jakarta.servlet.http.HttpServletResponse response) throws Exception {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=batch_credit_template.xlsx");

        org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
        org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Cấp điểm");

        // Header row
        org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("IBAN");
        header.createCell(1).setCellValue("Số điểm");
        header.createCell(2).setCellValue("Ghi chú (không bắt buộc)");

        // Sample rows
        org.apache.poi.ss.usermodel.Row sample1 = sheet.createRow(1);
        sample1.createCell(0).setCellValue("GB33BUKB20201555555555");
        sample1.createCell(1).setCellValue(500000);
        sample1.createCell(2).setCellValue("Điểm ăn ca T6/2026");

        org.apache.poi.ss.usermodel.Row sample2 = sheet.createRow(2);
        sample2.createCell(0).setCellValue("GB94BARC10201530093459");
        sample2.createCell(1).setCellValue(500000);
        sample2.createCell(2).setCellValue("Điểm ăn ca T6/2026");

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(2);

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    private String getCellString(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue()).toPlainString();
            default -> "";
        };
    }
}
