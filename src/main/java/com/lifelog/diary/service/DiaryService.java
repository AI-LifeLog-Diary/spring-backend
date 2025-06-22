package com.lifelog.diary.service;

import com.lifelog.diary.domain.Diary;
import com.lifelog.diary.domain.User;
import com.lifelog.diary.dto.DiaryResDto;
import com.lifelog.diary.image.service.ImageService;
import com.lifelog.diary.repository.DiaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final ImageService imageService;

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

    public boolean delete(String diaryId, User currentUser) {
//        if (!diaryRepository.existsById(diaryId)) {
//            return false;
//        }
//        diaryRepository.deleteById(diaryId);
//        return true;
        Optional<Diary> optionalDiary = diaryRepository.findById(diaryId);

        if (optionalDiary.isEmpty()) {
            return false;
        }

        Diary diary = optionalDiary.get();

        if (!diary.getUser().getId().equals(currentUser.getId())) {
            return false;
        }

        diaryRepository.deleteById(diaryId);
        return true;
    }

    public DiaryResDto createDiary(User user, String diaryContent, String imageUrl) {
        String diaryImageUrl = null;

        if (imageUrl != null && !imageUrl.isBlank()) {
            diaryImageUrl = imageService.uploadImageFromUrl(imageUrl);
        }

        Diary diary = Diary.builder()
                .user(user)
                .content(diaryContent)
                .imageUrl(diaryImageUrl)
                .build();

        Diary savedDiary = diaryRepository.save(diary);

        return convertToDto(savedDiary);
    }

    private DiaryResDto convertToDto(Diary diary) {
        String presignedImageUrl = null;

        if (diary.getImageUrl() != null && !diary.getImageUrl().isBlank()) {
            presignedImageUrl = imageService.generatePresignedUrlFromFullUrl(diary.getImageUrl());
        }

        return DiaryResDto.builder()
                .diaryId(diary.getId())
                .content(diary.getContent())
                .imageUrl(presignedImageUrl)
                .createdAt(diary.getCreatedAt().toString())
                .build();
    }
}
