package com.example.kay.controller;

import com.example.kay.Dto.SignupRequest;
import com.example.kay.model.User;
import com.example.kay.repository.UserRepository;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;



    // Signup
    @PostMapping("/signup")
    public ResponseEntity<?> signupWithImage(@RequestBody SignupRequest signupRequest) {
        try {
            if (signupRequest.getUsername() == null || signupRequest.getUsername().isEmpty()) {
                return badRequest("user name is either null or empty");
            }
            if (signupRequest.getPassword() == null || signupRequest.getPassword().isEmpty()) {
                return badRequest("user password is either null or empty");
            }
            if (signupRequest.getEmail() == null || signupRequest.getEmail().isEmpty()) {
                return badRequest("user email is either null or empty");
            }

            User user = userService.createUser(signupRequest);
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
    public ResponseEntity<?> login(@RequestBody LoginRequest request ) {
        try {
            if(request.getUsername()== null || request.getUsername().isEmpty()){
                return badRequest( "username is either null or empty");
            }
            if(request.getPassword()== null || request.getPassword().isEmpty()){
                return badRequest("password is either null or empty");
            }
            Optional<User> userExists = userRepository.findByUsername(request.getUsername());
            if(userExists.isEmpty()){
                return badRequest( "user does not exist");
            }
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

            User actualUser = userExists.get();

            if(!passwordEncoder.matches(request.getPassword(), actualUser.getPassword())){
                return badRequest("wrong password");
            }

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
            return badRequest("Invalid username or password");
        }
    }

    // Serve profile image by user ID
    @GetMapping("/user/{userId}/profile-image")
    public ResponseEntity<byte[]> getUserProfileImage(@PathVariable Long userId) {
        try {
            String imageString = userService.getUserProfileImage(userId);

            if (imageString == null || imageString.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Decoding base64 string to byte array
            byte[] imageBytes = Base64.getDecoder().decode(imageString);

            // Determining content type
            String contentType = determineImageContentType(imageBytes);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentLength(imageBytes.length);
            headers.setCacheControl("max-age=3600");

            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            System.err.println("Base64 decoding failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            System.err.println("Error retrieving image: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String determineImageContentType(byte[] imageBytes) {
        if (imageBytes.length >= 2) {
            if (imageBytes[0] == (byte) 0xFF && imageBytes[1] == (byte) 0xD8) {
                return "image/jpeg";
            } else if (imageBytes[0] == (byte) 0x89 && imageBytes[1] == (byte) 0x50) {
                return "image/png";
            } else if (imageBytes[0] == (byte) 0x47 && imageBytes[1] == (byte) 0x49) {
                return "image/gif";
            }
        }
        return "image/jpeg";
    }



    private ResponseEntity<?> badRequest (String errorMessage){
        return ResponseEntity.badRequest().body(Map.of("error", errorMessage));

    }


    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("id", user.getId());
        userResponse.put("username", user.getUsername());
        userResponse.put("email", user.getEmail());
        userResponse.put("Name", user.getName());
        userResponse.put("role", user.getRole());

        // Adding profile image URL if user has profile image
        if (userService.hasProfileImage(user.getId())) {
            userResponse.put("profile_image", "/api/auth/user/" + user.getId() + "/profile-image");
        }

        return userResponse;
    }



    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }
}