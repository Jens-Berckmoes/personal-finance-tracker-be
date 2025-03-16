package be.jensberckmoes.personal_finance_tracker.unit.controller;

import be.jensberckmoes.personal_finance_tracker.controller.AppUserController;
import be.jensberckmoes.personal_finance_tracker.dto.AppUserCreateDto;
import be.jensberckmoes.personal_finance_tracker.dto.AppUserDto;
import be.jensberckmoes.personal_finance_tracker.dto.AppUserUpdateDto;
import be.jensberckmoes.personal_finance_tracker.exception.AppUserNotFoundException;
import be.jensberckmoes.personal_finance_tracker.exception.DuplicateEmailException;
import be.jensberckmoes.personal_finance_tracker.exception.DuplicateUsernameException;
import be.jensberckmoes.personal_finance_tracker.exception.ServiceException;
import be.jensberckmoes.personal_finance_tracker.model.Role;
import be.jensberckmoes.personal_finance_tracker.service.AppUserService;
import be.jensberckmoes.personal_finance_tracker.service.CustomUserDetailsService;
import be.jensberckmoes.personal_finance_tracker.service.ValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
                .role("USER")
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
        when(validationService.isValidUsername("nonexistentuser")).thenReturn(true);
        when(appUserService.findByUsername("nonexistentuser"))
                .thenThrow(new AppUserNotFoundException("User not found"));

        mockMvc.perform(get("/users")
                        .param("username", "nonexistentuser")
                        .with(user("adminuser").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void givenInvalidUsername_whenFindByUsername_thenReturnBadRequest() throws Exception {
        when(validationService.isValidUsername("' OR 1=1 --")).thenReturn(false);

        mockMvc.perform(get("/users")
                        .param("username", "' OR 1=1 --")
                        .with(user("adminuser").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(ResponseStatusException.class, result.getResolvedException()))
                .andExpect(result -> assertTrue(Objects.requireNonNull(result.getResolvedException()).getMessage().contains("Invalid username format")));
    }


    @Test
    public void givenNoUsername_whenFindByUsername_thenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/users")
                        .with(user("adminuser").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenAuthenticatedUser_whenGetCurrentUser_thenReturnsUserDetails() throws Exception {
        final AppUserDto mockUserDto = AppUserDto.builder()
                .username("testuser")
                .email("testuser@example.com")
                .role("USER")
                .build();
        when(appUserService.findByUsername("testuser")).thenReturn(mockUserDto);

        mockMvc.perform(get("/users/me")
                        .with(user("testuser").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("testuser@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    public void givenValidUserId_whenGetUserById_thenReturnsUserDetails() throws Exception {
        final AppUserDto mockUserDto = AppUserDto.builder()
                .id(1L)
                .username("testuser")
                .email("testuser@example.com")
                .role("USER")
                .build();
        when(appUserService.getUserById(1L)).thenReturn(mockUserDto);

        mockMvc.perform(get("/users/1")
                        .with(user("adminuser").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("testuser@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    public void givenUsersExist_whenGetAllUsers_thenReturnsPagedUsers() throws Exception {
        final List<AppUserDto> userList = List.of(
                AppUserDto.builder()
                        .id(1L)
                        .username("user1")
                        .email("user1@example.com")
                        .role("USER")
                        .build(),
                AppUserDto.builder()
                        .id(2L)
                        .username("user2")
                        .email("user2@example.com")
                        .role("USER")
                        .build()
        );
        Page<AppUserDto> userPage = new PageImpl<>(userList);
        when(appUserService.getAllUsers(any(Pageable.class))).thenReturn(userPage);

        mockMvc.perform(get("/users/all")
                        .with(user("adminuser").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("user1"))
                .andExpect(jsonPath("$.content[1].username").value("user2"));
    }

    @Test
    public void givenUsersWithRole_whenGetUsersByRole_thenReturnsUsersList() throws Exception {
        final List<AppUserDto> userList = List.of(
                AppUserDto.builder()
                        .id(1L)
                        .username("admin1")
                        .email("admin1@example.com")
                        .role("USER")
                        .build(),
                AppUserDto.builder()
                        .id(2L)
                        .username("admin2")
                        .email("admin2@example.com")
                        .role("USER")
                        .build()
        );
        when(appUserService.getUsersByRole(Role.ADMIN)).thenReturn(userList);

        mockMvc.perform(get("/users/role/ADMIN")
                        .with(user("adminuser").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("admin1"))
                .andExpect(jsonPath("$[1].username").value("admin2"));
    }

    @Test
    public void givenUsersWithSubstring_whenGetUsersByUsernameContains_thenReturnsPagedUsers() throws Exception {
        final List<AppUserDto> userList = List.of(
                AppUserDto.builder()
                        .id(1L)
                        .username("testuser1")
                        .email("test1@example.com")
                        .role("USER")
                        .build(),
                AppUserDto.builder()
                        .id(2L)
                        .username("testuser2")
                        .email("test2@example.com")
                        .role("USER")
                        .build()
        );
        final Page<AppUserDto> userPage = new PageImpl<>(userList);
        when(appUserService.getUsersByUsernameContains(eq("test"), any(Pageable.class))).thenReturn(userPage);

        mockMvc.perform(get("/users/search")
                        .param("substring", "test")
                        .with(user("adminuser").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("testuser1"))
                .andExpect(jsonPath("$.content[1].username").value("testuser2"));
    }

    @Test
    public void givenAdminUser_whenUpdateAnotherUserRoleToAdmin_thenRoleIsUpdatedSuccessfully() throws Exception {
        final AppUserDto initialUserDto = AppUserDto.builder()
                .id(2L)
                .username("secondUser")
                .email("seconduser@example.com")
                .role("USER")
                .build();
        when(appUserService.getUserById(2L)).thenReturn(initialUserDto);

        final AppUserUpdateDto updateDto = AppUserUpdateDto.builder()
                .username("secondUser")
                .email("seconduser@example.com")
                .build();

        final AppUserDto updatedUserDto = AppUserDto.builder()
                .id(2L)
                .username("secondUser")
                .email("seconduser@example.com")
                .role("ADMIN")
                .build();
        when(appUserService.updateUser(eq(2L), any(AppUserUpdateDto.class))).thenReturn(updatedUserDto);

        mockMvc.perform(get("/users/2")
                        .with(user("adminuser").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("secondUser"))
                .andExpect(jsonPath("$.email").value("seconduser@example.com"))
                .andExpect(jsonPath("$.role").value("USER")); // Initial role is USER

        mockMvc.perform(put("/users/2")
                        .with(user("adminuser").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("secondUser"))
                .andExpect(jsonPath("$.email").value("seconduser@example.com"))
                .andExpect(jsonPath("$.role").value("ADMIN")); // Role should now be updated to ADMIN
    }


    @Test
    public void givenValidUserId_whenDeleteUser_thenReturnsNoContent() throws Exception {
        doNothing().when(appUserService).deleteUser(1L);

        mockMvc.perform(delete("/users/1")
                        .with(user("adminuser").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void givenUsername_whenCheckUsernameExists_thenReturnsTrue() throws Exception {
        when(appUserService.usernameExists("testuser")).thenReturn(true);

        mockMvc.perform(get("/users/exists/username")
                        .param("username", "testuser")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    public void givenEmail_whenCheckEmailExists_thenReturnsTrue() throws Exception {
        when(appUserService.emailExists("testuser@example.com")).thenReturn(true);

        mockMvc.perform(get("/users/exists/email")
                        .param("email", "testuser@example.com")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    public void givenUserWithRole_whenCheckHasRole_thenReturnsTrue() throws Exception {
        when(appUserService.hasRole(1L, Role.ADMIN)).thenReturn(true);

        mockMvc.perform(get("/users/1/hasRole")
                        .param("role", "ADMIN")
                        .with(user("adminuser").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }


}
