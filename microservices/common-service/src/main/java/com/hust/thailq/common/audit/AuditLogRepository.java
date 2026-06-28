package com.hust.thailq.common.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByRequestIdOrderByTimestamp(String requestId);

    List<AuditLog> findByServiceNameAndTimestampBetween(String serviceName, Instant from, Instant to);

    List<AuditLog> findByUsernameOrderByTimestampDesc(String username);

    List<AuditLog> findByLevelAndTimestampAfter(String level, Instant after);
}
