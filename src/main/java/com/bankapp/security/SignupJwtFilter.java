package com.bankapp.security;

import com.bankapp.service.SignupService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.Filter;

@Component
public class SignupJwtFilter implements Filter {
    private final JwtUtil jwtUtil;

    @Autowired
    public SignupJwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }
    
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getRequestURI();

        if (!path.startsWith("/api/signup/create-credentials") &&
                !path.startsWith("/api/reset-password/create-credentials")) {
            chain.doFilter(req, res);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing token");
            return;
        }

        try {
            String token = authHeader.substring(7);
            Claims claims = jwtUtil.extractAllClaims(token);

            String scope = String.valueOf(claims.get("scope"));

            if (!"SIGNUP".equals(scope) && !"RESET_PASSWORD".equals(scope)) {
                SecurityContextHolder.clearContext();
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid token scope");
                return;
            }

            Integer customerId =
                    Integer.valueOf(claims.get("customerId").toString());

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(customerId, null, List.of());

            SecurityContextHolder.getContext().setAuthentication(auth);
            chain.doFilter(req, res);

        } catch (JwtException | IllegalArgumentException e) {
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired or invalid");
        }
    }
}


