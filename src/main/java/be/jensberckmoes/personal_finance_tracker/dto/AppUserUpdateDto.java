package be.jensberckmoes.personal_finance_tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class AppUserUpdateDto {
    private String username;
    private String email;
}
