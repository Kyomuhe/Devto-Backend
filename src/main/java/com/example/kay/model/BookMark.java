package com.example.kay.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "book_marks")
public class BookMark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookMarkId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "postid")
    private Post post;
}
