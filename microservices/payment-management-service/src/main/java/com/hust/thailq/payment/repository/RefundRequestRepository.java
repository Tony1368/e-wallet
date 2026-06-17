package com.hust.thailq.payment.repository;

import com.hust.thailq.payment.domain.entity.RefundRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RefundRequestRepository extends JpaRepository<RefundRequest, Long> {
    List<RefundRequest> findByStatusOrderByCreatedAtDesc(String status);

    boolean existsByTransactionIdAndStatusIn(String transactionId, List<String> statuses);
}
