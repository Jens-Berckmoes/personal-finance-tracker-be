package be.jensberckmoes.personal_finance_tracker.service;

import be.jensberckmoes.personal_finance_tracker.exception.InvalidUserException;
import be.jensberckmoes.personal_finance_tracker.model.User;
import be.jensberckmoes.personal_finance_tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService{

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
        if(findByUsername(user.getUsername()).isPresent()){
            throw new InvalidUserException("Username already taken");
        }
        if (!validationService.isValidEmail(user.getEmail())) {
            throw new InvalidUserException("User has invalid email");
        }
        user.setPassword(hashingService.hashPassword(user.getPassword()));
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(final String username) {
        if (Objects.isNull(username) || !validationService.isValidUsername(username)) {
            throw new InvalidUserException("Username is invalid");
        }
        return userRepository.findByUsername(username);
    }

}