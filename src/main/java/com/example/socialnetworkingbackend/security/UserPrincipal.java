package com.example.socialnetworkingbackend.security;

import com.example.socialnetworkingbackend.domain.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.*;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class UserPrincipal implements UserDetails, OAuth2User, OidcUser {

    private final String id;

    private final String firstName;

    private final String lastName;

    private final String roleId;

    private final String roleName;

    @JsonIgnore
    private final String username;

    @JsonIgnore
    private String password;

    private final Collection<? extends GrantedAuthority> authorities;

    private Map<String, Object> attributes;
    private OidcIdToken idToken;
    private OidcUserInfo userInfo;

    public UserPrincipal(String id, String firstName, String lastName, String username, String password,
                         Collection<? extends GrantedAuthority> authorities) {
        this(id, firstName, lastName, username, password, authorities, "", "USER");
    }

    public UserPrincipal(String username, Collection<? extends GrantedAuthority> authorities) {
        this(null, "", "", username, "", authorities, "", "USER");
    }

    public UserPrincipal(String id, String firstName, String lastName, String username, String password,
                         Collection<? extends GrantedAuthority> authorities,
                         String roleId, String roleName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.authorities = authorities == null ? null : new ArrayList<>(authorities);
        this.roleId = roleId;
        this.roleName = roleName;
    }

    // Đăng nhập bằng tài khoản/mật khẩu
    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        String roleName = user.getRole() != null ? user.getRole().getName() : "USER";
        String roleId = user.getRole() != null ? String.valueOf(user.getRole().getId()) : "";

        authorities.add(new SimpleGrantedAuthority(roleName));

        return new UserPrincipal(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getUsername(),
                user.getPassword(),
                authorities,
                roleId,
                roleName);
    }

    // Dành cho Đăng nhập bằng Google / Facebook
    public static UserPrincipal create(User user, Map<String, Object> attributes, OidcIdToken idToken, OidcUserInfo userInfo) {
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        userPrincipal.attributes = attributes;
        userPrincipal.idToken = idToken;
        userPrincipal.userInfo = userInfo;
        return userPrincipal;
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getRoleId() {
        return roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    // UserDetails (Login thường)
    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities == null ? null : new ArrayList<>(authorities);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // OAuth2User (Facebook)
    @Override public Map<String, Object> getAttributes() { return attributes; }
    @Override public String getName() { return username; }

    // OidcUser (Google)
    @Override public Map<String, Object> getClaims() { return this.idToken != null ? this.idToken.getClaims() : null; }
    @Override public OidcUserInfo getUserInfo() { return this.userInfo; }
    @Override public OidcIdToken getIdToken() { return this.idToken; }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null || getClass() != object.getClass())
            return false;
        UserPrincipal that = (UserPrincipal) object;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

