package be.jensberckmoes.personal_finance_tracker.repository;

import be.jensberckmoes.personal_finance_tracker.model.User;

import java.util.Optional;

public interface UserRepository {
    User save(final User user);
    Optional<User> findByUsername(final String username);
}
