package com.lifelog.diary.service;

import com.lifelog.diary.common.response.enums.Code;
import com.lifelog.diary.common.response.exception.GeneralException;
import com.lifelog.diary.domain.User;
import com.lifelog.diary.domain.enums.Role;
import com.lifelog.diary.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Transactional
public class AccountService {

    private final UserRepository userRepository;

    // 현재 인증된 사용자 Member 객체 반환
    public User getCurrentUser() {
        String username;
        try{
            username = SecurityContextHolder.getContext().getAuthentication().getName();
        }catch (Exception e){
            throw new GeneralException(Code.USER_NOT_FOUND, "현재 인증된 사용자가 없습니다.");
        }
        return userRepository.findByUsername(username);
    }

    // 현재 인증된 사용자 ID를 반환
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    // 현재 인증된 사용자 Role을 반환
    public Role getCurrentUserRole() {
        return getCurrentUser().getRole();
    }

}
