package be.jensberckmoes.personal_finance_tracker.repository;

import be.jensberckmoes.personal_finance_tracker.model.User;

public interface UserRepository {
    User save(User user);
    User findByUsername(String username);
}
