package com.lifelog.diary.domain;


import com.lifelog.diary.domain.enums.Hobby;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "hobby")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserHobby {

    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private Hobby hobby;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public UserHobby(Hobby hobby, User user) {
        this.hobby = hobby;
        this.user = user;
    }

    public static UserHobby createHobby(User user, Hobby hobby) {
        return UserHobby.builder()
                .user(user)
                .hobby(hobby)
                .build();
    }

}
