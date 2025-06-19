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

    @OneToOne
    @JoinColumn(name = "diary_id")
    private Diary diary;


    @Builder
    private DialogueChatSession(User user, Diary diary) {
        this.user = user;
        this.diary = diary;
    }

    public static DialogueChatSession createChatSession(User user, Diary diary) {
        return DialogueChatSession.builder()
                .user(user)
                .diary(diary)
                .build();
    }

    public void updateTime() {
        this.updatedAt = LocalDateTime.now();
    }
}
