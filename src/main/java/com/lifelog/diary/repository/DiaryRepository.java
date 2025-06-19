package com.lifelog.diary.repository;

import com.lifelog.diary.domain.Diary;
import com.lifelog.diary.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, String> {
    List<Diary> findByUserId(Long userId);
    Optional<Diary> findTopByUserAndCreatedAtBetween(User user, LocalDateTime start, LocalDateTime end);
    Diary findTopByUserOrderByCreatedAtDesc(User user);
}

