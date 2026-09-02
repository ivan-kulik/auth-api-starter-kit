package com.starter.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtManager jwtManager;

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ROLES_CLAIM = "roles";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String token = resolveToken(request);

        if (StringUtils.hasText(token)) {
            try {
                Claims claims = this.jwtManager.parseAccessToken(token);
                String email = this.jwtManager.extractEmail(claims);
                Collection<? extends GrantedAuthority> authorities = extractAuthorities(claims);

                var userDetails = new org.springframework.security.core.userdetails.User(
                        email,
                        "",
                        authorities
                );

                var auth = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (JwtException | IllegalArgumentException e) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private List<GrantedAuthority> extractAuthorities(Claims claims) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        Object rolesClaim = claims.get(ROLES_CLAIM);

        if (rolesClaim instanceof List<?> rolesList) {
            for (Object role : rolesList) {
                if (role instanceof String roleStr && StringUtils.hasText(roleStr)) {
                    authorities.add(new SimpleGrantedAuthority(roleStr));
                }
            }
        } else if (rolesClaim instanceof String roleStr && StringUtils.hasText(roleStr)) {
            authorities.add(new SimpleGrantedAuthority(roleStr));
        }

        return authorities;
    }

    private String resolveToken(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (!StringUtils.hasText(authHeader)) {
            return null;
        }

        if (!authHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();

        return StringUtils.hasText(token) ? token : null;
    }
}
