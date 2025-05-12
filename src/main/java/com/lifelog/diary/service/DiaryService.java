package com.lifelog.diary.service;

import com.lifelog.diary.domain.Diary;
import com.lifelog.diary.domain.User;
import com.lifelog.diary.dto.DiaryReqDto;
import com.lifelog.diary.dto.DiaryResDto;
import com.lifelog.diary.repository.DiaryRepository;
import com.lifelog.diary.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final FollowQuestionService followQuestionService;
    private final GPTService gptService;

    public DiaryService(DiaryRepository diaryRepository, UserRepository userRepository,
                        FollowQuestionService followQuestionService, GPTService gptService) {
        this.diaryRepository = diaryRepository;
        this.userRepository = userRepository;
        this.followQuestionService = followQuestionService;
        this.gptService = gptService;
    }

    public String createContentFromConversation(String conversation) {
        return gptService.generateDiaryFromConversation(conversation);
    }

    public DiaryResDto createDiaryFromConversation(Long userId, String content) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Diary diary = Diary.builder()
                .user(user)
                .content(content)
                .build();
        diaryRepository.save(diary);
        return toResDto(diary);
    }

    public DiaryResDto createDiary(DiaryReqDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Diary diary = Diary.builder()
                .user(user)
                .content(dto.getContent())
                .build();
        diaryRepository.save(diary);
        return toResDto(diary);
    }

    public Optional<DiaryResDto> getById(Long diaryId) {
        return diaryRepository.findByDiaryId(diaryId)
                .map(this::toResDto);
    }

    public List<DiaryResDto> getByUserId(Long userId) {
        return diaryRepository.findByUserId(userId)
                .stream()
                .map(this::toResDto)
                .collect(Collectors.toList());
    }

    public Optional<DiaryResDto> update(String diaryId, String content) {
        return diaryRepository.findById(diaryId)
                .map(diary -> {
                    diary.setContent(content);
                    Diary updated = diaryRepository.save(diary);
                    return toResDto(updated);
                });
    }

    public boolean delete(String diaryId) {
        if (!diaryRepository.existsById(diaryId)) {
            return false;
        }
        diaryRepository.deleteById(diaryId);
        return true;
    }

    private DiaryResDto toResDto(Diary diary) {
        return DiaryResDto.builder()
                .diaryId(diary.getId())
                .userId(diary.getUser().getId())
                .content(diary.getContent())
                .createdAt(diary.getCreatedAt().toString())
                .build();
    }
}
