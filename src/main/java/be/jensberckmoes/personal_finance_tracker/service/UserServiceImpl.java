package be.jensberckmoes.personal_finance_tracker.service;

import be.jensberckmoes.personal_finance_tracker.dto.UserCreateDto;
import be.jensberckmoes.personal_finance_tracker.dto.UserDto;
import be.jensberckmoes.personal_finance_tracker.dto.UserUpdateDto;
import be.jensberckmoes.personal_finance_tracker.exception.*;
import be.jensberckmoes.personal_finance_tracker.model.Role;
import be.jensberckmoes.personal_finance_tracker.model.User;
import be.jensberckmoes.personal_finance_tracker.model.UserEntityMapper;
import be.jensberckmoes.personal_finance_tracker.repository.UserRepository;
import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HashingService hashingService;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private UserEntityMapper userEntityMapper;

    @Override
    public UserDto createUser(final UserCreateDto userCreateDto) {
        if (Objects.isNull(userCreateDto)) {
            throw new NullParameterException("Parameter 'userUpdateDto' cannot be null");
        }
        if (userRepository.existsByUsername(userCreateDto.getUsername())) {
            throw new DuplicateUsernameException("Username already taken");
        }
        if (!validationService.isValidUsername(userCreateDto.getUsername())) {
            throw new InvalidUserException("Username is invalid");
        }
        if (!validationService.isValidPassword(userCreateDto.getPassword())) {
            throw new InvalidPasswordException("User has invalid password. Password should be between 12-255 characters long, should contain 1 uppercase, 1 lowercase, 1 number and 1 special character(!.*_-).");
        }
        if (!validationService.isValidEmail(userCreateDto.getEmail())) {
            throw new InvalidEmailException("User has invalid email. Email should be in the form (test@example.com).");
        }
        final User user = User.builder()
                .username(userCreateDto.getUsername())
                .password(hashingService.hashPassword(userCreateDto.getPassword())) // Hash password
                .email(userCreateDto.getEmail())
                .role(Objects.isNull(userCreateDto.getRole()) ? Role.USER : userCreateDto.getRole()) // Default to USER
                .build();
        final User savedUser = userRepository.save(user);

        return userEntityMapper.mapToDto(savedUser);
    }

    @Override
    public UserDto getUserById(final Long id) {
        if (Objects.isNull(id) || id <= 0) {
            throw new InvalidUserIDException("User ID is invalid");
        }
        final User foundUser = userRepository.findById(id).orElseThrow(() -> new InvalidUserException("Username does not exist"));
        return userEntityMapper.mapToDto(foundUser);
    }

    public UserDto findByUsername(final String username) {
        if (Objects.isNull(username)) {
            throw new NullParameterException("Parameter 'username' cannot be null");
        }
        if(username.isBlank()){
            throw new BlankParameterException("Parameter 'username' cannot be blank");
        }
        final String lowerCaseUserName = username.toLowerCase();
        if (!validationService.isValidUsername(lowerCaseUserName)) {
            throw new InvalidUserException("Username is invalid");
        }
        final User foundUser = userRepository.findByUsername(username).orElseThrow(() -> new InvalidUserException("Username does not exist"));
        return userEntityMapper.mapToDto(foundUser);
    }

    @Override
    public List<UserDto> getAllUsers() {
        final List<User> users = userRepository.findAll();
        return users.stream().map(i -> userEntityMapper.mapToDto(i)).toList();
    }

    @Override
    public List<UserDto> getUsersByRole(final Role role) {
        if (Objects.isNull(role)){
            throw new InvalidRoleException("Role cannot be null");
        }
        final List<User> users = userRepository.findByRole(role);
        return users.stream().map(i -> userEntityMapper.mapToDto(i)).toList();
    }

    @Override
    public Page<UserDto> getUsersByUsernameContains(final String substring, final Pageable pageable) {
        if (Objects.isNull(substring)) {
            throw new NullParameterException("Parameter 'username' cannot be null");
        }
        final Page<User> usersPage = userRepository.findByUsernameContaining(substring, pageable);
        return usersPage.map(i -> userEntityMapper.mapToDto(i));
    }

    @Override
    public UserDto updateUser(final Long id,
                              final UserUpdateDto userUpdateDto) {
        if(Objects.isNull(id) || Objects.isNull(userUpdateDto)){
            throw new NullParameterException("Parameters 'id' and 'userUpdateDto' cannot be null");
        }
        if (!validationService.isValidEmail(userUpdateDto.getEmail())) {
            throw new InvalidEmailException("Invalid email format");
        }
        if (userRepository.existsByEmail(userUpdateDto.getEmail())) {
            throw new DuplicateEmailException("Email already in use");
        }

        final User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        updateFields(existingUser, userUpdateDto);

        final User updatedUser = userRepository.save(existingUser);

        return userEntityMapper.mapToDto(updatedUser);
    }

    @Override
    public void deleteUser(final Long id) {
        if(Objects.isNull(id)){
            throw new NullParameterException("Parameter 'id' cannot be null");
        }
        userRepository.deleteById(id);
    }

    @Override
    public boolean usernameExists(final String username) {
        if(Objects.isNull(username)){
            throw new NullParameterException("Parameter 'id' cannot be null");
        }
        if(username.isBlank()){
            throw new BlankParameterException("Parameter 'username' cannot be blank");
        }
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean emailExists(final String email) {
        if(Objects.isNull(email)){
            throw new NullParameterException("Parameter 'id' cannot be null");
        }
        if(email.isBlank()){
            throw new BlankParameterException("Parameter 'username' cannot be blank");
        }
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean hasRole(final Long userId,
                           final Role role) {
        return false;
    }

    private <T> void updateFieldIfNotNull(final Consumer<T> setter, final T value) {
        if (Objects.nonNull(value)) {
            setter.accept(value);
        }
    }

    private void updateFields(final User fromExistingUser, final UserUpdateDto toUserUpdateDto) {
        updateFieldIfNotNull(fromExistingUser::setUsername, toUserUpdateDto.getUsername());
        updateFieldIfNotNull(fromExistingUser::setEmail, toUserUpdateDto.getEmail());
    }
}