package com.train.repository;

import com.train.config.TestContainerSpringBootTest;
import com.train.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@TestContainerSpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Integration Tests - UserRepository with PostgreSQL")
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .email("test@example.com")
            .username("testuser")
            .password("hashedpassword")
            .firstName("Test")
            .lastName("User")
            .isActive(true)
            .createdAt(LocalDateTime.now())
            .build();
    }

    @Test
    @DisplayName("Should save user to PostgreSQL successfully")
    void testSaveUser() {
        // Act
        User savedUser = userRepository.save(testUser);

        // Assert
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should find user by email")
    void testFindUserByEmail() {
        // Arrange
        userRepository.save(testUser);

        // Act
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");

        // Assert
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should find user by username")
    void testFindUserByUsername() {
        // Arrange
        userRepository.save(testUser);

        // Act
        Optional<User> foundUser = userRepository.findByUsername("testuser");

        // Assert
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should update user")
    void testUpdateUser() {
        // Arrange
        User savedUser = userRepository.save(testUser);
        Long userId = savedUser.getId();

        // Act
        savedUser.setFirstName("Updated");
        User updatedUser = userRepository.save(savedUser);

        // Assert
        assertThat(updatedUser.getFirstName()).isEqualTo("Updated");
        assertThat(updatedUser.getId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Should delete user")
    void testDeleteUser() {
        // Arrange
        User savedUser = userRepository.save(testUser);
        Long userId = savedUser.getId();

        // Act
        userRepository.deleteById(userId);
        Optional<User> deletedUser = userRepository.findById(userId);

        // Assert
        assertThat(deletedUser).isEmpty();
    }

    @Test
    @DisplayName("Should return empty when user not found by email")
    void testFindNonExistentUserByEmail() {
        // Act
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // Assert
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Should check if email exists")
    void testExistsByEmail() {
        // Arrange
        userRepository.save(testUser);

        // Act & Assert
        assertThat(userRepository.existsByEmail("test@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("other@example.com")).isFalse();
    }
}