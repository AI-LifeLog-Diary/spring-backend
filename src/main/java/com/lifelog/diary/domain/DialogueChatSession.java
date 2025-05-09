package com.lifelog.diary.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_session")
@Getter
@NoArgsConstructor
public class DialogueChatSession extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    private DialogueChatSession(User user) {
        this.user = user;
    }

    public static DialogueChatSession createChatSession(User user) {
        return DialogueChatSession.builder()
                .user(user)
                .build();
    }

    public void updateTime() {
        this.updatedAt = LocalDateTime.now();
    }
}
