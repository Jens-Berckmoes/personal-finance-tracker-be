package be.jensberckmoes.personal_finance_tracker.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DuplicateCategoryNameException extends RuntimeException{
    public DuplicateCategoryNameException(String message) {
        super(message);
    }
}
