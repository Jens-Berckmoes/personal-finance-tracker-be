package be.jensberckmoes.personal_finance_tracker.service;

import be.jensberckmoes.personal_finance_tracker.exception.InvalidPasswordException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class HashingServiceImpl implements HashingService {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public String hashPassword(final String password) {
        if (password == null) {
            throw new InvalidPasswordException("Password cannot be null");
        }
        if (password.isEmpty()){
            throw new InvalidPasswordException("Password was empty");
        }
        return passwordEncoder.encode(password);
    }
}