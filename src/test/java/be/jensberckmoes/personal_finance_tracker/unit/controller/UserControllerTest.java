package be.jensberckmoes.personal_finance_tracker.unit.controller;

import be.jensberckmoes.personal_finance_tracker.controller.UserController;
import be.jensberckmoes.personal_finance_tracker.dto.UserCreateDto;
import be.jensberckmoes.personal_finance_tracker.dto.UserDto;
import be.jensberckmoes.personal_finance_tracker.exception.DuplicateEmailException;
import be.jensberckmoes.personal_finance_tracker.exception.DuplicateUsernameException;
import be.jensberckmoes.personal_finance_tracker.model.Role;
import be.jensberckmoes.personal_finance_tracker.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    public void givenValidUserDto_whenCreateUser_thenReturnsCreatedStatus() throws Exception {
        final UserCreateDto inputUser = UserCreateDto
                .builder()
                .username("testuser")
                .password("Password123!")
                .email("test@example.com")
                .role(Role.USER)
                .build();
        final UserDto createdUser = UserDto
                .builder()
                .username("testuser")
                .email("test@example.com")
                .role("USER")
                .build();

        when(userService.createUser(any(UserCreateDto.class))).thenReturn(createdUser);

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
        final UserCreateDto inputUser = UserCreateDto.builder()
                .password("Password123!")
                .email("test@example.com")
                .role(Role.USER)
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
        final UserCreateDto inputUser = UserCreateDto.builder()
                .username("testuser")
                .password("Password123!")
                .email("invalid-email")
                .role(Role.USER)
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
        final UserCreateDto inputUser = UserCreateDto.builder()
                .username("existinguser")
                .password("Password123!")
                .email("test@example.com")
                .role(Role.USER)
                .build();

        when(userService.createUser(any(UserCreateDto.class)))
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
        final UserCreateDto inputUser = UserCreateDto.builder()
                .username("newuser")
                .password("Password123!")
                .email("existing@example.com")
                .role(Role.USER)
                .build();

        when(userService.createUser(any(UserCreateDto.class)))
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
        final UserCreateDto inputUser = UserCreateDto.builder()
                .username("testuser")
                .password("Password123!")
                .email("test@example.com")
                .role(Role.USER)
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(inputUser)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
    @Test
    public void givenNoRole_whenCreateUser_thenAssignDefaultRole() throws Exception {
        final UserCreateDto inputUser = UserCreateDto.builder()
                .username("testuser")
                .password("Password123!")
                .email("test@example.com")
                .build();

        final UserDto createdUser = UserDto.builder()
                .username("testuser")
                .email("test@example.com")
                .role("USER") // Default role
                .build();

        when(userService.createUser(any(UserCreateDto.class))).thenReturn(createdUser);

        mockMvc.perform(post("/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(inputUser)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("USER"));
    }
    @Test
    public void givenServiceFailure_whenCreateUser_thenReturnsInternalServerError() throws Exception {
        final UserCreateDto inputUser = UserCreateDto.builder()
                .username("testuser")
                .password("Password123!")
                .email("test@example.com")
                .role(Role.USER)
                .build();

        when(userService.createUser(any(UserCreateDto.class)))
                .thenThrow(new RuntimeException("Unexpected error occurred"));

        mockMvc.perform(post("/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(inputUser)))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Unexpected error occurred"));
    }


    @Test
    public void shouldReturnUserDetails_whenValidUserIdIsProvided() throws Exception {
        final UserDto createdUser = UserDto
                .builder()
                .username("testuser")
                .email("test@example.com")
                .role("USER")
                .build();
        when(userService.findByUsername("testuser")).thenReturn(createdUser);

        // Act & Assert: Perform the GET request and validate the response
        mockMvc.perform(get("/users?username=testuser")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService, times(1)).findByUsername("testuser");
    }
    @Test
    public void shouldReturnUserDetails_whenValidUserIdIsProvidedAndRoleIsAdmin() throws Exception {
        final UserDto createdUser = UserDto
                .builder()
                .username("testuser")
                .email("test@example.com")
                .role("ADMIN")
                .build();
        when(userService.findByUsername("testuser")).thenReturn(createdUser);

        // Act & Assert: Perform the GET request and validate the response
        mockMvc.perform(get("/users?username=testuser")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService, times(1)).findByUsername("testuser");
    }


}
