package be.jensberckmoes.personal_finance_tracker.service;

import be.jensberckmoes.personal_finance_tracker.exception.InvalidUserException;
import be.jensberckmoes.personal_finance_tracker.model.User;
import be.jensberckmoes.personal_finance_tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HashingService hashingService;

    @Autowired
    private ValidationService validationService;

    public User register(final User user) {
        if (Objects.isNull(user) || !validationService.isValidUsername(user.getUsername())) {
            throw new InvalidUserException("Username is invalid");
        }
        if (!validationService.isValidEmail(user.getEmail())) {
            throw new InvalidUserException("User has invalid email");
        }
        user.setPassword(hashingService.hashPassword(user.getPassword()));
        return userRepository.save(user);
    }

    public User findByUsername(final String username) {
        return null;
    }

}