package com.mstiniofoodplant.mstone.service;

import com.mstiniofoodplant.mstone.model.Users;
import com.mstiniofoodplant.mstone.model.Employees;
import com.mstiniofoodplant.mstone.repository.EmployeesRepository;
import com.mstiniofoodplant.mstone.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private EmployeesRepository employeesRepository;

    public Optional<Users> findByEmail(String email) {
        return usersRepository.findByEmail(email);
    }

    public Users createUser(String email, String username, String firstName, String lastName, String middleName, String passwordHash) {
        Employees employee = new Employees();
        employee.setEmployeeNumber("EMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setMiddleName(middleName);
        employee.setEmail(email);
        employee.setHireDate(LocalDate.now());
        employee.setEmploymentStatus("ACTIVE");
        employee.setCreatedAt(LocalDateTime.now());
        employee.setUpdatedAt(LocalDateTime.now());
        Employees savedEmployee = employeesRepository.save(employee);

        Users user = new Users();
        user.setEmployeeId(savedEmployee.getId());
        user.setEmail(email);
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setMiddleName(middleName);
        user.setPasswordHash(passwordHash);
        user.setActive(true);
        user.setLocked(false);
        user.setFailedLoginAttempts(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return usersRepository.save(user);
    }

    public Users save(Users user) {
        return usersRepository.save(user);
    }
}
