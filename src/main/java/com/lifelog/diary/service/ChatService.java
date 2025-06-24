package com.lifelog.diary.service;

import com.lifelog.diary.domain.User;
import com.lifelog.diary.dto.ChatMessageResDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final GPTService gptService;
    private final FollowQuestionService followQuestionService;

    public ChatMessageResDto chatAndCreateDiary(String conversation, String currentDiary, User user) {
        boolean isDiaryPresent = currentDiary != null && !currentDiary.isBlank();

        if (isDiaryPresent) {
            String diaryContent = gptService.createDiaryFromConversation(conversation, currentDiary);
            return new ChatMessageResDto(null, diaryContent);
        }

        if (conversation == null || conversation.isBlank()) {
            return new ChatMessageResDto("안녕하세요, " + user.getNickname() + "님! " + "오늘은 어떤 일이 있었나요?", null);
        }

        String followUp = followQuestionService.createFollowUpQuestionFromConversation(conversation);
        String diaryContent = gptService.createDiaryFromConversation(conversation, currentDiary);

        return new ChatMessageResDto(followUp, diaryContent);
    }
}