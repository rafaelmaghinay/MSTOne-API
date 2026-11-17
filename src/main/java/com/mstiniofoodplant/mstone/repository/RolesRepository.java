package com.mstiniofoodplant.mstone.repository;

import com.mstiniofoodplant.mstone.model.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolesRepository extends JpaRepository<Roles, Long> {
    Optional<Roles> findByRoleName(String roleName);
    List<Roles> findAllByOrderByRoleNameAsc();
}
