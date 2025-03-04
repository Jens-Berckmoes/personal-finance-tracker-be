package be.jensberckmoes.personal_finance_tracker.service;

import be.jensberckmoes.personal_finance_tracker.model.User;

import java.util.Optional;

public interface UserService {
    User register(final User user);
    Optional<User> findByUsername(final String username);
}
