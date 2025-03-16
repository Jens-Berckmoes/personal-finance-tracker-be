package be.jensberckmoes.personal_finance_tracker.controller;

import be.jensberckmoes.personal_finance_tracker.dto.AppUserCreateDto;
import be.jensberckmoes.personal_finance_tracker.dto.AppUserDto;
import be.jensberckmoes.personal_finance_tracker.dto.AppUserUpdateDto;
import be.jensberckmoes.personal_finance_tracker.model.Role;
import be.jensberckmoes.personal_finance_tracker.service.AppUserService;
import be.jensberckmoes.personal_finance_tracker.service.ValidationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

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

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppUserDto> getUserById(@PathVariable final Long id) {
        final AppUserDto user = appUserService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AppUserDto>> getAllUsers(final Pageable pageable) {
        final Page<AppUserDto> users = appUserService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AppUserDto>> getUsersByRole(@PathVariable final Role role) {
        final List<AppUserDto> users = appUserService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AppUserDto>> getUsersByUsernameContains(
            @RequestParam String substring,
            Pageable pageable) {
        final Page<AppUserDto> users = appUserService.getUsersByUsernameContains(substring, pageable);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppUserDto> updateUser(
            @PathVariable Long id,
            @RequestBody @Valid AppUserUpdateDto appUserUpdateDto) {
        final AppUserDto updatedUser = appUserService.updateUser(id, appUserUpdateDto);
        return ResponseEntity.ok(updatedUser);
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppUserDto> updateUserRole(
            @PathVariable Long id,
            @RequestParam Role role) {
        final AppUserDto updatedUser = appUserService.updateUserRole(id, role);
        return ResponseEntity.ok(updatedUser);
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        appUserService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exists/username")
    public ResponseEntity<Boolean> usernameExists(@RequestParam String username) {
        final boolean exists = appUserService.usernameExists(username);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/exists/email")
    public ResponseEntity<Boolean> emailExists(@RequestParam String email) {
        final boolean exists = appUserService.emailExists(email);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/{id}/hasRole")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Boolean> hasRole(
            @PathVariable Long id,
            @RequestParam Role role) {
        final boolean hasRole = appUserService.hasRole(id, role);
        return ResponseEntity.ok(hasRole);
    }

}