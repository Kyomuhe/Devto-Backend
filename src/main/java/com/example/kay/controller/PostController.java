package com.example.kay.controller;

import com.example.kay.model.Post;
import com.example.kay.model.User;
import com.example.kay.service.PostService;
import com.example.kay.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PostController {
    private final PostService postService;
    private final UserService userService;

    //create post
    @PostMapping("/create")
    public ResponseEntity<Post> createPost(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String tags,
            @RequestParam(required = false) MultipartFile coverImage,
            @RequestParam Long userId) throws IOException {

        // Get user from UserService (you'll need this)
        User user = userService.findById(userId);

        Post post = postService.createPost(title, description, tags, coverImage, user);
        return ResponseEntity.ok(post);
    }


    @GetMapping("/display")
    public ResponseEntity<List<Post>> getAllPosts() {
        return ResponseEntity.ok(postService.findAllPosts());
    }

    @GetMapping("/image/{postId}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long postId) {
        try {
            Post post = postService.findById(postId);

            if (post != null && post.getCoverImage() != null) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG) // or detect type
                        .body(post.getCoverImage());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
