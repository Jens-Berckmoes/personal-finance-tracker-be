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
        if(Objects.isNull(user)){
            throw new InvalidUserException("Username is invalid");
        }
        if(findByUsername(user.getUsername()).isPresent()){
            throw new InvalidUserException("Username already taken");
        }
        if(!validationService.isValidPassword(user.getPassword())){
            throw new InvalidUserException("User has invalid password. Password should be 8+ characters long, 1 uppercase, 1 lowercase, 1 number and 1 special character(@$!%*?&).");
        }
        if (!validationService.isValidEmail(user.getEmail())) {
            throw new InvalidUserException("User has invalid email. Email should be in the form (test@example.com).");
        }
        user.setPassword(hashingService.hashPassword(user.getPassword()));
        return userRepository.save(user);
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