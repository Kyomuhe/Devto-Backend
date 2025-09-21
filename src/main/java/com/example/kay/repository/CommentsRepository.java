package com.example.kay.repository;


import com.example.kay.model.Comments;
import com.example.kay.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentsRepository extends JpaRepository<Comments, Integer> {

    // Find all comments for a specific post, ordered by creation time
    List<Comments> findByPostOrderByIdDesc(Post post);

    // Find all comments by a specific user
    List<Comments> findByUser_Id(Integer userId);

    // Count comments for a specific post
    long countByPost(Post post);

    // Custom query to get comments with user info for a post
    @Query("SELECT c FROM Comments c JOIN FETCH c.user WHERE c.post = :post ORDER BY c.id DESC")
    List<Comments> findCommentsWithUserByPost(@Param("post") Post post);
}
