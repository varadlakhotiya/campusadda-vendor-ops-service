package com.campusadda.vendorops.auth.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.campusadda.vendorops.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUser_IdAndRevokedFalse(Long userId);

    void deleteByExpiryAtBefore(LocalDateTime dateTime);
}