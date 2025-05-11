package com.lifelog.diary.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lifelog.diary.domain.enums.AuthProvider;
import com.lifelog.diary.domain.enums.Gender;
import com.lifelog.diary.domain.enums.Hobby;
import com.lifelog.diary.domain.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseCreatedTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private AuthProvider provider;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "birth", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "hobby", nullable = false)
    private Hobby hobby;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "profile_url")
    private String profile_url;

    @PrePersist
    public void prePersist() {
        if (this.username == null && provider != null && providerId != null) {
            this.username = provider + "_" + providerId;
        }
    }

    @Builder
    public User(AuthProvider provider, String providerId, String username,
                String nickname, LocalDate birth, Gender gender, Role role, String profile_url) {
        this.provider = provider;
        this.providerId = providerId;
        this.username = username;
        this.nickname = nickname;
        this.birth = birth;
        this.gender = gender;
        this.role = role;
        this.profile_url = profile_url;
    }
}
