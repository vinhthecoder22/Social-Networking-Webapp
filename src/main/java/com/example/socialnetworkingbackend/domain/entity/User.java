package com.example.socialnetworkingbackend.domain.entity;

import com.example.socialnetworkingbackend.constant.AuthProvider;
import com.example.socialnetworkingbackend.constant.GenderConstant;
import com.example.socialnetworkingbackend.domain.entity.common.FlagUserDateAuditing;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "user_account")
@SQLDelete(sql = "UPDATE user_account SET delete_flag = true WHERE id=?")
@SQLRestriction("delete_flag = false")
public class User extends FlagUserDateAuditing {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private String id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Nationalized
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Nationalized
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    private GenderConstant gender;

    @Column(nullable = false)
    private LocalDate dob;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider")
    private AuthProvider provider;

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "image_url")
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", foreignKey = @ForeignKey(name = "FK_USER_ROLE"))
    private Role role;

    @OneToMany(mappedBy = "following", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Follow> followings = new ArrayList<>();

    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Follow> followers = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "setting_id", referencedColumnName = "id")
    private UserSetting userSetting;
}
