package be.jensberckmoes.personal_finance_tracker.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "app_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters.")
    @Column(nullable = false, length = 20)
    @Pattern(regexp = "^[a-zA-Z0-9._]+$", message = "Username can only contain letters, numbers, dots, or underscores.")
    private String username;
    @Size(min = 12, max = 64, message = "Password must be between 12 and 64 characters.")
    @Column(nullable = false, length = 64)
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!.*_-])[A-Za-z\\d!.*_-]+$", message = "Password must contain lower case letter(s), uppercase letter(s), number(s) and special character(s) (!.*_-).")
    private String password;
    @Size(min = 2, max = 254, message = "E-mail must be between 2 and 254 characters.")
    @Column(nullable = false, length = 254)
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]+$", message = "E-mail must be in following format: (something@test.com)")
    private String email;
}
