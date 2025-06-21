package com.lifelog.diary.service;

import com.lifelog.diary.common.response.enums.Code;
import com.lifelog.diary.common.response.exception.GeneralException;
import com.lifelog.diary.domain.User;
import com.lifelog.diary.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final GPTService gptService;
    private final FollowQuestionService followQuestionService;
    private final UserRepository userRepository;
    private final AccountService accountService;

    public Flux<String> chatAndCreateDiary(String conversation, String currentDiary) {
        return Mono.fromCallable(accountService::getCurrentUser)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(user -> {
                    boolean isDiaryPresent = currentDiary != null && !currentDiary.isBlank();

                    if (isDiaryPresent) {
                        StringBuilder diaryBuilder = new StringBuilder();
                        return gptService.streamDiaryFromConversation(conversation)
                                .publishOn(Schedulers.boundedElastic())
                                .doOnNext(diaryBuilder::append)
                                .map(token -> "DIARY: " + token);
                    }

                    if (conversation == null || conversation.isBlank()) {
                        return Flux.just("data: " + "안녕, 오늘은 어떤 일이 있었어?");
                    }

                    Flux<String> followUpFlux = followQuestionService.streamFollowUpQuestionFromConversation(conversation)
                            .map(token -> "FOLLOWUP: " + token);

                    Flux<String> diaryFlux = gptService.streamDiaryFromConversation(conversation)
                            .map(token -> "DIARY: " + token);

                    return Flux.concat(followUpFlux, diaryFlux);
                });
    }

}