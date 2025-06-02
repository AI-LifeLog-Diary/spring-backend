package com.lifelog.diary.repository;

import com.lifelog.diary.domain.Diary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, String> {
    List<Diary> findByUserId(Long userId);
}

