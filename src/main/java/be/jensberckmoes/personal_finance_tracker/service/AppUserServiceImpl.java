package be.jensberckmoes.personal_finance_tracker.service;

import be.jensberckmoes.personal_finance_tracker.dto.AppUserCreateDto;
import be.jensberckmoes.personal_finance_tracker.dto.AppUserDto;
import be.jensberckmoes.personal_finance_tracker.dto.AppUserUpdateDto;
import be.jensberckmoes.personal_finance_tracker.exception.*;
import be.jensberckmoes.personal_finance_tracker.model.AppUser;
import be.jensberckmoes.personal_finance_tracker.model.AppUserEntityMapper;
import be.jensberckmoes.personal_finance_tracker.model.Role;
import be.jensberckmoes.personal_finance_tracker.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class AppUserServiceImpl implements AppUserService {
    private final AppUserRepository appUserRepository;
    private final HashingService hashingService;
    private final ValidationService validationService;
    private final AppUserEntityMapper appUserEntityMapper;

    @Override
    public AppUserDto createUser(final AppUserCreateDto appUserCreateDto) {
        try {
            if (Objects.isNull(appUserCreateDto)) {
                throw new NullParameterException("Parameter 'userUpdateDto' cannot be null");
            }
            if (appUserRepository.existsByUsername(appUserCreateDto.getUsername())) {
                throw new DuplicateUsernameException("Username already taken");
            }
            if (!validationService.isValidUsername(appUserCreateDto.getUsername())) {
                throw new InvalidAppUserException("Username is invalid");
            }
            if (!validationService.isValidPassword(appUserCreateDto.getPassword())) {
                throw new InvalidPasswordException("User has invalid password. Password should be between 12-255 characters long, should contain 1 uppercase, 1 lowercase, 1 number and 1 special character(!.*_-).");
            }
            if (!validationService.isValidEmail(appUserCreateDto.getEmail())) {
                throw new InvalidEmailException("User has invalid email. Email should be in the form (test@example.com).");
            }
            final AppUser appUser = AppUser.builder()
                    .username(appUserCreateDto.getUsername())
                    .password(hashingService.hashPassword(appUserCreateDto.getPassword()))
                    .email(appUserCreateDto.getEmail())
                    .build();
            final AppUser savedAppUser = appUserRepository.save(appUser);

            return appUserEntityMapper.mapToDto(savedAppUser);
        } catch (DataAccessException ex){
            throw new ServiceException("An unexpected error occurred while creating a user", ex);
        }

    }

    @Override
    public AppUserDto getUserById(final Long id) {
        if (Objects.isNull(id) || id <= 0) {
            throw new InvalidAppUserIDException("ID must be a positive number");
        }
        final AppUser foundAppUser = appUserRepository.findById(id).orElseThrow(() -> new InvalidAppUserException("Username does not exist"));
        return appUserEntityMapper.mapToDto(foundAppUser);
    }

    @Override
    public AppUserDto findByUsername(final String username) {
        if (Objects.isNull(username)) {
            throw new NullParameterException("Parameter 'username' cannot be null");
        }
        if(username.isBlank()){
            throw new BlankParameterException("Parameter 'username' cannot be blank");
        }
        final String lowerCaseUserName = username.toLowerCase();
        if (!validationService.isValidUsername(lowerCaseUserName)) {
            throw new InvalidAppUserException("Username is invalid");
        }
        final AppUser foundAppUser = appUserRepository.findByUsername(username).orElseThrow(() -> new InvalidAppUserException("Username does not exist"));
        return appUserEntityMapper.mapToDto(foundAppUser);
    }

    @Override
    public Page<AppUserDto> getAllUsers(final Pageable pageable) {
        final Page<AppUser> usersPage = appUserRepository.findAll(pageable);
        return usersPage.map(appUserEntityMapper::mapToDto);
    }

    @Override
    public List<AppUserDto> getUsersByRole(final Role role) {
        if (Objects.isNull(role)){
            throw new NullParameterException("Role cannot be null");
        }
        final List<AppUser> appUsers = appUserRepository.findByRole(role);
        return appUsers.stream().map(appUserEntityMapper::mapToDto).toList();
    }

    @Override
    public Page<AppUserDto> getUsersByUsernameContains(final String substring, final Pageable pageable) {
        if (Objects.isNull(substring)) {
            throw new NullParameterException("Parameter 'username' cannot be null");
        }
        final Page<AppUser> usersPage = appUserRepository.findByUsernameContaining(substring, pageable);
        return usersPage.map(appUserEntityMapper::mapToDto);
    }

    @Override
    public AppUserDto updateUser(final Long id,
                                 final AppUserUpdateDto appUserUpdateDto) {
        if(Objects.isNull(id) || Objects.isNull(appUserUpdateDto)){
            throw new NullParameterException("Parameters 'id' and 'userUpdateDto' cannot be null");
        }
        if (!validationService.isValidEmail(appUserUpdateDto.getEmail())) {
            throw new InvalidEmailException("Invalid email format");
        }
        if (appUserRepository.existsByEmail(appUserUpdateDto.getEmail())) {
            throw new DuplicateEmailException("Email already in use");
        }

        final AppUser existingAppUser = appUserRepository.findById(id)
                .orElseThrow(() -> new AppUserNotFoundException("User not found"));

        updateFields(existingAppUser, appUserUpdateDto);

        final AppUser updatedAppUser = appUserRepository.save(existingAppUser);

        return appUserEntityMapper.mapToDto(updatedAppUser);
    }

    @Override
    public void deleteUser(final Long id) {
        if (Objects.isNull(id) || id <= 0) {
            throw new InvalidAppUserIDException("ID must be a positive number");
        }
        appUserRepository.deleteById(id);
    }

    @Override
    public boolean usernameExists(final String username) {
        if(Objects.isNull(username)){
            throw new NullParameterException("Parameter 'id' cannot be null");
        }
        if(username.isBlank()){
            throw new BlankParameterException("Parameter 'username' cannot be blank");
        }
        return appUserRepository.existsByUsername(username);
    }

    @Override
    public boolean emailExists(final String email) {
        if(Objects.isNull(email)){
            throw new NullParameterException("Parameter 'id' cannot be null");
        }
        if(email.isBlank()){
            throw new BlankParameterException("Parameter 'username' cannot be blank");
        }
        return appUserRepository.existsByEmail(email);
    }

    @Override
    public boolean hasRole(final Long id, final Role role) {
        if (Objects.isNull(id) || id <= 0) {
            throw new InvalidAppUserIDException("ID must be a positive number");
        }
        if (Objects.isNull(role)) {
            throw new NullParameterException("Role cannot be null");
        }
        return appUserRepository.findByIdAndRole(id, role);
    }

    //TODO: add updateUserRole method


    private <T> void updateFieldIfNotNull(final Consumer<T> setter, final T value) {
        if (Objects.nonNull(value)) {
            setter.accept(value);
        }
    }

    private void updateFields(final AppUser fromExistingAppUser, final AppUserUpdateDto toAppUserUpdateDto) {
        updateFieldIfNotNull(fromExistingAppUser::setUsername, toAppUserUpdateDto.getUsername());
        updateFieldIfNotNull(fromExistingAppUser::setEmail, toAppUserUpdateDto.getEmail());
    }
}