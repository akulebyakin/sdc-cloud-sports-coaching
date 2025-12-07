package com.kulebiakin.sportscoaching.service;

import com.kulebiakin.sportscoaching.entity.User;
import com.kulebiakin.sportscoaching.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import static org.mockito.Mockito.*;

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
                .firstName("Alice")
                .lastName("Johnson")
                .sessionsTaken(5)
                .build();
    }

    @Nested
    @DisplayName("getAllUsers")
    class GetAllUsers {

        @Test
        @DisplayName("should return all users")
        void shouldReturnAllUsers() {
            User user2 = User.builder()
                    .userId(2L)
                    .firstName("Bob")
                    .lastName("Williams")
                    .sessionsTaken(3)
                    .build();

            when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, user2));

            List<User> result = userService.getAllUsers();

            assertThat(result).hasSize(2);
            verify(userRepository).findAll();
        }

        @Test
        @DisplayName("should return empty list when no users exist")
        void shouldReturnEmptyListWhenNoUsers() {
            when(userRepository.findAll()).thenReturn(List.of());

            List<User> result = userService.getAllUsers();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getUserById")
    class GetUserById {

        @Test
        @DisplayName("should return user when found")
        void shouldReturnUserWhenFound() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            User result = userService.getUserById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getFirstName()).isEqualTo("Alice");
            assertThat(result.getLastName()).isEqualTo("Johnson");
            assertThat(result.getSessionsTaken()).isEqualTo(5);
        }

        @Test
        @DisplayName("should throw exception when user not found")
        void shouldThrowExceptionWhenNotFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserById(99L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("User not found with id: 99");
        }
    }

    @Nested
    @DisplayName("createUser")
    class CreateUser {

        @Test
        @DisplayName("should create user successfully")
        void shouldCreateUser() {
            User newUser = User.builder()
                    .firstName("New")
                    .lastName("User")
                    .build();

            when(userRepository.save(any(User.class))).thenReturn(testUser);

            User result = userService.createUser(newUser);

            assertThat(result).isNotNull();
            verify(userRepository).save(newUser);
        }
    }

    @Nested
    @DisplayName("updateUser")
    class UpdateUser {

        @Test
        @DisplayName("should update user successfully")
        void shouldUpdateUser() {
            User updatedDetails = User.builder()
                    .firstName("UpdatedAlice")
                    .lastName("UpdatedJohnson")
                    .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            User result = userService.updateUser(1L, updatedDetails);

            assertThat(result.getFirstName()).isEqualTo("UpdatedAlice");
            assertThat(result.getLastName()).isEqualTo("UpdatedJohnson");
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("should throw exception when updating non-existent user")
        void shouldThrowExceptionWhenUpdatingNonExistent() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUser(99L, testUser))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("incrementSessionsTaken")
    class IncrementSessionsTaken {

        @Test
        @DisplayName("should increment sessions taken by 1")
        void shouldIncrementSessionsTaken() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            User result = userService.incrementSessionsTaken(1L);

            assertThat(result.getSessionsTaken()).isEqualTo(6);
            verify(userRepository).save(testUser);
        }
    }

    @Nested
    @DisplayName("deleteUser")
    class DeleteUser {

        @Test
        @DisplayName("should delete user successfully")
        void shouldDeleteUser() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            doNothing().when(userRepository).delete(testUser);

            userService.deleteUser(1L);

            verify(userRepository).delete(testUser);
        }

        @Test
        @DisplayName("should throw exception when deleting non-existent user")
        void shouldThrowExceptionWhenDeletingNonExistent() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.deleteUser(99L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }
}
