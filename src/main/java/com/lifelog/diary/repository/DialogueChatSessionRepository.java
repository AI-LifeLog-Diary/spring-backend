package com.lifelog.diary.repository;

import com.lifelog.diary.domain.DialogueChatSession;
import com.lifelog.diary.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DialogueChatSessionRepository extends JpaRepository<DialogueChatSession, Long> {

    Optional<DialogueChatSession> findTopByUserOrderByCreatedAtDesc(User user);
    List<DialogueChatSession> findAllTopByUserOrderByCreatedAtDesc(User user);
}
