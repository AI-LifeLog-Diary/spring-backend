package com.lifelog.diary.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "diary")
@Getter
@NoArgsConstructor
public class Diary extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Setter
    @Column(name = "content", nullable = false, length = 10000)
    private String content;

    @Column(name = "imageUrl")
    private String imageUrl;

    @Builder
    private Diary(User user, String content, String imageUrl) {
        this.user = user;
        this.content = content;
        this.imageUrl = imageUrl;
    }

}
