package be.jensberckmoes.personal_finance_tracker.service;

import be.jensberckmoes.personal_finance_tracker.dto.UserCreateDto;
import be.jensberckmoes.personal_finance_tracker.dto.UserDto;
import be.jensberckmoes.personal_finance_tracker.dto.UserUpdateDto;
import be.jensberckmoes.personal_finance_tracker.model.Role;

import java.util.List;

public interface UserService {
    UserDto createUser(final UserCreateDto user);

    UserDto getUserById(final Long id);

    UserDto findByUsername(final String username);

    List<UserDto> getAllUsers();

    List<UserDto> getUsersByRole(final Role role);

    List<UserDto> getUsersByUsernameContains(final String keyword);

    UserDto updateUser(final Long id, final UserUpdateDto userUpdateDto);

    void deleteUser(final Long id);

    boolean usernameExists(final String username);

    boolean emailExists(final String email);

    boolean hasRole(final Long userId, final Role role);
}
