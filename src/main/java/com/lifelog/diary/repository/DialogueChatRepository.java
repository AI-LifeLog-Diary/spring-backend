package com.lifelog.diary.repository;

import com.lifelog.diary.domain.DialogueChat;
import com.lifelog.diary.domain.DialogueChatSession;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DialogueChatRepository extends JpaRepository<DialogueChat, Long> {

    DialogueChat findTopByDialogueChatSessionOrderByCreatedAtDesc(DialogueChatSession dialogueChatSession);
    List<DialogueChat> findByUserIdAndDialogueChatSessionIdOrderByIdDesc(Long userId, Long sessionId, Pageable pageable);
    List<DialogueChat> findByUserIdAndDialogueChatSessionIdAndIdLessThanOrderByIdDesc(Long userId, Long sessionId, Long cursor, Pageable pageable);
    boolean existsByDialogueChatSessionIdAndIdLessThan(Long sessionId, Long cursor);

}
