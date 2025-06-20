package com.lifelog.diary.service;

import com.lifelog.diary.client.ChatClient;
import com.lifelog.diary.common.aes.AESUtil;
import com.lifelog.diary.common.response.enums.Code;
import com.lifelog.diary.common.response.exception.GeneralException;
import com.lifelog.diary.domain.*;
import com.lifelog.diary.domain.enums.ChatRole;
import com.lifelog.diary.domain.enums.Hobby;
import com.lifelog.diary.dto.*;
import com.lifelog.diary.repository.DialogueChatRepository;
import com.lifelog.diary.repository.DialogueChatSessionRepository;
import com.lifelog.diary.repository.DiaryRepository;
import com.lifelog.diary.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuples;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class DialogueChatService {

    private final AccountService accountService;
    private final ChatClient chatClient;
    private final UserRepository userRepository;
    private final DialogueChatRepository dialogueChatRepository;
    private final DialogueChatSessionRepository dialogueChatSessionRepository;
    private final DiaryRepository diaryRepository;
    private final AESUtil aesUtil;

    public Flux<String> streamDialogue(DialogueChatReqDto dialogueChatReqDto) throws Exception {

        User user = accountService.getCurrentUser();

        DialogueChatSession session;

        Diary latestDiary = diaryRepository.findTopByUserOrderByCreatedAtDesc(user);

        // 첫 대화
        if (dialogueChatReqDto.getUserInput() == null) {
            session = DialogueChatSession.createChatSession(user, latestDiary);
            dialogueChatSessionRepository.save(session);

            dialogueChatReqDto = getDiaryAndUserInfo(user, dialogueChatReqDto);
        }
        // 두 번째 이후 대화
        else {
            session = dialogueChatSessionRepository.findTopByUserOrderByCreatedAtDesc(user)
                    .orElseThrow(() -> new GeneralException(Code.SESSION_NOT_FOUND, "채팅방이 존재하지 않습니다."));

            dialogueChatReqDto = getDiaryAndUserInfo(user, dialogueChatReqDto);

            DialogueChat userChat = DialogueChat.createDialogueChat(
                    user,
                    session,
                    aesUtil.encrypt(dialogueChatReqDto.getUserInput()),
                    user.getNickname(),
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
                                "챗봇",
                                ChatRole.ASSISTANT
                        );
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    dialogueChatRepository.save(assistantChat);
                });
    }

    public Flux<String> streamDialogueWithNoSession(DialogueChatWithNoSessionReqDto dto) {
        User user = accountService.getCurrentUser();

        return Mono.fromCallable(() -> {

                    Diary latestDiary = diaryRepository.findTopByUserOrderByCreatedAtDesc(user);

                    if (latestDiary == null) {
                        throw new GeneralException(Code.DIARY_NOT_FOUND, "최근 일기가 존재하지 않습니다.");
                    }

                    DialogueChatSession session;

                    if (dto.getUserInput() == null) {
                        DialogueChatWithNoSessionReqDto updatedDto = getDiaryAndUserInfoWithNoSession(user, dto);
                        Optional<DialogueChat> latestChat = dialogueChatRepository.findTopByUserOrderByCreatedAtDesc(user);

                        if (latestChat.isEmpty()) {
                            session = DialogueChatSession.createChatSession(user, latestDiary);
                            dialogueChatSessionRepository.save(session);

                            updatedDto = updatedDto.toBuilder()
                                    .todayDiary(latestDiary.getContent())
                                    .newChat(true)
                                    .build();

                            return Tuples.of(user, session, updatedDto);
                        }

                        Long latestDiaryId = latestDiary.getId();
                        Long latestChatDiaryId = latestChat
                                .map(chat -> chat.getDialogueChatSession().getDiary())
                                .filter(Objects::nonNull)
                                .map(Diary::getId)
                                .orElse(null);

                        if (Objects.equals(latestDiaryId, latestChatDiaryId)) {
                            throw new GeneralException(Code.DUPLICATE_SESSION, "이미 최신 일기에 대한 세션이 존재합니다.");
                        }

                        session = DialogueChatSession.createChatSession(user, latestDiary);
                        dialogueChatSessionRepository.save(session);

                        updatedDto = updatedDto.toBuilder()
                                .todayDiary(latestDiary.getContent())
                                .newChat(true)
                                .build();

                        return Tuples.of(user, session, updatedDto);

                    } else {
                        session = dialogueChatSessionRepository.findTopByUserOrderByCreatedAtDesc(user)
                                .orElseThrow(() -> new GeneralException(Code.SESSION_NOT_FOUND, "채팅방이 존재하지 않습니다."));

                        DialogueChatWithNoSessionReqDto updatedDto = getDiaryAndUserInfoWithNoSession(user, dto);

                        DialogueChat userChat = DialogueChat.createDialogueChat(
                                user,
                                session,
                                aesUtil.encrypt(updatedDto.getUserInput()),
                                user.getNickname(),
                                ChatRole.USER
                        );
                        dialogueChatRepository.save(userChat);
                        session.updateTime();

                        return Tuples.of(user, session, updatedDto);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(tuple -> {
                    User u = tuple.getT1();
                    DialogueChatSession session = tuple.getT2();
                    DialogueChatWithNoSessionReqDto updatedDto = tuple.getT3();

                    return streamWithNoSession(u, session, aesUtil, updatedDto)
                            .switchIfEmpty(Flux.just("챗봇 응답이 없습니다."));
                })
                .onErrorResume(ex -> {
                    String message = (ex instanceof GeneralException)
                            ? ((GeneralException) ex).getMessage()
                            : "알 수 없는 오류가 발생했습니다.";
                    return Flux.just("error: " + message);
                });
    }


    @Transactional
    public List<DialogueChatSessionResDto> getChatList(Long userId) {

        try {
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
        } catch (Exception e) {
            throw new GeneralException(Code.INTERNAL_ERROR, "채팅방 목록 조회 도중 알 수 없는 에러가 발생했습니다.");
        }

    }

    @Transactional
    public DialogueChatMessageResDto getChatDetail(Long userId, Long sessionId, Long cursor, int size) {

        try {
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
        } catch (Exception e) {
            throw new GeneralException(Code.INTERNAL_ERROR, "채팅 목록 조회 도중 알 수 없는 오류가 발생했습니다.");
        }
    }

    @Transactional
    public DialogueChatMessageWithNoSessionResDto getChatDetailWithNoSession(Long cursor, int size) {

        try {
            Long userId = accountService.getCurrentUserId();

            Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "id"));

            List<DialogueChat> messages = (cursor == null)
                    ? dialogueChatRepository.findByUserIdOrderByIdDesc(userId, pageable)
                    : dialogueChatRepository.findByUserIdAndIdLessThanOrderByIdDesc(userId, cursor, pageable);

            List<MessageInfoDtoWithNoSessionDto> messageInfoDtoList = messages.stream()
                    .map(chat -> {
                        try {
                            return MessageInfoDtoWithNoSessionDto.from(chat, aesUtil);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());

            Long nextCursor = messageInfoDtoList.isEmpty() ? null : messageInfoDtoList.get(messageInfoDtoList.size() - 1).getMessageId();
            boolean hasNextPage = nextCursor != null && dialogueChatRepository.existsByIdLessThan(nextCursor);

            return DialogueChatMessageWithNoSessionResDto.builder()
                    .messages(messageInfoDtoList)
                    .nextCursor(nextCursor)
                    .hasNextPage(hasNextPage)
                    .build();
        } catch (Exception e) {
            throw new GeneralException(Code.INTERNAL_ERROR, "채팅 목록 조회 도중 알 수 없는 오류가 발생했습니다.");
        }
    }

    private DialogueChatReqDto getDiaryAndUserInfo(User user, DialogueChatReqDto dialogueChatReqDto) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
        Optional<Diary> diary = diaryRepository.findTopByUserAndCreatedAtBetween(user, startOfDay, endOfDay);

        String diaryContent = diary.map(Diary::getContent).orElse(null);

        List<Hobby> hobbyList = user.getHobbyList().stream()
                .map(UserHobby::getHobby)
                .toList();

        return dialogueChatReqDto.toBuilder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .gender(user.getGender())
                .hobby(hobbyList)
                .todayDiary(diaryContent)
                .userInput(dialogueChatReqDto.getUserInput())
                .build();
    }

    private DialogueChatWithNoSessionReqDto getDiaryAndUserInfoWithNoSession(User user, DialogueChatWithNoSessionReqDto dialogueChatWithNoSessionReqDto) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
        Optional<Diary> diary = diaryRepository.findTopByUserAndCreatedAtBetween(user, startOfDay, endOfDay);

        String diaryContent = diary.map(Diary::getContent).orElse(null);

        List<Hobby> hobbyList = user.getHobbyList().stream()
                .map(UserHobby::getHobby)
                .toList();

        return dialogueChatWithNoSessionReqDto.toBuilder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .gender(user.getGender())
                .hobby(hobbyList)
                .todayDiary(diaryContent)
                .userInput(dialogueChatWithNoSessionReqDto.getUserInput())
                .newChat(false)
                .build();
    }

    private Flux<String> streamWithNoSession(User user, DialogueChatSession session, AESUtil aesUtil, DialogueChatWithNoSessionReqDto dto) {
        StringBuilder buffer = new StringBuilder();

        return chatClient.streamDialogueWithNoSession(dto)
                .map(chunk -> {
                    String clean = chunk
                            .replaceFirst("^data:data:", "")
                            .replaceFirst("^data:", "")
                            .strip();
                    if (clean.matches("^\\s{2,}.*")) {
                        clean = " " + clean.trim();
                    }
                    buffer.append(clean);
                    return clean;
                })
                .onErrorResume(e -> {
                    log.error("stream 중 에러 발생", e);
                    return Flux.just("error: 챗봇 응답 도중 오류가 발생했습니다.");
                })
                .concatWith(
                        Mono.fromRunnable(() -> {
                            try {
                                DialogueChat assistantChat = DialogueChat.createDialogueChat(
                                        user,
                                        session,
                                        aesUtil.encrypt(buffer.toString()),
                                        "챗봇",
                                        ChatRole.ASSISTANT
                                );
                                dialogueChatRepository.save(assistantChat);
                            } catch (Exception e) {
                                log.error("Assistant 메시지 저장 실패", e);
                            }
                        }).subscribeOn(Schedulers.boundedElastic()).thenMany(Flux.empty())
                );
    }


}
