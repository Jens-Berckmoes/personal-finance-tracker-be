package be.jensberckmoes.personal_finance_tracker.service;

import be.jensberckmoes.personal_finance_tracker.dto.AppUserCreateDto;
import be.jensberckmoes.personal_finance_tracker.dto.AppUserDto;
import be.jensberckmoes.personal_finance_tracker.dto.AppUserUpdateDto;
import be.jensberckmoes.personal_finance_tracker.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AppUserService {
    AppUserDto createUser(final AppUserCreateDto user);

    AppUserDto getUserById(final Long id);

    AppUserDto findByUsername(final String username);

    Page<AppUserDto> getAllUsers(final Pageable pageable);

    List<AppUserDto> getUsersByRole(final Role role);

    Page<AppUserDto> getUsersByUsernameContains(final String substring, final Pageable pageable);

    AppUserDto updateUser(final Long id, final AppUserUpdateDto appUserUpdateDto);

    void deleteUser(final Long id);

    boolean usernameExists(final String username);

    boolean emailExists(final String email);

    boolean hasRole(final Long id, final Role role);

    AppUserDto updateUserRole(final Long id, final Role role);
}
