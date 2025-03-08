package be.jensberckmoes.personal_finance_tracker.model;

import be.jensberckmoes.personal_finance_tracker.dto.UserDto;

import java.util.Objects;

public class UserEntityMapper {
    public static UserDto mapToDto(final User user){
        return UserDto.
                builder()
                .username(user.getUsername())
                .role(Objects.isNull(user.getRole()) ? "USER" : user.getRole().toString())
                .email(user.getEmail())
                .build();
    }
}
