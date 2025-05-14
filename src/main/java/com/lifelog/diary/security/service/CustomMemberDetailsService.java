package com.lifelog.diary.security.service;

import com.lifelog.diary.domain.User;
import com.lifelog.diary.repository.UserRepository;
import com.lifelog.diary.security.dto.CustomUserDetails;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Transactional
public class CustomMemberDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다 : " + username);
        }

        return new CustomUserDetails(user);
    }
}