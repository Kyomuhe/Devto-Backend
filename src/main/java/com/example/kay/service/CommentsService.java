package com.example.kay.service;

import com.example.kay.model.Comments;
import com.example.kay.model.Post;
import com.example.kay.model.User;
import com.example.kay.repository.CommentsRepository;
import com.example.kay.repository.PostRepository;
import com.example.kay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class CommentsService {

    private final CommentsRepository commentsRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    /**
     * Create a new comment
     */
    public Comments createComment(Long postId, Long userId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be empty");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Comments comment = new Comments();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(content.trim());

        return commentsRepository.save(comment);
    }

    /**
     * Get all comments for a specific post
     */
    @Transactional(readOnly = true)
    public List<Comments> getCommentsByPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        return commentsRepository.findCommentsWithUserByPost(post);
    }


    /**
     * Get comment count for a post
     */
    @Transactional(readOnly = true)
    public long getCommentCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        return commentsRepository.countByPost(post);
    }




}
