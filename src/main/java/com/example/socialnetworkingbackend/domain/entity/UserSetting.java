package com.example.socialnetworkingbackend.domain.entity;

import com.example.socialnetworkingbackend.constant.LanguageSetting;
import com.example.socialnetworkingbackend.constant.ThemeSetting;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "user_setting")
public class UserSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ThemeSetting theme;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LanguageSetting language;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;
}

