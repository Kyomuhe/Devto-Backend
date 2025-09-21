package com.example.kay.service;

import com.example.kay.model.Likes;
import com.example.kay.model.Post;
import com.example.kay.model.User;
import com.example.kay.repository.LikesRepository;
import com.example.kay.repository.PostRepository;
import com.example.kay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class LikesService {

    private final LikesRepository likesRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    /**
     * Like a post
     */
    public boolean likePost(Long postId, Long userId) {
        // Find post
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Check if user already liked the post
        if (likesRepository.existsByUserAndPost(user, post)) {
            return false; // Already liked
        }

        // Create new like
        Likes like = new Likes();
        like.setUser(user);
        like.setPost(post);

        likesRepository.save(like);
        return true; // Successfully liked
    }

    /**
     * Unlike a post
     */
    public boolean unlikePost(Long postId, Long userId) {
        // Find post
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Check if like exists
        Optional<Likes> likeOpt = likesRepository.findByUserAndPost(user, post);

        if (likeOpt.isPresent()) {
            likesRepository.delete(likeOpt.get());
            return true; // Successfully unliked
        }

        return false; // Like doesn't exist
    }

    /**
     * Toggle like status (like if not liked, unlike if liked)
     */
    public boolean toggleLike(Long postId, Long userId) {
        // Find post
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (likesRepository.existsByUserAndPost(user, post)) {
            return !unlikePost(postId, userId); // Returns true if liked (unliked successfully)
        } else {
            return likePost(postId, userId); // Returns true if liked successfully
        }
    }

    /**
     * Check if user has liked a post
     */
    @Transactional(readOnly = true)
    public boolean hasUserLikedPost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        return likesRepository.existsByUserAndPost(user, post);
    }

    /**
     * Get like count for a post
     */
    @Transactional(readOnly = true)
    public long getLikeCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        return likesRepository.countByPost(post);
    }

    /**
     * Get all likes by user
     */
    @Transactional(readOnly = true)
    public List<Likes> getLikesByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return likesRepository.findByUser(user);
    }

    /**
     * Get all likes by post
     */
    @Transactional(readOnly = true)
    public List<Likes> getLikesByPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        return likesRepository.findByPost(post);
    }
}
