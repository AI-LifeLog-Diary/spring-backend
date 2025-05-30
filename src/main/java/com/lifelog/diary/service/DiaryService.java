package com.lifelog.diary.service;

import com.lifelog.diary.domain.Diary;
import com.lifelog.diary.domain.User;
import com.lifelog.diary.dto.DiaryReqDto;
import com.lifelog.diary.dto.DiaryResDto;
import com.lifelog.diary.repository.DiaryRepository;
import com.lifelog.diary.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;

    public Optional<DiaryResDto> getById(Long diaryId) {
        return diaryRepository.findById(String.valueOf(diaryId))
                .map(this::convertToDto);
    }

    public List<DiaryResDto> getByUserId(Long userId) {
        return diaryRepository.findByUserId(userId).stream()
                .map(this::convertToDto)
                .toList();
    }

    public Optional<DiaryResDto> update(Long diaryId, String content) {
        return diaryRepository.findById(String.valueOf(diaryId))
                .map(diary -> {
                    diary.setContent(content);
                    Diary updated = diaryRepository.save(diary);
                    return convertToDto(updated);
                });
    }

    public boolean delete(String diaryId) {
        if (!diaryRepository.existsById(diaryId)) {
            return false;
        }
        diaryRepository.deleteById(diaryId);
        return true;
    }

    public DiaryResDto createDiary(DiaryReqDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다"));

        Diary diary = Diary.builder()
                .user(user)
                .content(dto.getContent())
                .build();

        Diary savedDiary = diaryRepository.save(diary);

        return convertToDto(savedDiary);
    }

    private DiaryResDto convertToDto(Diary diary) {
        return DiaryResDto.builder()
                .diaryId(diary.getId())
                .userId(diary.getUser().getId())
                .content(diary.getContent())
                .createdAt(diary.getCreatedAt().toString())
                .build();
    }
}
