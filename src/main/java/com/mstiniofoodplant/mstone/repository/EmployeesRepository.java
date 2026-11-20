package com.mstiniofoodplant.mstone.repository;

import com.mstiniofoodplant.mstone.model.Employees;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmployeesRepository extends JpaRepository<Employees, UUID> {
    Optional<Employees> findByEmail(String email);
    Optional<Employees> findByEmailIgnoreCase(String email);
}
