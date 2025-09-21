package com.example.kay.controller;

import com.example.kay.model.Likes;
import com.example.kay.service.LikesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LikesController {

    private final LikesService likesService;


    @PostMapping("/like")
    public ResponseEntity<?> likePost(@RequestBody LikeRequest request) {
        try {
            boolean liked = likesService.likePost(request.getPostId(), request.getUserId());
            if (liked) {
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(Map.of("message", "Post liked successfully", "liked", true));
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("message", "Post already liked", "liked", true));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }


    @DeleteMapping("/unlike")
    public ResponseEntity<?> unlikePost(@RequestBody LikeRequest request) {
        try {
            boolean unliked = likesService.unlikePost(request.getPostId(), request.getUserId());
            if (unliked) {
                return ResponseEntity.ok(Map.of("message", "Post unliked successfully", "liked", false));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Like not found", "liked", false));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Toggle like status
     */
    @PostMapping("/toggle")
    public ResponseEntity<?> toggleLike(@RequestBody LikeRequest request) {
        try {
            boolean isNowLiked = likesService.toggleLike(request.getPostId(), request.getUserId());
            String message = isNowLiked ? "Post liked successfully" : "Post unliked successfully";
            return ResponseEntity.ok(Map.of("message", message, "liked", isNowLiked));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Check if user has liked a post
     */
    @GetMapping("/check/{postId}/{userId}")
    public ResponseEntity<?> hasUserLikedPost(
            @PathVariable Long postId,
            @PathVariable Long userId) {
        try {
            boolean hasLiked = likesService.hasUserLikedPost(postId, userId);
            return ResponseEntity.ok(Map.of("liked", hasLiked));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get like count for a post
     */
    @GetMapping("/count/{postId}")
    public ResponseEntity<?> getLikeCount(@PathVariable Long postId) {
        try {
            long count = likesService.getLikeCount(postId);
            return ResponseEntity.ok(Map.of("likeCount", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all posts liked by a user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getLikesByUser(@PathVariable Long userId) {
        try {
            List<Likes> likes = likesService.getLikesByUser(userId);
            return ResponseEntity.ok(likes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }


    // Request DTO
    public static class LikeRequest {
        private Long postId;
        private Long userId;

        // Getters and setters
        public Long getPostId() { return postId; }
        public void setPostId(Long postId) { this.postId = postId; }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
    }
}
