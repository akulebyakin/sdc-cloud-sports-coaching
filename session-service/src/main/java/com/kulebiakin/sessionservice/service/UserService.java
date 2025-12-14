package com.kulebiakin.sessionservice.service;

import com.kulebiakin.common.exception.ResourceNotFoundException;
import com.kulebiakin.sessionservice.entity.User;
import com.kulebiakin.sessionservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public List<User> getAllUsers() {
        log.debug("Fetching all users");
        return userRepository.findAll();
    }

    public User getUserById(Long userId) {
        log.debug("Fetching user with id: {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    @Transactional
    public User createUser(User user) {
        log.debug("Creating new user: {} {}", user.getFirstName(), user.getLastName());
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long userId, User userDetails) {
        log.debug("Updating user with id: {}", userId);
        User user = getUserById(userId);
        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        return userRepository.save(user);
    }

    @Transactional
    public User incrementSessionsTaken(Long userId) {
        log.debug("Incrementing sessions taken for user id: {}", userId);
        User user = getUserById(userId);
        user.setSessionsTaken(user.getSessionsTaken() + 1);
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        log.debug("Deleting user with id: {}", userId);
        User user = getUserById(userId);
        userRepository.delete(user);
    }
}
