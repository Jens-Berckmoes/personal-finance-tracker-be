package be.jensberckmoes.personal_finance_tracker.repository;

import be.jensberckmoes.personal_finance_tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(final String username);
}
