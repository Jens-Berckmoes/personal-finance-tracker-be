package be.jensberckmoes.personal_finance_tracker.repository;

import be.jensberckmoes.personal_finance_tracker.model.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {
    public User save(User user) {
        return null;
    }

    public Optional<User> findByUsername(String username) {
        return null;
    }
}
