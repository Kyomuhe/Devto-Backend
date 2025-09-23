package com.example.kay.controller;

import com.example.kay.model.BookMark;
import com.example.kay.model.Post;
import com.example.kay.model.User;
import com.example.kay.service.PostService;
import com.example.kay.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    //displaying posts for a particular user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Post>> getPostsByUserId(@PathVariable Long userId) {
        try {
            List<Post> userPosts = postService.findPostsByUserId(userId);
            return ResponseEntity.ok(userPosts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Getting a single post by ID
    @GetMapping("/{postId}")
    public ResponseEntity<Post> getPostById(@PathVariable Long postId) {
        try {
            Post post = postService.findById(postId);
            if (post != null) {
                return ResponseEntity.ok(post);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Update post
    @PutMapping("/{postId}")
    public ResponseEntity<Post> updatePost(
            @PathVariable Long postId,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String tags,
            @RequestParam(required = false) MultipartFile coverImage,
            @RequestParam Long userId) throws IOException {

        try {
            Post existingPost = postService.findById(postId);
            if (existingPost == null) {
                return ResponseEntity.notFound().build();
            }

            if (!existingPost.getUser().getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Post updatedPost = postService.updatePost(postId, title, description, tags, coverImage);
            return ResponseEntity.ok(updatedPost);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Delete post
    @DeleteMapping("/delete/{postId}")
    public ResponseEntity<String> deletePost(@PathVariable Long postId, @RequestParam(required = false) Long userId) {
        try {
            Post existingPost = postService.findById(postId);
            if (existingPost == null) {
                return ResponseEntity.notFound().build();
            }

            if (userId != null && !existingPost.getUser().getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You don't have permission to delete this post");
            }

            postService.deletePost(postId);
            return ResponseEntity.ok("Post deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete post");
        }
    }

    @PostMapping("/bookmark/{userId}/{postId}")
    public ResponseEntity<String> bookmarkPost(@PathVariable Long userId, @PathVariable Long postId) {
        String result = postService.bookmarkPost(userId, postId);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/unbookmark/{userId}/{postId}")
    public ResponseEntity<String> unbookmarkPost(@PathVariable Long userId, @PathVariable Long postId) {
        String result = postService.unbookmarkPost(userId, postId);
        return ResponseEntity.ok(result);
    }


    @GetMapping("/displayBookMarks/{userId}")
    public ResponseEntity<List<BookMark>> getUserBookmarks(@PathVariable Long userId) {
        return ResponseEntity.ok(postService.getUserBookmarks(userId));
    }

    @GetMapping("/checkBookmark/{userId}/{postId}")
    public ResponseEntity<Boolean> checkBookmarkStatus(@PathVariable Long userId, @PathVariable Long postId) {
        try {
            boolean isBookmarked = postService.isPostBookmarked(userId, postId);
            return ResponseEntity.ok(isBookmarked);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

}
