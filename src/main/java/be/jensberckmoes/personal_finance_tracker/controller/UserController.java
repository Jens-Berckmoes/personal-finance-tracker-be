package be.jensberckmoes.personal_finance_tracker.controller;

import be.jensberckmoes.personal_finance_tracker.dto.UserCreateDto;
import be.jensberckmoes.personal_finance_tracker.dto.UserDto;
import be.jensberckmoes.personal_finance_tracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(final UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody @Valid final UserCreateDto userCreateDto) {
        final UserDto createdUser = userService.createUser(userCreateDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdUser);
    }

    @GetMapping
    public ResponseEntity<UserDto> findByUsername(@RequestParam final String username) {
        final UserDto user = userService.findByUsername(username);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(final Authentication authentication) {
        final String username = authentication.getName();
        final UserDto user = userService.findByUsername(username);
        return ResponseEntity.ok(user);
    }

}