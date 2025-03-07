package be.jensberckmoes.personal_finance_tracker.service;

import be.jensberckmoes.personal_finance_tracker.dto.UserCreateDto;
import be.jensberckmoes.personal_finance_tracker.dto.UserDto;
import be.jensberckmoes.personal_finance_tracker.model.User;

import java.util.Optional;

public interface UserService {
    User register(final User user);
    UserDto createUser(final UserCreateDto user);
    Optional<User> findByUsername(final String username);
}
