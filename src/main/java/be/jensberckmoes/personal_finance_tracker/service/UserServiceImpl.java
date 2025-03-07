package be.jensberckmoes.personal_finance_tracker.service;

import be.jensberckmoes.personal_finance_tracker.dto.UserCreateDto;
import be.jensberckmoes.personal_finance_tracker.dto.UserDto;
import be.jensberckmoes.personal_finance_tracker.exception.InvalidUserException;
import be.jensberckmoes.personal_finance_tracker.model.Role;
import be.jensberckmoes.personal_finance_tracker.model.User;
import be.jensberckmoes.personal_finance_tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HashingService hashingService;

    @Autowired
    private ValidationService validationService;


    @Override
    public UserDto createUser(final UserCreateDto userCreateDto) {
        if(Objects.isNull(userCreateDto)){
            throw new InvalidUserException("Username is invalid");
        }
        if(findByUsername(userCreateDto.getUsername()).isPresent()){
            throw new InvalidUserException("Username already taken");
        }
        if(!validationService.isValidPassword(userCreateDto.getPassword())){
            throw new InvalidUserException("User has invalid password. Password should be between 12-255 characters long, should contain 1 uppercase, 1 lowercase, 1 number and 1 special character(!.*_-).");
        }
        if (!validationService.isValidEmail(userCreateDto.getEmail())) {
            throw new InvalidUserException("User has invalid email. Email should be in the form (test@example.com).");
        }
        final User user = User.builder()
                .username(userCreateDto.getUsername())
                .password(hashingService.hashPassword(userCreateDto.getPassword())) // Hash password
                .email(userCreateDto.getEmail())
                .role(Objects.isNull(userCreateDto.getRole()) ? Role.USER : userCreateDto.getRole()) // Default to USER
                .build();
        final User savedUser = userRepository.save(user);

        return UserDto.
                builder()
                .username(savedUser.getUsername())
                .role(savedUser.getRole().toString())
                .email(savedUser.getEmail())
                .build();
    }

    public Optional<User> findByUsername(final String username) {
        if(Objects.isNull(username) || username.isBlank()){
            throw new InvalidUserException("Username is invalid");
        }
        final String lowerCaseUserName = username.toLowerCase();
        if (!validationService.isValidUsername(lowerCaseUserName)) {
            throw new InvalidUserException("Username is invalid");
        }
        return userRepository.findByUsername(username);
    }

}