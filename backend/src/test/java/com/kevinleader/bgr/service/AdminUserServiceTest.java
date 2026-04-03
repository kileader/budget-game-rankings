package com.kevinleader.bgr.service;

import com.kevinleader.bgr.dto.admin.AdminUserDto;
import com.kevinleader.bgr.entity.AppUser;
import com.kevinleader.bgr.repository.AppUserRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.kevinleader.bgr.exception.NotFoundException;

class AdminUserServiceTest {

    private final AppUserRepository repository = mock(AppUserRepository.class);
    private final AdminUserService service = new AdminUserService(repository);

    private AppUser testUser() {
        AppUser user = new AppUser();
        user.setUsername("kevin");
        user.setEmail("kevin@example.com");
        user.setRole("USER");
        user.setActive(true);
        return user;
    }

    @Test
    void listUsersReturnsMappedDtos() {
        when(repository.findAll()).thenReturn(List.of(testUser()));

        List<AdminUserDto> result = service.listUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).username()).isEqualTo("kevin");
    }

    @Test
    void getUserThrowsWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUser(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void setActiveUpdatesUser() {
        AppUser user = testUser();
        when(repository.findById(2L)).thenReturn(Optional.of(user));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AdminUserDto result = service.setActive(1L, 2L, false);

        assertThat(result.active()).isFalse();
    }

    @Test
    void setActiveRejectsSelf() {
        assertThatThrownBy(() -> service.setActive(1L, 1L, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("own active status");
    }

    @Test
    void setRolePromotesToAdmin() {
        AppUser user = testUser();
        when(repository.findById(2L)).thenReturn(Optional.of(user));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AdminUserDto result = service.setRole(1L, 2L, "ADMIN");

        assertThat(result.role()).isEqualTo("ADMIN");
    }

    @Test
    void setRoleNormalizesToUppercase() {
        AppUser user = testUser();
        when(repository.findById(2L)).thenReturn(Optional.of(user));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AdminUserDto result = service.setRole(1L, 2L, "admin");

        assertThat(result.role()).isEqualTo("ADMIN");
    }

    @Test
    void setRoleRejectsSelf() {
        assertThatThrownBy(() -> service.setRole(1L, 1L, "USER"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("own role");
    }

    @Test
    void setRoleRejectsInvalidRole() {
        AppUser user = testUser();
        when(repository.findById(2L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.setRole(1L, 2L, "SUPERUSER"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid role");
    }
}
