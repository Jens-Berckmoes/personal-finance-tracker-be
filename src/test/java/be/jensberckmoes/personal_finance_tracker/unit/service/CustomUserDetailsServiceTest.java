package be.jensberckmoes.personal_finance_tracker.unit.service;

import be.jensberckmoes.personal_finance_tracker.model.AppUser;
import be.jensberckmoes.personal_finance_tracker.model.Role;
import be.jensberckmoes.personal_finance_tracker.repository.AppUserRepository;
import be.jensberckmoes.personal_finance_tracker.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    public void givenExistingUser_whenLoadUserByUsername_thenReturnsUserDetails() {
        final AppUser testUser = new AppUser(
                1L,
                "testuser",
                "securepassword",
                "testuser@example.com",
                Role.USER
        );
        when(appUserRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("securepassword", userDetails.getPassword());
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
    }

    @Test
    public void givenNonExistingUser_whenLoadUserByUsername_thenThrowsException() {
        when(appUserRepository.findByUsername("nonexistentuser")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> customUserDetailsService.loadUserByUsername("nonexistentuser"));
    }

    @Test
    public void whenLoadUserByUsername_thenRepositoryIsCalledOnce() {
        final AppUser testUser = new AppUser(
                1L,
                "testuser",
                "securepassword",
                "testuser@example.com",
                Role.USER
        );
        when(appUserRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        customUserDetailsService.loadUserByUsername("testuser");

        verify(appUserRepository, times(1)).findByUsername("testuser");
    }

}
