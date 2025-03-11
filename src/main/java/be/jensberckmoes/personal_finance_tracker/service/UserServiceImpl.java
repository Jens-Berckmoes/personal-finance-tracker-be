package be.jensberckmoes.personal_finance_tracker.service;

import be.jensberckmoes.personal_finance_tracker.dto.UserCreateDto;
import be.jensberckmoes.personal_finance_tracker.dto.UserDto;
import be.jensberckmoes.personal_finance_tracker.dto.UserUpdateDto;
import be.jensberckmoes.personal_finance_tracker.exception.InvalidRoleException;
import be.jensberckmoes.personal_finance_tracker.exception.InvalidUserException;
import be.jensberckmoes.personal_finance_tracker.model.Role;
import be.jensberckmoes.personal_finance_tracker.model.User;
import be.jensberckmoes.personal_finance_tracker.model.UserEntityMapper;
import be.jensberckmoes.personal_finance_tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static be.jensberckmoes.personal_finance_tracker.model.UserEntityMapper.mapToDto;

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
        if (Objects.isNull(userCreateDto)) {
            throw new InvalidUserException("Username is invalid");
        }
        if (userRepository.findByUsername(userCreateDto.getUsername()).isPresent()) {
            throw new InvalidUserException("Username already taken");
        }
        if (!validationService.isValidUsername(userCreateDto.getUsername())) {
            throw new InvalidUserException("Username is invalid");
        }
        if (!validationService.isValidPassword(userCreateDto.getPassword())) {
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

        return mapToDto(savedUser);
    }

    @Override
    public UserDto getUserById(final Long id) {
        if (Objects.isNull(id) || id <= 0) {
            throw new InvalidUserException("User ID is invalid");
        }
        final User foundUser = userRepository.findById(id).orElseThrow(() -> new InvalidUserException("Username does not exist"));
        return mapToDto(foundUser);
    }

    public UserDto findByUsername(final String username) {
        if (Objects.isNull(username) || username.isBlank()) {
            throw new InvalidUserException("Username is invalid");
        }
        final String lowerCaseUserName = username.toLowerCase();
        if (!validationService.isValidUsername(lowerCaseUserName)) {
            throw new InvalidUserException("Username is invalid");
        }
        final User foundUser = userRepository.findByUsername(username).orElseThrow(() -> new InvalidUserException("Username does not exist"));
        return mapToDto(foundUser);
    }

    @Override
    public List<UserDto> getAllUsers() {
        final List<User> users = userRepository.findAll();
        return users.stream().map(UserEntityMapper::mapToDto).toList();
    }

    @Override
    public List<UserDto> getUsersByRole(final Role role) {
        if(Objects.isNull(role)) throw new InvalidRoleException("Role cannot be null");
        final List<User> users = userRepository.findByRole(role);
        return users.stream().map(UserEntityMapper::mapToDto).toList();
    }

    @Override
    public Page<UserDto> getUsersByUsernameContains(final String substring, final Pageable pageable) {
        Page<User> usersPage = userRepository.findByUsernameContaining(substring, pageable);
        return usersPage.map(UserEntityMapper::mapToDto);
    }


    @Override
    public UserDto updateUser(final Long id,
                              final UserUpdateDto userUpdateDto) {
        return null;
    }

    @Override
    public void deleteUser(final Long id) {

    }

    @Override
    public boolean usernameExists(final String username) {
        return false;
    }

    @Override
    public boolean emailExists(final String email) {
        return false;
    }

    @Override
    public boolean hasRole(final Long userId,
                           final Role role) {
        return false;
    }

}