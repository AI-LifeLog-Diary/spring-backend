package com.lifelog.diary.service;

import com.lifelog.diary.client.QuestionChatClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class FollowQuestionService {

    private final QuestionChatClient questionChatClient;

    public String createFollowUpQuestionFromConversation(String conversation) {
        return questionChatClient.getFollowUpQuestion(conversation);
    }
}
