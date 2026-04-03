package com.kevinleader.bgr.service;

import com.kevinleader.bgr.dto.admin.AdminUserDto;
import com.kevinleader.bgr.entity.AppUser;
import com.kevinleader.bgr.repository.AppUserRepository;
import com.kevinleader.bgr.exception.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class AdminUserService {

    private static final Set<String> VALID_ROLES = Set.of("USER", "ADMIN");

    private final AppUserRepository appUserRepository;

    public AdminUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public List<AdminUserDto> listUsers() {
        return appUserRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public AdminUserDto getUser(Long id) {
        return toDto(findOrThrow(id));
    }

    public AdminUserDto setActive(Long callerId, Long id, boolean active) {
        if (callerId.equals(id)) {
            throw new IllegalArgumentException("Cannot change your own active status");
        }
        AppUser user = findOrThrow(id);
        user.setActive(active);
        return toDto(appUserRepository.save(user));
    }

    public AdminUserDto setRole(Long callerId, Long id, String role) {
        if (callerId.equals(id)) {
            throw new IllegalArgumentException("Cannot change your own role");
        }
        String normalized = role.toUpperCase();
        if (!VALID_ROLES.contains(normalized)) {
            throw new IllegalArgumentException("Invalid role: " + role + ". Must be USER or ADMIN");
        }
        AppUser user = findOrThrow(id);
        user.setRole(normalized);
        return toDto(appUserRepository.save(user));
    }

    private AppUser findOrThrow(Long id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private AdminUserDto toDto(AppUser user) {
        return new AdminUserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}
