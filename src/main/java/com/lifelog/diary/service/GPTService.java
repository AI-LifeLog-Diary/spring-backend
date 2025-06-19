package com.lifelog.diary.service;

import com.lifelog.diary.client.DiaryChatClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class GPTService {

    private final DiaryChatClient diaryChatClient;

    public Flux<String> streamDiaryFromConversation(String conversation, String accessToken) {
        return diaryChatClient.streamDiary(conversation, accessToken);
    }
}