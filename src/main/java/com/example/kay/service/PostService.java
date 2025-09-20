package com.example.kay.service;

import com.example.kay.model.Post;
import com.example.kay.model.User;
import com.example.kay.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;

    public Post createPost(String title, String description, String tags,
                           MultipartFile imageFile, User user) throws IOException {
        Post post = new Post();
        post.setTitle(title);
        post.setDescription(description);
        post.setTags(tags);
        post.setUser(user);

        // Handle optional cover image
        if (imageFile != null && !imageFile.isEmpty()) {
            post.setCoverImage(imageFile.getBytes());
        }

        return postRepository.save(post);
    }

    // Display all posts
    public List<Post> findAllPosts() {
        return postRepository.findAll();
    }


    public Post findById(Long postId) {
        return postRepository.findById(postId).orElse(null);
    }

    // Find posts by user ID - NEW METHOD
    public List<Post> findPostsByUserId(Long userId) {
        return postRepository.findByUserId(userId);
    }

    // Update post - NEW METHOD
    public Post updatePost(Long postId, String title, String description,
                           String tags, MultipartFile imageFile) throws IOException {
        Post existingPost = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // Update fields
        existingPost.setTitle(title);
        existingPost.setDescription(description);
        existingPost.setTags(tags);
        existingPost.setUpdatedAt(LocalDateTime.now());

        // Handle cover image update
        if (imageFile != null && !imageFile.isEmpty()) {
            existingPost.setCoverImage(imageFile.getBytes());
        }

        return postRepository.save(existingPost);
    }

    // Delete post - NEW METHOD
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        postRepository.delete(post);
    }

    // Check if user owns post - UTILITY METHOD
    public boolean isUserOwnerOfPost(Long postId, Long userId) {
        Post post = findById(postId);
        return post != null && post.getUser().getId().equals(userId);
    }



}
