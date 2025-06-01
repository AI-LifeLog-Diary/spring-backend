package com.lifelog.diary.security.dto;

import com.lifelog.diary.domain.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;

    // role 반환
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return user.getRole().toString();
            }
        });

        return collection;
    }

    // 비밀번호 (없음)
    @Override
    public String getPassword() {
        return "";
    }

    // username 반환
    @Override
    public String getUsername() {
        return user.getUsername();
    }

    // 계정 만료 여부 반환
    @Override
    public boolean isAccountNonExpired() {
        return true; // 임의로 만료되지 않았다고 설정
    }

    // 계정 잠금 여부 반환
    @Override
    public boolean isAccountNonLocked() {
        return true; // 임의로 잠금되지 않았다고 설정
    }

    // 비밀번호 만료 여부 반환
    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 임의로 만료되지 않았다고 설정
    }

    // 계정 활성화 여부 반환
    @Override
    public boolean isEnabled() {
        return true; // 임의로 활성화 되었다고 설정
    }
}
