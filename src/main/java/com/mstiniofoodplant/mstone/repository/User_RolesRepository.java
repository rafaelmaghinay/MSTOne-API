package com.mstiniofoodplant.mstone.repository;

import com.mstiniofoodplant.mstone.model.User_Roles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface User_RolesRepository extends JpaRepository<User_Roles,Long> {
    Optional<User_Roles> findByUserId(UUID userId);
    java.util.List<User_Roles> findAllByUserId(UUID userId);
    boolean existsByUserIdAndRoleId(UUID userId, Long roleId);
    void deleteByRoleId(Long roleId);
    void deleteByUserId(UUID userId);
    void deleteByUserIdAndRoleId(UUID userId, Long roleId);
}
