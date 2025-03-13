package be.jensberckmoes.personal_finance_tracker.model;

import be.jensberckmoes.personal_finance_tracker.dto.AppUserDto;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class AppUserEntityMapper {
    public AppUserDto mapToDto(final AppUser appUser){
        return AppUserDto.
                builder()
                .username(appUser.getUsername())
                .role(Objects.isNull(appUser.getRole()) ? "USER" : appUser.getRole().toString())
                .email(appUser.getEmail())
                .build();
    }
}
