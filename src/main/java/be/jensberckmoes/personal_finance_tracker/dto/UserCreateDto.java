package be.jensberckmoes.personal_finance_tracker.dto;

import be.jensberckmoes.personal_finance_tracker.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserCreateDto {
    private String username;
    private String password;
    private String email;
    private Role role;
}