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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private AuthProvider provider;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "birth")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "profile_url")
    private String profileUrl;

    @Column(name = "profile_completed")
    private boolean profileCompleted;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserHobby> hobbyList = new ArrayList<>();

    @Builder
    public User(AuthProvider provider, String providerId, String email, String username, String nickname,
                LocalDate birth, Gender gender, Role role, String profileUrl, boolean profileCompleted, List<UserHobby> hobbyList) {
        this.provider = provider;
        this.providerId = providerId;
        this.email = email;
        this.username = username;
        this.nickname = nickname;
        this.birth = birth;
        this.gender = gender;
        this.role = role;
        this.profileUrl = profileUrl;
        this.profileCompleted = profileCompleted;
        this.hobbyList = hobbyList;
    }

    public static User createUser(AuthProvider provider, String providerId,
                                  String email, String username, Role role, boolean profileCompleted) {
        return User.builder()
                .provider(provider)
                .providerId(providerId)
                .email(email)
                .username(username)
                .role(role)
                .profileCompleted(profileCompleted)
                .build();
    }

    public void updateSocialInfo(String email) {
        this.email = email;
    }

    public void updateProfile(String nickname, LocalDate birth, Gender gender, String profileUrl,
                              List<Hobby> newHobbies, boolean profileCompleted) {
        if (nickname != null) this.nickname = nickname;
        if (birth != null) this.birth = birth;
        if (gender != null) this.gender = gender;
        if (profileUrl != null) this.profileUrl = profileUrl;

        if (newHobbies != null) {
            List<UserHobby> toRemove = this.hobbyList.stream()
                    .filter(userHobby -> !newHobbies.contains(userHobby.getHobby()))
                    .toList();
            this.hobbyList.removeAll(toRemove);

            for (Hobby hobby : newHobbies) {
                boolean alreadyExists = this.hobbyList.stream()
                        .anyMatch(userHobby -> userHobby.getHobby().equals(hobby));

                if (!alreadyExists) {
                    this.hobbyList.add(UserHobby.createHobby(this, hobby));
                }
            }
        }

        this.profileCompleted = profileCompleted;
    }

}
