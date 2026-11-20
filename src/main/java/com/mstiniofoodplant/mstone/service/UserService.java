package com.mstiniofoodplant.mstone.service;

import com.mstiniofoodplant.mstone.dto.UserResponse;
import com.mstiniofoodplant.mstone.dto.UserUpdateRequest;
import com.mstiniofoodplant.mstone.model.Roles;
import com.mstiniofoodplant.mstone.model.User_Roles;
import com.mstiniofoodplant.mstone.model.Users;
import com.mstiniofoodplant.mstone.repository.RolesRepository;
import com.mstiniofoodplant.mstone.repository.User_RolesRepository;
import com.mstiniofoodplant.mstone.repository.UsersRepository;
import com.mstiniofoodplant.mstone.security.CurrentUserResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UsersRepository usersRepository;
    private final User_RolesRepository userRolesRepository;
    private final RolesRepository rolesRepository;

    public UserService(
            UsersRepository usersRepository,
            User_RolesRepository userRolesRepository,
            RolesRepository rolesRepository) {
        this.usersRepository = usersRepository;
        this.userRolesRepository = userRolesRepository;
        this.rolesRepository = rolesRepository;
    }

    public Optional<Users> findByEmail(String email) {
        return usersRepository.findByEmail(email);
    }

    public Users createUser(String email, String username, String firstName, String lastName, String middleName, String passwordHash) {
        Users user = new Users();
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

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        List<Users> users = usersRepository.findAll();
        Map<Long, String> roleMap = rolesRepository.findAll().stream()
                .collect(Collectors.toMap(Roles::getId, Roles::getRoleName, (r1, r2) -> r1));

        Map<UUID, List<String>> userRolesMap = userRolesRepository.findAll().stream()
                .filter(ur -> ur.getRoleId() != null && roleMap.containsKey(ur.getRoleId()))
                .collect(Collectors.groupingBy(
                        User_Roles::getUserId,
                        Collectors.mapping(ur -> roleMap.get(ur.getRoleId()), Collectors.toList())));

        return users.stream()
                .map(user -> {
                    List<String> assignedRoles = userRolesMap.getOrDefault(user.getId(), List.of("Employee"));
                    String primaryRole = assignedRoles.stream()
                            .filter(CurrentUserResolver::isMatchingSuperUserRole)
                            .findFirst()
                            .orElse(assignedRoles.isEmpty() ? "Employee" : assignedRoles.get(0));
                    return toResponse(user, primaryRole, assignedRoles.isEmpty() ? List.of(primaryRole) : assignedRoles);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        Users user = usersRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        List<User_Roles> userRoles = userRolesRepository.findAllByUserId(user.getId());
        List<String> assignedRoles = userRoles.stream()
                .map(User_Roles::getRoleId)
                .filter(Objects::nonNull)
                .map(rolesRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Roles::getRoleName)
                .distinct()
                .collect(Collectors.toList());

        String primaryRole = assignedRoles.stream()
                .filter(CurrentUserResolver::isMatchingSuperUserRole)
                .findFirst()
                .orElse(assignedRoles.isEmpty() ? "Employee" : assignedRoles.get(0));

        return toResponse(user, primaryRole, assignedRoles.isEmpty() ? List.of(primaryRole) : assignedRoles);
    }

    @Transactional
    public UserResponse updateUser(UUID id, UserUpdateRequest request) {
        // Verify user exists - DO NOT modify the users table!
        if (!usersRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found: " + id);
        }

        // 1. Fetch current user_roles records for this user
        List<User_Roles> currentRoles = userRolesRepository.findAllByUserId(id);
        Set<Long> currentRoleIds = currentRoles.stream()
                .map(User_Roles::getRoleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // 2. Resolve requested role IDs from request
        Set<Long> requestedRoleIds = new LinkedHashSet<>();

        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            for (Long rId : request.getRoleIds()) {
                if (rId != null && rolesRepository.existsById(rId)) {
                    requestedRoleIds.add(rId);
                } else if (rId != null) {
                    throw new IllegalArgumentException("Role ID not found: " + rId);
                }
            }
        }

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            for (String rName : request.getRoles()) {
                if (rName != null && !rName.trim().isEmpty()) {
                    Roles resolvedRole = resolveRole(rName.trim());
                    requestedRoleIds.add(resolvedRole.getId());
                }
            }
        }

        if (request.getRoleId() != null) {
            if (rolesRepository.existsById(request.getRoleId())) {
                requestedRoleIds.add(request.getRoleId());
            } else {
                throw new IllegalArgumentException("Role ID not found: " + request.getRoleId());
            }
        }

        if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
            Roles resolvedRole = resolveRole(request.getRole().trim());
            requestedRoleIds.add(resolvedRole.getId());
        }

        if (request.getAddRole() != null && !request.getAddRole().trim().isEmpty()) {
            Roles resolvedRole = resolveRole(request.getAddRole().trim());
            requestedRoleIds.add(resolvedRole.getId());
        }

        if (request.getAddRoleId() != null) {
            if (rolesRepository.existsById(request.getAddRoleId())) {
                requestedRoleIds.add(request.getAddRoleId());
            } else {
                throw new IllegalArgumentException("Role ID not found: " + request.getAddRoleId());
            }
        }

        if (request.getAddRoles() != null && !request.getAddRoles().isEmpty()) {
            for (String rName : request.getAddRoles()) {
                if (rName != null && !rName.trim().isEmpty()) {
                    Roles resolvedRole = resolveRole(rName.trim());
                    requestedRoleIds.add(resolvedRole.getId());
                }
            }
        }

        if (request.getAddRoleIds() != null && !request.getAddRoleIds().isEmpty()) {
            for (Long rId : request.getAddRoleIds()) {
                if (rId != null && rolesRepository.existsById(rId)) {
                    requestedRoleIds.add(rId);
                } else if (rId != null) {
                    throw new IllegalArgumentException("Role ID not found: " + rId);
                }
            }
        }

        // If no role was specified, return current user profile
        if (requestedRoleIds.isEmpty()) {
            return getUserById(id);
        }

        // 3. Determine action: "change" vs "add"
        String action = request.getAction() != null ? request.getAction().trim().toLowerCase() : "";
        boolean isChangeMode = "change".equals(action) || "replace_single".equals(action) || request.getOldRole() != null || request.getOldRoleId() != null;

        if (isChangeMode) {
            // Mode: Change the role_id on the existing user_roles row
            Long targetNewRoleId = requestedRoleIds.iterator().next();

            Long targetOldRoleId = null;
            if (request.getOldRoleId() != null) {
                targetOldRoleId = request.getOldRoleId();
            } else if (request.getOldRole() != null && !request.getOldRole().trim().isEmpty()) {
                targetOldRoleId = resolveRole(request.getOldRole().trim()).getId();
            } else if (!currentRoles.isEmpty()) {
                targetOldRoleId = currentRoles.get(0).getRoleId();
            }

            if (targetOldRoleId != null && !targetNewRoleId.equals(targetOldRoleId)) {
                if (currentRoleIds.contains(targetNewRoleId)) {
                    // User already has the new role, delete the old role row
                    for (User_Roles ur : currentRoles) {
                        if (targetOldRoleId.equals(ur.getRoleId())) {
                            userRolesRepository.delete(ur);
                        }
                    }
                } else {
                    // Update role_id in place on user_roles row
                    boolean updated = false;
                    for (User_Roles ur : currentRoles) {
                        if (targetOldRoleId.equals(ur.getRoleId())) {
                            ur.setRoleId(targetNewRoleId);
                            userRolesRepository.save(ur);
                            updated = true;
                            break;
                        }
                    }
                    if (!updated) {
                        User_Roles newUr = new User_Roles();
                        newUr.setUserId(id);
                        newUr.setRoleId(targetNewRoleId);
                        userRolesRepository.save(newUr);
                    }
                }
                userRolesRepository.flush();
            }
        } else {
            // Mode: Add another row on user_roles so 1 user can have multiple roles
            // Only insert roles that are NOT already present in currentRoleIds
            for (Long newRoleId : requestedRoleIds) {
                if (!currentRoleIds.contains(newRoleId)) {
                    User_Roles newUr = new User_Roles();
                    newUr.setUserId(id);
                    newUr.setRoleId(newRoleId);
                    userRolesRepository.save(newUr);
                    currentRoleIds.add(newRoleId);
                }
            }
            userRolesRepository.flush();
        }

        return getUserById(id);
    }

    private Roles resolveRole(String roleName) {
        return rolesRepository.findByRoleNameIgnoreCase(roleName)
                .or(() -> rolesRepository.findByRoleName(roleName))
                .or(() -> {
                    List<Roles> allRoles = rolesRepository.findAll();
                    String normalizedInput = roleName.trim().toLowerCase().replaceAll("[\\s_\\-]+", "");
                    for (Roles r : allRoles) {
                        String normalizedDb = r.getRoleName().trim().toLowerCase().replaceAll("[\\s_\\-]+", "");
                        if (normalizedDb.equals(normalizedInput)) {
                            return Optional.of(r);
                        }
                    }
                    return Optional.empty();
                })
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
    }

    @Transactional
    public void deleteUser(String idOrUsername) {
        Users user = null;
        try {
            UUID id = UUID.fromString(idOrUsername);
            user = usersRepository.findById(id).orElse(null);
        } catch (IllegalArgumentException ignored) {
            // not a valid UUID, search by username
        }

        if (user == null) {
            user = usersRepository.findByUsername(idOrUsername)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
        }

        userRolesRepository.deleteByUserId(user.getId());
        usersRepository.delete(user);
    }

    private UserResponse toResponse(Users user, String roleName) {
        return toResponse(user, roleName, List.of(roleName));
    }

    private UserResponse toResponse(Users user, String roleName, List<String> roles) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getMiddleName(),
                user.getLastName(),
                roleName,
                roles,
                user.isActive(),
                user.isLocked(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
