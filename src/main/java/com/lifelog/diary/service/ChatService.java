package com.lifelog.diary.service;

import com.lifelog.diary.dto.ChatMessageResDto;
import com.lifelog.diary.dto.DiaryReqDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final GPTService gptService;
    private final DiaryService diaryService;
    private final FollowQuestionService followQuestionService;

    public Flux<ChatMessageResDto> chatAndCreateDiary(Long userId, String conversation, String currentDiary) {
        boolean isDiaryPresent = currentDiary != null && !currentDiary.isBlank();

        if (isDiaryPresent) {
            StringBuilder diaryBuilder = new StringBuilder();
            return gptService.streamDiaryFromConversation(conversation)
                    .doOnNext(diaryBuilder::append)
                    .then(Mono.defer(() -> {
                        String fullDiary = diaryBuilder.toString();
                        diaryService.createDiary(new DiaryReqDto(userId, fullDiary));
                        return Mono.just(ChatMessageResDto.of(null, fullDiary));
                    }))
                    .subscribeOn(Schedulers.boundedElastic())
                    .flux();
        } else {
            boolean isEmptyConversation = conversation == null || conversation.isBlank();
            if (isEmptyConversation) {
                return Flux.just(ChatMessageResDto.of("안녕, 오늘은 어떤 일이 있었어?", null));
            }
            return followQuestionService.streamFollowUpQuestion(conversation)
                    .subscribeOn(Schedulers.boundedElastic());
        }
    }
}