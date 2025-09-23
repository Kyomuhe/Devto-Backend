package com.example.kay.repository;

import com.example.kay.model.BookMark;
import com.example.kay.model.User;
import com.example.kay.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookMarkRepository extends JpaRepository<BookMark, Long> {
    Optional<BookMark> findByUserAndPost(User user, Post post);
    boolean existsByUserAndPost(User user, Post post);
    List<BookMark> findByUserId(Long userId);

}

