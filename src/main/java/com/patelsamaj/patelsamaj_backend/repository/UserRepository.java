package com.patelsamaj.patelsamaj_backend.repository;

import com.patelsamaj.patelsamaj_backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByMobileNumberAndIsActiveTrue(String mobileNumber);
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailOrMobileNumber(String email,String mobileNumber);

    @Query("""
        SELECT u
        FROM User u
        WHERE u.role.id = :roleId
        AND u.userId > 3
        AND (
            :search IS NULL
            OR :search = ''
            OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
        )""")
    Page<User> findByRoleIdAndSearch(@Param("roleId") Integer roleId, @Param("search") String search, Pageable pageable);
}
