package com.lifelog.diary.controller;

import com.lifelog.diary.common.response.dto.MetaResponseDto;
import com.lifelog.diary.common.response.dto.ResponseDto;
import com.lifelog.diary.common.response.enums.Code;
import com.lifelog.diary.dto.DiaryReqDto;
import com.lifelog.diary.dto.DiaryUpdateDto;
import com.lifelog.diary.dto.DiaryResDto;
import com.lifelog.diary.service.DiaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/diaries")
public class DiaryController {

    private final DiaryService diaryService;

    public DiaryController(DiaryService diaryService) {
        this.diaryService = diaryService;
    }

    @PostMapping
    public ResponseEntity<ResponseDto<DiaryResDto>> createDiary(
            @RequestParam Long userId,
            @RequestParam String content
    ) {
        DiaryReqDto dto = new DiaryReqDto(userId, content);
        DiaryResDto diary = diaryService.createDiary(dto);

        MetaResponseDto meta = new MetaResponseDto(Code.OK, "일기 생성 성공");
        List<DiaryResDto> data = List.of(diary);

        return ResponseEntity
                .status(meta.getStatus())
                .body(new ResponseDto<>(meta, data));
    }

    @GetMapping("/{diaryId}")
    public ResponseEntity<ResponseDto<DiaryResDto>> getDiary(@PathVariable Long diaryId) {
        Optional<DiaryResDto> diary = diaryService.getById(diaryId);

        if (diary.isPresent()) {
            MetaResponseDto meta = new MetaResponseDto(Code.OK, "일기 조회 성공");
            return ResponseEntity.ok(new ResponseDto<>(meta, List.of(diary.get())));
        } else {
            MetaResponseDto meta = new MetaResponseDto(Code.NOT_FOUND, "일기를 찾을 수 없습니다.");
            return ResponseEntity.status(meta.getStatus()).body(new ResponseDto<>(meta, null));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ResponseDto<DiaryResDto>> getDiaries(@PathVariable Long userId) {
        List<DiaryResDto> diaries = diaryService.getByUserId(userId);
        MetaResponseDto meta = new MetaResponseDto(Code.OK, "사용자의 일기 목록 조회 성공");
        return ResponseEntity.ok(new ResponseDto<>(meta, diaries));
    }

    @PutMapping("/{diaryId}")
    public ResponseEntity<ResponseDto<DiaryResDto>> updateDiary(@PathVariable String diaryId,
                                                                @RequestBody DiaryUpdateDto update) {

        Optional<DiaryResDto> updatedDiary = diaryService.update(diaryId, update.getContent());

        return updatedDiary
                .map(diary -> {
                    MetaResponseDto meta = new MetaResponseDto(Code.OK, "일기 수정 성공");
                    return ResponseEntity.ok(new ResponseDto<>(meta, List.of(diary)));
                })
                .orElseGet(() -> {
                    MetaResponseDto meta = new MetaResponseDto(Code.NOT_FOUND, "수정할 일기를 찾을 수 없습니다.");
                    return ResponseEntity.status(meta.getStatus()).body(new ResponseDto<>(meta, null));
                });
    }


    @DeleteMapping("/{diaryId}")
    public ResponseEntity<ResponseDto<Void>> deleteDiary(@PathVariable String diaryId) {
        boolean deleted = diaryService.delete(diaryId);
        if (deleted) {
            MetaResponseDto meta = new MetaResponseDto(Code.OK, "일기 삭제 성공");
            return ResponseEntity.ok(new ResponseDto<>(meta, null));
        }
        MetaResponseDto meta = new MetaResponseDto(Code.NOT_FOUND, "삭제할 일기를 찾을 수 없습니다.");
        return ResponseEntity.status(meta.getStatus()).body(new ResponseDto<>(meta, null));
    }

}
