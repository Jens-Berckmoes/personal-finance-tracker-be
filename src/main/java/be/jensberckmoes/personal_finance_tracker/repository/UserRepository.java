package be.jensberckmoes.personal_finance_tracker.repository;

import be.jensberckmoes.personal_finance_tracker.model.Role;
import be.jensberckmoes.personal_finance_tracker.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(final String username);
    boolean existsByEmail(final String email);
    boolean existsByUsername(final String username);
    List<User> findByRole(final Role role);

    Page<User> findByUsernameContaining(final String substring, final Pageable pageable);


}
