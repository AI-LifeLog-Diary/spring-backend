package com.lifelog.diary.domain;

import com.lifelog.diary.domain.enums.ChatRole;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dialogue_chat")
@Getter
@NoArgsConstructor
public class DialogueChat extends BaseCreatedTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "chat_session_id", nullable = false)
    private DialogueChatSession dialogueChatSession;

    @Column(name = "message", nullable = false, length = 10000)
    private String message;

    @Enumerated(value = EnumType.STRING)
    private ChatRole chatRole;

    @Builder
    private DialogueChat(User user, DialogueChatSession dialogueChatSession,
                        String message, ChatRole chatRole) {
        this.user = user;
        this.dialogueChatSession = dialogueChatSession;
        this.message = message;
        this.chatRole = chatRole;
    }

    // 생성 메서드
    public static DialogueChat createDialogueChat(User user, DialogueChatSession dialogueChatSession,
                                                  String message, ChatRole chatRole) {
        return DialogueChat.builder()
                .user(user)
                .dialogueChatSession(dialogueChatSession)
                .message(message)
                .chatRole(chatRole)
                .build();
    }
}
