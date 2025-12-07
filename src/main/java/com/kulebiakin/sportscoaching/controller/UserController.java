package com.kulebiakin.sportscoaching.controller;

import com.kulebiakin.sportscoaching.entity.User;
import com.kulebiakin.sportscoaching.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("GET /api/users - Fetching all users");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
        log.info("GET /api/users/{} - Fetching user by id", userId);
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        log.info("POST /api/users - Creating new user");
        User createdUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUser(@PathVariable Long userId, @Valid @RequestBody User user) {
        log.info("PUT /api/users/{} - Updating user", userId);
        return ResponseEntity.ok(userService.updateUser(userId, user));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        log.info("DELETE /api/users/{} - Deleting user", userId);
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
