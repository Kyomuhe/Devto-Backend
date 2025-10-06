package com.example.kay.service;

import com.example.kay.Dto.SignupRequest;
import com.example.kay.controller.AuthController;
import com.example.kay.model.User;
import com.example.kay.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Service
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Allowed image types
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    // Max file size (2MB)
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }


    public User createUser(SignupRequest signupRequest) throws IOException {
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setName(signupRequest.getName());
        user.setRole(User.Role.USER);
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // Handle profile image - ensure it's stored as clean base64
        if (signupRequest.getProfileImage() != null && !signupRequest.getProfileImage().isEmpty()) {
            String profileImage = signupRequest.getProfileImage();

            // Remove data URL prefix if present (e.g., "data:image/png;base64,")
            if (profileImage.contains(",")) {
                profileImage = profileImage.split(",", 2)[1];
            }

            // Validate it's proper base64 and normalize it
            try {
                // Test decode to validate
                byte[] imageBytes = Base64.getDecoder().decode(profileImage);

                // Re-encode to ensure clean base64 (removes any whitespace/newlines)
                String cleanBase64 = Base64.getEncoder().encodeToString(imageBytes);

                user.setProfileImage(cleanBase64);
                System.out.println("Saved image as base64, length: " + cleanBase64.length());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid image format. Please provide a valid base64 encoded image.");
            }
        }

        return userRepository.save(user);
    }



    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public String getUserProfileImage(Long userId) {
        User user = getUserById(userId);
        return user != null ? user.getProfileImage() : null;
    }
    public boolean hasProfileImage(Long userId) {
        return getUserProfileImage(userId) != null ;
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }


}