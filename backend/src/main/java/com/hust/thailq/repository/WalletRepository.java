package com.hust.thailq.repository;

import com.hust.thailq.domain.entity.Wallet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByIban(String iban);

    List<Wallet> findByUserId(Long userId);

    @Query("SELECT w FROM Wallet w JOIN FETCH w.user WHERE w.user.id = :userId")
    List<Wallet> findByUserIdWithUser(@Param("userId") Long userId);

    boolean existsByIbanIgnoreCase(String iban);

    boolean existsByUserIdAndNameIgnoreCase(Long userId, String name);

    Wallet getReferenceByIban(String iban);

    @Query("SELECT w FROM Wallet w JOIN FETCH w.user u WHERE LOWER(u.username) = LOWER(:username)")
    List<Wallet> findByUserUsernameIgnoreCase(@Param("username") String username);
}
