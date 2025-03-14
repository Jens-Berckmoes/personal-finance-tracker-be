package be.jensberckmoes.personal_finance_tracker.unit.controller;

import be.jensberckmoes.personal_finance_tracker.controller.AppUserController;
import be.jensberckmoes.personal_finance_tracker.dto.AppUserCreateDto;
import be.jensberckmoes.personal_finance_tracker.dto.AppUserDto;
import be.jensberckmoes.personal_finance_tracker.exception.AppUserNotFoundException;
import be.jensberckmoes.personal_finance_tracker.exception.DuplicateEmailException;
import be.jensberckmoes.personal_finance_tracker.exception.DuplicateUsernameException;
import be.jensberckmoes.personal_finance_tracker.exception.ServiceException;
import be.jensberckmoes.personal_finance_tracker.service.AppUserService;
import be.jensberckmoes.personal_finance_tracker.service.CustomUserDetailsService;
import be.jensberckmoes.personal_finance_tracker.service.ValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AppUserController.class)
public class AppUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private AppUserService appUserService;

    @MockitoBean
    private ValidationService validationService;

    @Test
    public void givenValidUserDto_whenCreateUser_thenReturnsCreatedStatus() throws Exception {
        final AppUserCreateDto inputUser = AppUserCreateDto
                .builder()
                .username("testuser")
                .password("Password123!")
                .email("test@example.com")
                .build();
        final AppUserDto createdUser = AppUserDto
                .builder()
                .username("testuser")
                .email("test@example.com")
                .role("USER")
                .build();

        when(appUserService.createUser(any(AppUserCreateDto.class))).thenReturn(createdUser);

        mockMvc.perform(post("/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(inputUser)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    public void givenMissingUsername_whenCreateUser_thenReturnsBadRequest() throws Exception {
        final AppUserCreateDto inputUser = AppUserCreateDto.builder()
                .password("Password123!")
                .email("test@example.com")
                .build();

        mockMvc.perform(post("/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(inputUser)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    public void givenInvalidEmail_whenCreateUser_thenReturnsBadRequest() throws Exception {
        final AppUserCreateDto inputUser = AppUserCreateDto.builder()
                .username("testuser")
                .password("Password123!")
                .email("invalid-email")
                .build();

        mockMvc.perform(post("/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(inputUser)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    public void givenExistingUsername_whenCreateUser_thenReturnsConflict() throws Exception {
        final AppUserCreateDto inputUser = AppUserCreateDto.builder()
                .username("existinguser")
                .password("Password123!")
                .email("test@example.com")
                .build();

        when(appUserService.createUser(any(AppUserCreateDto.class)))
                .thenThrow(new DuplicateUsernameException("Username already exists"));

        mockMvc.perform(post("/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(inputUser)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }

    @Test
    public void givenExistingEmail_whenCreateUser_thenReturnsConflict() throws Exception {
        final AppUserCreateDto inputUser = AppUserCreateDto.builder()
                .username("newuser")
                .password("Password123!")
                .email("existing@example.com")
                .build();

        when(appUserService.createUser(any(AppUserCreateDto.class)))
                .thenThrow(new DuplicateEmailException("Email already exists"));

        mockMvc.perform(post("/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(inputUser)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    @Test
    public void givenNoCsrfToken_whenCreateUser_thenReturnsForbidden() throws Exception {
        final AppUserCreateDto inputUser = AppUserCreateDto.builder()
                .username("testuser")
                .password("Password123!")
                .email("test@example.com")
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(inputUser)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void givenNoRole_whenCreateUser_thenAssignDefaultRole() throws Exception {
        final AppUserCreateDto inputUser = AppUserCreateDto.builder()
                .username("testuser")
                .password("Password123!")
                .email("test@example.com")
                .build();

        final AppUserDto createdUser = AppUserDto.builder()
                .username("testuser")
                .email("test@example.com")
                .role("USER") // Default role
                .build();

        when(appUserService.createUser(any(AppUserCreateDto.class))).thenReturn(createdUser);

        mockMvc.perform(post("/users")
                        .with(csrf())
                        .with(user("testuser").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(inputUser)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    public void givenServiceFailure_whenCreateUser_thenReturnsInternalServerError() throws Exception {
        final AppUserCreateDto inputUser = AppUserCreateDto.builder()
                .username("testuser")
                .password("Password123!")
                .email("test@example.com")
                .build();

        when(appUserService.createUser(any(AppUserCreateDto.class)))
                .thenThrow(new ServiceException("Unexpected error occurred"));

        mockMvc.perform(post("/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(inputUser)))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Unexpected error occurred"));
    }

    @Test
    public void givenValidUserDtoButNoAdminRole_whenFindByUsername_thenReturnsNotAuthorised() throws Exception {
        final AppUserDto createdUser = AppUserDto
                .builder()
                .username("testuser")
                .email("test@example.com")
                .role("USER")
                .build();
        when(appUserService.findByUsername("testuser")).thenReturn(createdUser);

        // Act & Assert: Perform the GET request and validate the response
        mockMvc.perform(get("/users?username=testuser")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

    }

    @Test
    public void givenValidUserDtoWithAdminRole_whenFindByUsername_thenReturnsOk() throws Exception {
        final AppUserDto createdUser = AppUserDto
                .builder()
                .username("testuser")
                .email("test@example.com")
                .role("ADMIN")
                .build();
        when(appUserService.findByUsername("testuser")).thenReturn(createdUser);
        when(validationService.isValidUsername("testuser")).thenReturn(true);
        // Act & Assert: Perform the GET request and validate the response
        mockMvc.perform(get("/users?username=testuser")
                        .with(user("adminUser").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(appUserService, times(1)).findByUsername("testuser");
    }

    @Test
    public void givenUnauthenticatedUser_whenFindByUsername_thenReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/users")
                        .param("username", "testuser")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void givenUserRole_whenFindAnotherUserByUsername_thenReturnForbidden() throws Exception {
        mockMvc.perform(get("/users")
                        .param("username", "anotheruser")
                        .with(user("testuser").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void givenAdminRole_whenFindNonexistentUsername_thenReturnNotFound() throws Exception {
        when(appUserService.findByUsername("nonexistentuser"))
                .thenThrow(new AppUserNotFoundException("User not found")); // Simulate service behavior

        mockMvc.perform(get("/users/nonexistentuser")
                        .with(user("adminuser").roles("ADMIN")) // Simulate admin user
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Expect 404 Not Found
    }

    @Test
    public void givenInvalidUsername_whenFindByUsername_thenReturnBadRequest() throws Exception {
        when(validationService.isValidUsername("' OR 1=1 --")).thenReturn(false);

        mockMvc.perform(get("/users")
                        .param("username", "' OR 1=1 --")
                        .with(user("adminuser").roles("ADMIN")) // Simulate admin user
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()) // Expect 400 Bad Request
                .andExpect(result -> assertInstanceOf(ResponseStatusException.class, result.getResolvedException()))
                .andExpect(result -> assertTrue(Objects.requireNonNull(result.getResolvedException()).getMessage().contains("Invalid username format")));
    }


    @Test
    public void givenNoUsername_whenFindByUsername_thenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/users")
                        .with(user("adminuser").roles("ADMIN")) // Simulate admin user
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()); // Expect 400 Bad Request
    }

}
