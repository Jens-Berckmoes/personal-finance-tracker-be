package be.jensberckmoes.personal_finance_tracker.model;

import be.jensberckmoes.personal_finance_tracker.dto.UserDto;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class UserEntityMapper {
    public UserDto mapToDto(final User user){
        return UserDto.
                builder()
                .username(user.getUsername())
                .role(Objects.isNull(user.getRole()) ? "USER" : user.getRole().toString())
                .email(user.getEmail())
                .build();
    }
}
