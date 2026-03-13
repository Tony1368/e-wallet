package com.hust.thailq.user.repository;

import com.hust.thailq.user.domain.entity.UserSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findBySessionId(String sessionId);

    List<UserSession> findByUserIdOrderByLoginTimeDesc(Long userId);

    List<UserSession> findByUserUsernameOrderByLoginTimeDesc(String username);

    List<UserSession> findByUserIdAndIsActiveTrue(Long userId);

    @Query("SELECT us FROM UserSession us WHERE us.user.id = :userId AND us.loginTime >= :startTime ORDER BY us.loginTime DESC")
    List<UserSession> findByUserIdAndLoginTimeAfter(@Param("userId") Long userId, @Param("startTime") Instant startTime);

    @Query("SELECT us FROM UserSession us WHERE us.user.id = :userId AND us.loginTime BETWEEN :startTime AND :endTime ORDER BY us.loginTime DESC")
    List<UserSession> findByUserIdAndLoginTimeBetween(@Param("userId") Long userId, @Param("startTime") Instant startTime, @Param("endTime") Instant endTime);

    Page<UserSession> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT us FROM UserSession us WHERE us.isActive = true ORDER BY us.loginTime DESC")
    Page<UserSession> findAllActiveSessions(Pageable pageable);

    @Query("SELECT us FROM UserSession us ORDER BY us.loginTime DESC")
    Page<UserSession> findAllOrderByLoginTimeDesc(Pageable pageable);

    @Modifying
    @Query(value = "UPDATE user_session SET logout_time = CURRENT_TIMESTAMP, is_active = false WHERE session_id = :sessionId", nativeQuery = true)
    void endSessionNow(@Param("sessionId") String sessionId);
} 