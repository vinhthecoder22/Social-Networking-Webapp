package com.example.socialnetworkingbackend.security.jwt;

import com.example.socialnetworkingbackend.base.RestData;
import com.example.socialnetworkingbackend.constant.ErrorMessage;
import com.example.socialnetworkingbackend.exception.NotFoundException;
import com.example.socialnetworkingbackend.exception.UnauthorizedException;
import com.example.socialnetworkingbackend.service.CustomUserDetailsService;
import com.example.socialnetworkingbackend.service.RedisService;
import com.example.socialnetworkingbackend.util.BeanUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final CustomUserDetailsService customUserDetailsService;

    private final JwtTokenProvider tokenProvider;

    private final RedisService redisService;

    @SneakyThrows
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {

        final String requestURI = request.getRequestURI();
        log.debug("Processing authentication for URI: {}", requestURI);

        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                if (redisService.hasKey("blacklist:" + jwt)) {
                    throw new UnauthorizedException(ErrorMessage.Auth.INVALID_ACCESS_TOKEN);
                }

                if (tokenProvider.validateToken(jwt)) {
                    String userId = tokenProvider.extractSubjectFromJwt(jwt);
                    UserDetails userDetails = customUserDetailsService.loadUserById(userId);
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        } catch (UnauthorizedException e) {
            MessageSource messageSource = BeanUtil.getBean(MessageSource.class);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            String message = messageSource.getMessage(e.getMessage(), null, LocaleContextHolder.getLocale());
            response.getOutputStream().write(new ObjectMapper().writeValueAsBytes(RestData.error(e.getMessage(), message)));
            return;
        } catch (NotFoundException ex) {
            MessageSource messageSource = BeanUtil.getBean(MessageSource.class);
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            String message = messageSource.getMessage(ex.getMessage(), null, LocaleContextHolder.getLocale());
            response.getOutputStream().write(new ObjectMapper().writeValueAsBytes(RestData.error(ex.getMessage(), message)));
            return;
        } catch (Exception ex) {
            log.error("Failed to process authentication request", ex);
            MessageSource messageSource = BeanUtil.getBean(MessageSource.class);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            String message = messageSource.getMessage(ErrorMessage.ERR_EXCEPTION_GENERAL, null, LocaleContextHolder.getLocale());
            response.getOutputStream().write(new ObjectMapper().writeValueAsBytes(RestData.error(ErrorMessage.ERR_EXCEPTION_GENERAL, message)));
            return;
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        if (path.equals("/api/v1/auth/me") || path.equals("/api/v1/auth/logout")) {
            return false;
        }
        return path.startsWith("/api/v1/auth/")
                || path.startsWith("/api/v1/forgot-password/")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui.html")
                || path.startsWith("/login/oauth2/")
                || path.startsWith("/oauth2/")
                || path.startsWith("/api/v1/oauth2/info/");
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7, bearerToken.length());
        }
        return null;
    }
}
