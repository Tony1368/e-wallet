package com.hust.thailq.repository;

import com.hust.thailq.domain.entity.UserActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {

    Optional<UserActivity> findByActivityId(String activityId);

    List<UserActivity> findByUserIdOrderByActivityTimeDesc(Long userId);

    List<UserActivity> findByUserIdAndActivityTypeOrderByActivityTimeDesc(Long userId, String activityType);

    @Query("SELECT ua FROM UserActivity ua WHERE ua.user.id = :userId AND ua.activityTime >= :startTime ORDER BY ua.activityTime DESC")
    List<UserActivity> findByUserIdAndActivityTimeAfter(@Param("userId") Long userId, @Param("startTime") Instant startTime);

    @Query("SELECT ua FROM UserActivity ua WHERE ua.user.id = :userId AND ua.activityTime BETWEEN :startTime AND :endTime ORDER BY ua.activityTime DESC")
    List<UserActivity> findByUserIdAndActivityTimeBetween(@Param("userId") Long userId, @Param("startTime") Instant startTime, @Param("endTime") Instant endTime);

    @Query("SELECT ua FROM UserActivity ua WHERE ua.user.id = :userId AND ua.activityType IN :activityTypes ORDER BY ua.activityTime DESC")
    List<UserActivity> findByUserIdAndActivityTypeIn(@Param("userId") Long userId, @Param("activityTypes") List<String> activityTypes);

    Page<UserActivity> findByUserId(Long userId, Pageable pageable);

    Page<UserActivity> findByUserUsername(String username, Pageable pageable);

    Page<UserActivity> findByUserIdAndActivityType(Long userId, String activityType, Pageable pageable);

    @Query("SELECT ua FROM UserActivity ua WHERE ua.activityType IN ('TRANSFER', 'WITHDRAW', 'ADD_FUNDS') ORDER BY ua.activityTime DESC")
    Page<UserActivity> findAllFinancialActivities(Pageable pageable);

    @Query("SELECT ua FROM UserActivity ua WHERE ua.user.id = :userId AND ua.activityType IN ('TRANSFER', 'WITHDRAW', 'ADD_FUNDS') ORDER BY ua.activityTime DESC")
    Page<UserActivity> findFinancialActivitiesByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT ua FROM UserActivity ua ORDER BY ua.activityTime DESC")
    Page<UserActivity> findAllOrderByActivityTimeDesc(Pageable pageable);

    @Query("SELECT ua FROM UserActivity ua WHERE ua.user.id = :userId ORDER BY ua.activityTime DESC")
    Page<UserActivity> findByUserIdOrderByActivityTimeDesc(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT ua FROM UserActivity ua WHERE ua.user.username = :username ORDER BY ua.activityTime DESC")
    Page<UserActivity> findByUserUsernameOrderByActivityTimeDesc(@Param("username") String username, Pageable pageable);

    @Query(value = "SELECT * FROM user_activity ua WHERE ua.from_wallet_iban = :fromWalletIban AND ua.to_wallet_iban = :toWalletIban AND ua.amount = :amount AND ABS(EXTRACT(EPOCH FROM (ua.activity_time - :transactionTime))) < 300 ORDER BY ua.activity_time DESC", nativeQuery = true)
    List<UserActivity> findByTransactionDetails(@Param("fromWalletIban") String fromWalletIban, @Param("toWalletIban") String toWalletIban, @Param("amount") BigDecimal amount, @Param("transactionTime") Instant transactionTime);
} 