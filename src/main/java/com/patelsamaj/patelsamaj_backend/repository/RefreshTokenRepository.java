package com.patelsamaj.patelsamaj_backend.repository;

import com.patelsamaj.patelsamaj_backend.model.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Integer> {
    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);
}
