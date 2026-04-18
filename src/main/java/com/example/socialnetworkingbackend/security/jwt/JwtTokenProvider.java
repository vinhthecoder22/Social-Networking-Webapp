package com.example.socialnetworkingbackend.security.jwt;

import com.example.socialnetworkingbackend.constant.ErrorMessage;
import com.example.socialnetworkingbackend.exception.InvalidException;
import com.example.socialnetworkingbackend.exception.UnauthorizedException;
import com.example.socialnetworkingbackend.security.UserPrincipal;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final String CLAIM_TYPE = "type";
    private final String TYPE_ACCESS = "access";
    private final String TYPE_REFRESH = "refresh";
    private final String USERNAME_KEY = "username";
    private final String AUTHORITIES_KEY = "auth";

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.access.expiration_time}")
    private Integer EXPIRATION_TIME_ACCESS_TOKEN;

    @Value("${jwt.refresh.expiration_time}")
    private Integer EXPIRATION_TIME_REFRESH_TOKEN;

    public String generateToken(UserPrincipal userPrincipal, Boolean isRefreshToken) {
        String authorities = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        Map<String, Object> claim = new HashMap<>();
        claim.put(CLAIM_TYPE, isRefreshToken ? TYPE_REFRESH : TYPE_ACCESS);
        claim.put(USERNAME_KEY, userPrincipal.getUsername());
        claim.put(AUTHORITIES_KEY, authorities);
        if (isRefreshToken) {
            return Jwts.builder()
                    .setClaims(claim)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + (EXPIRATION_TIME_REFRESH_TOKEN * 60 * 1000L)))
                    .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                    .compact();
        }
        return Jwts.builder()
                .setClaims(claim)
                .setSubject(userPrincipal.getId())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + (EXPIRATION_TIME_ACCESS_TOKEN * 60 * 1000L)))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Authentication getAuthenticationByRefreshToken(String refreshToken) {
        Claims claims = Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(refreshToken).getBody();
        if (!claims.get(CLAIM_TYPE).equals(TYPE_REFRESH) || ObjectUtils.isEmpty(claims.get(AUTHORITIES_KEY))
                || ObjectUtils.isEmpty(claims.get(USERNAME_KEY))) {
            throw new InvalidException(ErrorMessage.Auth.INVALID_REFRESH_TOKEN);
        }
        Collection<? extends GrantedAuthority> authorities = Arrays
                .stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        UserDetails principal = new UserPrincipal(claims.get(USERNAME_KEY).toString(), authorities);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    public String extractClaimUsername(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody().get(USERNAME_KEY).toString();
    }

    public String extractSubjectFromJwt(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody().getSubject();
    }

    public Date extractExpirationFromJwt(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody().getExpiration();
    }

    public Boolean isTokenExpired(String token) {
        return extractExpirationFromJwt(token).before(new Date());
    }

    public boolean validateToken(String token) {
        try {
            if (token == null) {
                throw new UnauthorizedException(ErrorMessage.Auth.INVALID_ACCESS_TOKEN);
            }
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature");
            throw new UnauthorizedException(ErrorMessage.Auth.INVALID_JWT_SIGNATURE);
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
            throw new UnauthorizedException(ErrorMessage.Auth.INVALID_ACCESS_TOKEN);
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
            throw new UnauthorizedException(ErrorMessage.Auth.EXPIRED_ACCESS_TOKEN);
        }
    }

    public long getExpirationTimeAccess() {
        return EXPIRATION_TIME_ACCESS_TOKEN;
    }

    public long getExpirationTimeRefresh() {
        return EXPIRATION_TIME_REFRESH_TOKEN;
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
