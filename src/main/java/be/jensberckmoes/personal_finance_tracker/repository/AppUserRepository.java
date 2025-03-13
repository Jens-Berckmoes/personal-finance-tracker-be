package be.jensberckmoes.personal_finance_tracker.repository;

import be.jensberckmoes.personal_finance_tracker.model.AppUser;
import be.jensberckmoes.personal_finance_tracker.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(final String username);
    boolean existsByEmail(final String email);
    boolean existsByUsername(final String username);
    boolean findByIdAndRole(final Long id, final Role role);
    List<AppUser> findByRole(final Role role);

    Page<AppUser> findByUsernameContaining(final String substring, final Pageable pageable);


}
