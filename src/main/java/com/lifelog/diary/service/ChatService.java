package com.lifelog.diary.service;

import com.lifelog.diary.dto.ChatMessageResDto;
import com.lifelog.diary.dto.DiaryResDto;
import org.springframework.stereotype.Service;

import java.util.List;
//
//@Service
//public class ChatService {
//
//    private final FollowQuestionService followQuestionService;
//    private final DiaryService diaryService;
//
//    public ChatService(FollowQuestionService followQuestionService, DiaryService diaryService) {
//        this.followQuestionService = followQuestionService;
//        this.diaryService = diaryService;
//    }
//
//    public ChatMessageResDto handleChatLogic(Long userId, String conversation, String currentDiary) {
//        boolean isFinished = currentDiary != null && !currentDiary.isBlank();
//
//        String diaryContent = diaryService.createContentFromConversation(conversation);
//
//        String followUpQuestion = isFinished ? null : followQuestionService.generateFollowUpQuestion(conversation);
//        DiaryResDto diaryResDto = isFinished ? diaryService.createDiaryFromConversation(userId, diaryContent) : null;
//
//        return new ChatMessageResDto(followUpQuestion, diaryContent, diaryResDto);
//    }
//}
