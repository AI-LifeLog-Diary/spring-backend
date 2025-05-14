package com.lifelog.diary.repository;

import com.lifelog.diary.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.OptionalInt;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);
    boolean existsByNickname(String nickname);
}
