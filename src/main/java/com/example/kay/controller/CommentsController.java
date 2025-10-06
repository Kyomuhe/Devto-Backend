package com.example.kay.controller;

import com.example.kay.model.Comments;
import com.example.kay.service.CommentsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CommentsController {

    private final CommentsService commentsService;


    @PostMapping("/create")
    public ResponseEntity<?> createComment(@RequestBody CreateCommentRequest request) {
        try {
            Comments comment = commentsService.createComment(
                    request.getPostId(),
                    request.getUserId(),
                    request.getContent()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(comment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/display/{postId}")
    public ResponseEntity<?> getCommentsByPost(@PathVariable Long postId) {
        try {
            List<Comments> comments = commentsService.getCommentsByPost(postId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }


    /**
     * Get comment count for a post
     */
    @GetMapping("/count/{postId}")
    public ResponseEntity<?> getCommentCount(@PathVariable Long postId) {
        try {
            long count = commentsService.getCommentCount(postId);
            return ResponseEntity.ok(Map.of("commentCount", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }




    // Request DTOs
    public static class CreateCommentRequest {
        private Long postId;
        private Long userId;
        private String content;

        // Getters and setters
        public Long getPostId() { return postId; }
        public void setPostId(Long postId) { this.postId = postId; }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }


}
