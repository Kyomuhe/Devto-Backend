package com.example.kay.controller;

import com.example.kay.model.User;
import com.example.kay.service.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        try {
            User user = userService.createUser(
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getName()
            );
            String jwt = jwtService.generateToken(user);
            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("user", createUserResponse(user));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Signup with profile
    @PostMapping("/signup-with-image")
    public ResponseEntity<?> signupWithImage(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("name") String name,
            @RequestParam("profileImage") MultipartFile profileImage) {
        try {
            User user = userService.createUser(username, email, password, name, profileImage);
            String jwt = jwtService.generateToken(user);
            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("user", createUserResponse(user));
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process profile image"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            User user = (User) authentication.getPrincipal();
            String jwt = jwtService.generateToken(user);

            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("user", createUserResponse(user));

            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid username or password"));
        }
    }

    // Serve profile image by user ID (for database-stored images)
    @GetMapping("/user/{userId}/profile-image")
    public ResponseEntity<byte[]> getUserProfileImage(@PathVariable Long userId) {
        try {
            byte[] imageBytes = userService.getUserProfileImage(userId);

            if (imageBytes == null || imageBytes.length == 0) {
                return ResponseEntity.notFound().build();
            }

            // Determine content type based on image data
            String contentType = determineImageContentType(imageBytes);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentLength(imageBytes.length);
            headers.setCacheControl("max-age=3600"); // Cache for 1 hour

            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Determine image content type from byte array
     */
    private String determineImageContentType(byte[] imageBytes) {
        if (imageBytes.length < 4) {
            return "application/octet-stream";
        }

        // Check file signature (magic numbers)
        if (imageBytes[0] == (byte) 0xFF && imageBytes[1] == (byte) 0xD8) {
            return "image/jpeg";
        } else if (imageBytes[0] == (byte) 0x89 && imageBytes[1] == (byte) 0x50 &&
                imageBytes[2] == (byte) 0x4E && imageBytes[3] == (byte) 0x47) {
            return "image/png";
        } else if (imageBytes[0] == (byte) 0x47 && imageBytes[1] == (byte) 0x49 &&
                imageBytes[2] == (byte) 0x46) {
            return "image/gif";
        } else if (imageBytes.length >= 12 &&
                imageBytes[8] == (byte) 0x57 && imageBytes[9] == (byte) 0x45 &&
                imageBytes[10] == (byte) 0x42 && imageBytes[11] == (byte) 0x50) {
            return "image/webp";
        }

        // Default to JPEG if we can't determine
        return "image/jpeg";
    }

    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("id", user.getId());
        userResponse.put("username", user.getUsername());
        userResponse.put("email", user.getEmail());
        userResponse.put("Name", user.getName());
        userResponse.put("role", user.getRole());

        // Add profile image URL if user has profile image
        if (userService.hasProfileImage(user.getId())) {
            userResponse.put("profile_image", "/api/auth/user/" + user.getId() + "/profile-image");
        }

        return userResponse;
    }

    // Binds incoming JSON to Java objects
    @Data
    public static class SignupRequest {
        private String username;
        private String email;
        private String password;
        private String name;
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }
}