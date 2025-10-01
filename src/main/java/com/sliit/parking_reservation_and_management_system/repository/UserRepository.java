package com.sliit.parking_reservation_and_management_system.repository;

import com.sliit.parking_reservation_and_management_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);// <-- fix here

    // New method: fetch paginated users
    Page<User> findAll(Pageable pageable);

    // Combined search (all filters are optional and combined with AND)
    @Query(
            "SELECT u FROM User u " +
                    "WHERE (:role IS NULL OR UPPER(u.role) = UPPER(:role)) " +
                    "AND (:status IS NULL OR UPPER(u.status) = UPPER(:status)) " +
                    "AND (:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')))"
    )
    Page<User> search(
            @Param("role") String role,
            @Param("status") String status,
            @Param("email") String email,
            Pageable pageable
    );
}
