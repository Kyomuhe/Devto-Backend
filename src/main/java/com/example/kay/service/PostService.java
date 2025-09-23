package com.example.kay.service;

import com.example.kay.model.BookMark;
import com.example.kay.model.Post;
import com.example.kay.model.User;
import com.example.kay.repository.BookMarkRepository;
import com.example.kay.repository.PostRepository;
import com.example.kay.repository.UserRepository;
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
    private final BookMarkRepository bookMarkRepository;
    private final UserRepository userRepository;

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

    // Find posts by user ID
    public List<Post> findPostsByUserId(Long userId) {
        return postRepository.findByUserId(userId);
    }

    // Update post
    public Post updatePost(Long postId, String title, String description,
                           String tags, MultipartFile imageFile) throws IOException {
        Post existingPost = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        existingPost.setTitle(title);
        existingPost.setDescription(description);
        existingPost.setTags(tags);
        existingPost.setUpdatedAt(LocalDateTime.now());

        if (imageFile != null && !imageFile.isEmpty()) {
            existingPost.setCoverImage(imageFile.getBytes());
        }

        return postRepository.save(existingPost);
    }

    // Delete post
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        postRepository.delete(post);
    }

//bookmark post
public String bookmarkPost(Long userId, Long postId) {
    User user = userRepository.findById(userId).orElse(null);
    if (user == null) {
        return "User not found";
    }

    Post post = postRepository.findById(postId).orElse(null);
    if (post == null) {
        return "Post not found";
    }

    if (bookMarkRepository.existsByUserAndPost(user, post)) {
        return "Post already bookmarked";
    }

    BookMark bookMark = new BookMark();
    bookMark.setUser(user);
    bookMark.setPost(post);
    bookMarkRepository.save(bookMark);

    return "Post bookmarked successfully";
}

//un bookmark a post
    public String unbookmarkPost(Long userId, Long postId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return "User not found";
        }

        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            return "Post not found";
        }

        BookMark bookMark = bookMarkRepository.findByUserAndPost(user, post).orElse(null);
        if (bookMark == null) {
            return "Bookmark not found";
        }

        bookMarkRepository.delete(bookMark);
        return "Post unbookmarked successfully";
    }

    public List<BookMark> getUserBookmarks(Long userId) {
        return bookMarkRepository.findByUserId(userId);
    }

    public boolean isPostBookmarked(Long userId, Long postId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }

        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            return false;
        }

        return bookMarkRepository.existsByUserAndPost(user, post);
    }

}





