package be.jensberckmoes.personal_finance_tracker.service;

import be.jensberckmoes.personal_finance_tracker.dto.TransactionCreateDto;
import be.jensberckmoes.personal_finance_tracker.exception.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class TransactionValidationService {
    public void validateTransactionCreateDto(final TransactionCreateDto dto) {
        validateNotNull(dto);
        validateUserId(dto);
        validateAmount(dto);
        validateType(dto);
        validateMethod(dto);
        validateDate(dto);
        validateDescription(dto);
    }

    private static void validateNotNull(final TransactionCreateDto dto) {
        if (Objects.isNull(dto)) {
            throw new NullParameterException("TransactionCreateDto cannot be null.");
        }
    }

    private static void validateDescription(final TransactionCreateDto dto) {
        if (dto.getDescription() != null && dto.getDescription().length() > 255) {
            throw new InvalidTransactionDescriptionException("Description cannot exceed 255 characters.");
        }
    }

    private static void validateDate(final TransactionCreateDto dto) {
        if (Objects.isNull(dto.getDate())) {
            throw new NullTransactionDateException();
        }

        if (dto.getDate().isAfter(LocalDateTime.now())) {
            throw new InvalidTransactionDateException("Transaction date cannot be in the future.");
        }
    }

    private static void validateMethod(final TransactionCreateDto dto) {
        if (dto.getMethod() != null && dto.getMethod().toString().length() > 100) {
            throw new InvalidTransactionMethodException("Payment method cannot exceed 100 characters.");
        }
    }

    private static void validateType(final TransactionCreateDto dto) {
        if (Objects.isNull(dto.getType())) {
            throw new NullTransactionTypeException();
        }

        if (dto.getType().toString().length() > 7) {
            throw new InvalidTransactionTypeException("Payment method cannot exceed 100 characters.");
        }
    }

    private static void validateAmount(final TransactionCreateDto dto) {
        if (Objects.isNull(dto.getAmount())) {
            throw new NullTransactionAmountException();
        }

        if (dto.getAmount().precision() > 10) {
            throw new InvalidTransactionAmountException("Amount precision cannot exceed 10 digits.");
        }

        if (dto.getAmount().scale() != 2) {
            throw new InvalidTransactionAmountException("Amount scale must be 2.");
        }

        if (dto.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidTransactionAmountException("Amount cannot be negative.");
        }
    }

    private static void validateUserId(final TransactionCreateDto dto) {
        if (Objects.isNull(dto.getUserId())) {
            throw new NullUserIdException();
        }
    }
}
