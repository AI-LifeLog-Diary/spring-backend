package com.lifelog.diary.service;

import com.lifelog.diary.client.ChatClient;
import com.lifelog.diary.common.aes.AESUtil;
import com.lifelog.diary.common.response.enums.Code;
import com.lifelog.diary.common.response.exception.GeneralException;
import com.lifelog.diary.domain.DialogueChat;
import com.lifelog.diary.domain.DialogueChatSession;
import com.lifelog.diary.domain.User;
import com.lifelog.diary.domain.enums.ChatRole;
import com.lifelog.diary.dto.*;
import com.lifelog.diary.repository.DialogueChatRepository;
import com.lifelog.diary.repository.DialogueChatSessionRepository;
import com.lifelog.diary.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional
public class DialogueChatService {

    private final ChatClient chatClient;
    private final UserRepository userRepository;
    private final DialogueChatRepository dialogueChatRepository;
    private final DialogueChatSessionRepository dialogueChatSessionRepository;
    private final AESUtil aesUtil;

    public Flux<String> streamDialogue(DialogueChatReqDto dialogueChatReqDto) throws Exception {

        User user = userRepository.findById(dialogueChatReqDto.getUserId())
                .orElseThrow(() -> new GeneralException(Code.USER_NOT_FOUND, "존재하지 않는 사용자입니다."));

        DialogueChatSession session;

        // 첫 대화
        if (dialogueChatReqDto.getUserInput() == null) {
            session = DialogueChatSession.createChatSession(user);
            dialogueChatSessionRepository.save(session);
        }
        // 두 번째 이후 대화
        else {
            session = dialogueChatSessionRepository.findTopByUserOrderByCreatedAtDesc(user)
                    .orElseThrow(() -> new GeneralException(Code.SESSION_NOT_FOUND));

            DialogueChat userChat = DialogueChat.createDialogueChat(
                    user,
                    session,
                    aesUtil.encrypt(dialogueChatReqDto.getUserInput()),
                    ChatRole.USER
            );
            dialogueChatRepository.save(userChat);
            session.updateTime();
        }

        StringBuilder buffer = new StringBuilder();

        return chatClient.streamDialogue(dialogueChatReqDto)
                .doOnNext(chunk -> {
                    String clean = chunk
                            .replaceFirst("^data:data:", "")  // 한 줄이 data:data:로 시작하면 제거
                            .replaceFirst("^data:", "");    // 또는 data:로 시작하면 제거

                    if (clean.matches("^\\s{2,}.*")) {
                        clean = " " + clean.trim();  // 앞 공백 1개 + 본문
                    } else {
                        clean = clean.trim();  // 그 외는 앞뒤 공백 제거
                    }

                    buffer.append(clean);
                })
                .doOnComplete(() -> {
                    DialogueChat assistantChat;
                    try {
                        assistantChat = DialogueChat.createDialogueChat(
                                user,
                                session,
                                aesUtil.encrypt(buffer.toString()),
                                ChatRole.ASSISTANT
                        );
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    dialogueChatRepository.save(assistantChat);
                });
    }

    public List<DialogueChatSessionResDto> getChatList(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(Code.USER_NOT_FOUND, "존재하지 않는 사용자입니다."));

        List<DialogueChatSession> dialogueChatSessions = dialogueChatSessionRepository.findAllTopByUserOrderByCreatedAtDesc(user);

        return dialogueChatSessions.stream()
                .map(dialogueChatSession -> {

                    DialogueChat lastChat = dialogueChatRepository.findTopByDialogueChatSessionOrderByCreatedAtDesc(dialogueChatSession);

                    LastMessageInfoDto lastMessage = null;

                    if (lastChat != null) {
                        try {
                            lastMessage = LastMessageInfoDto.builder()
                                    .messageId(lastChat.getId())
                                    .chatRole(lastChat.getChatRole())
                                    .message(aesUtil.decrypt(lastChat.getMessage()))
                                    .createdAt(lastChat.getCreatedAt())
                                    .build();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return DialogueChatSessionResDto.builder()
                            .sessionId(dialogueChatSession.getId())
                            .userId(userId)
                            .lastMessageInfoDto(lastMessage)
                            .createdAt(dialogueChatSession.getCreatedAt())
                            .updatedAt(dialogueChatSession.getUpdatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }


    public DialogueChatMessageResDto getChatDetail(Long userId, Long sessionId, Long cursor, int size) {
        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "id"));

        List<DialogueChat> messages = (cursor == null)
                ? dialogueChatRepository.findByUserIdAndDialogueChatSessionIdOrderByIdDesc(userId, sessionId, pageable)
                : dialogueChatRepository.findByUserIdAndDialogueChatSessionIdAndIdLessThanOrderByIdDesc(userId, sessionId, cursor, pageable);

        List<MessageInfoDto> messageInfoDtoList = messages.stream()
                .map(chat -> {
                    try {
                        return MessageInfoDto.from(chat, aesUtil);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());

        Long nextCursor = messageInfoDtoList.isEmpty() ? null : messageInfoDtoList.get(messageInfoDtoList.size() - 1).getMessageId();
        boolean hasNextPage = nextCursor != null && dialogueChatRepository.existsByDialogueChatSessionIdAndIdLessThan(sessionId, nextCursor);

        return DialogueChatMessageResDto.builder()
                .messages(messageInfoDtoList)
                .nextCursor(nextCursor)
                .hasNextPage(hasNextPage)
                .build();
    }
}
