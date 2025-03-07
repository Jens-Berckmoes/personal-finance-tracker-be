package be.jensberckmoes.personal_finance_tracker.controller;

import be.jensberckmoes.personal_finance_tracker.dto.UserCreateDto;
import be.jensberckmoes.personal_finance_tracker.dto.UserDto;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        // Arrange: Prepare the input and mocked service behavior
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

        // Act & Assert: Perform POST request and verify response
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON) // Set content type to JSON
                        .content(new ObjectMapper().writeValueAsString(inputUser))) // Serialize DTO to JSON
                .andExpect(status().isCreated()) // Expect HTTP 201
                .andExpect(jsonPath("$.username").value("testuser")) // Verify response content
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist()); // Password should not be in response
    }
}
