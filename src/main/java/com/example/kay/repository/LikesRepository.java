package com.example.kay.repository;

import com.example.kay.model.Likes;
import com.example.kay.model.Post;
import com.example.kay.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikesRepository extends JpaRepository<Likes, Integer> {

    // Check if a user has already liked a post
    boolean existsByUserAndPost(User user, Post post);

    // Find a specific like by user and post
    Optional<Likes> findByUserAndPost(User user, Post post);

    // Count total likes for a post
    long countByPost(Post post);

    // Find all likes by a user
    List<Likes> findByUser(User user);

    // Find all likes for a post
    List<Likes> findByPost(Post post);

    // Delete a like by user and post
    void deleteByUserAndPost(User user, Post post);
}