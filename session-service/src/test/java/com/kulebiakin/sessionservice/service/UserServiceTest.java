package com.kulebiakin.sessionservice.service;

import com.kulebiakin.common.exception.ResourceNotFoundException;
import com.kulebiakin.sessionservice.entity.User;
import com.kulebiakin.sessionservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .userId(1L)
            .firstName("John")
            .lastName("Doe")
            .sessionsTaken(0)
            .build();
    }

    @Test
    void getAllUsers_returnsListOfUsers() {
        List<User> users = Arrays.asList(testUser, User.builder().userId(2L).build());
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertThat(result).hasSize(2);
        verify(userRepository).findAll();
    }

    @Test
    void getUserById_existingId_returnsUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User result = userService.getUserById(1L);

        assertThat(result).isEqualTo(testUser);
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_nonExistingId_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createUser_savesAndReturnsUser() {
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.createUser(testUser);

        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("John");
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUser_existingId_updatesAndReturnsUser() {
        User updatedDetails = User.builder()
            .firstName("Jane")
            .lastName("Smith")
            .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.updateUser(1L, updatedDetails);

        assertThat(result.getFirstName()).isEqualTo("Jane");
        assertThat(result.getLastName()).isEqualTo("Smith");
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUser_nonExistingId_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(99L, testUser))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void incrementSessionsTaken_incrementsCounter() {
        testUser.setSessionsTaken(5);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.incrementSessionsTaken(1L);

        assertThat(result.getSessionsTaken()).isEqualTo(6);
        verify(userRepository).save(testUser);
    }

    @Test
    void deleteUser_existingId_deletesUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        userService.deleteUser(1L);

        verify(userRepository).delete(testUser);
    }

    @Test
    void deleteUser_nonExistingId_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
