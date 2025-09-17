package com.example.kay.service;

import com.example.kay.model.Post;
import com.example.kay.model.User;
import com.example.kay.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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


}
