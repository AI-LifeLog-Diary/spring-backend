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

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Setter
    @Column(name = "content", nullable = false)
    private String content;

    @Builder
    private Diary(User user, String content) {
        this.user = user;
        this.content = content;
    }

}
