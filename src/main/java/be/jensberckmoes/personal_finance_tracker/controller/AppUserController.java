package be.jensberckmoes.personal_finance_tracker.controller;

import be.jensberckmoes.personal_finance_tracker.dto.AppUserCreateDto;
import be.jensberckmoes.personal_finance_tracker.dto.AppUserDto;
import be.jensberckmoes.personal_finance_tracker.service.AppUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class AppUserController {

    private final AppUserService appUserService;

    public AppUserController(final AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @PostMapping
    public ResponseEntity<AppUserDto> createUser(@RequestBody @Valid final AppUserCreateDto appUserCreateDto) {
        final AppUserDto createdUser = appUserService.createUser(appUserCreateDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdUser);
    }

    @GetMapping
    public ResponseEntity<AppUserDto> findByUsername(@RequestParam final String username) {
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