package be.jensberckmoes.personal_finance_tracker.service;

import org.springframework.stereotype.Service;

@Service
public class ValidationServiceImpl implements ValidationService {

    @Override
    public boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    @Override
    public boolean isValidUsername(String username) {
        return username != null && username.matches("^[a-zA-Z0-9._]{3,20}$");
    }

}
