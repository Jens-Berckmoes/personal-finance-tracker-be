package be.jensberckmoes.personal_finance_tracker.controller;

import be.jensberckmoes.personal_finance_tracker.dto.AppUserCreateDto;
import be.jensberckmoes.personal_finance_tracker.dto.AppUserDto;
import be.jensberckmoes.personal_finance_tracker.service.AppUserService;
import be.jensberckmoes.personal_finance_tracker.service.ValidationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class AppUserController {

    private final AppUserService appUserService;
    private final ValidationService validationService;

    @PostMapping
    public ResponseEntity<AppUserDto> createUser(@RequestBody @Valid final AppUserCreateDto appUserCreateDto) {
        final AppUserDto createdUser = appUserService.createUser(appUserCreateDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdUser);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppUserDto> getUserByUsername(@RequestParam final String username) {
        if (!validationService.isValidUsername(username)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid username format");
        }

        final AppUserDto user = appUserService.findByUsername(username);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/me")
    public ResponseEntity<AppUserDto> getCurrentUser(final Authentication authentication) {
        final String username = authentication.getName();
        final AppUserDto user = appUserService.findByUsername(username);
        return ResponseEntity.ok(user);
    }

}