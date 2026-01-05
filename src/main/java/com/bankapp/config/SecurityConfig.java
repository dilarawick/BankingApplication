package com.bankapp.config;

import com.bankapp.security.JwtFilter;
import com.bankapp.security.SignupJwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        private final JwtFilter jwtFilter;
        private final SignupJwtFilter signupJwtFilter;

        @Autowired
        public SecurityConfig(
                        JwtFilter jwtFilter,
                        SignupJwtFilter signupJwtFilter) {
                this.jwtFilter = jwtFilter;
                this.signupJwtFilter = signupJwtFilter;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                .authorizeHttpRequests(auth -> auth

                                                .requestMatchers(
                                                                "/",
                                                                "/index.html",
                                                                "/*.html",
                                                                "/css/**",
                                                                "/js/**",
                                                                "/img/**",
                                                                "/favicon.ico")
                                                .permitAll()

                                                .requestMatchers(
                                                                "/api/auth/login",
                                                                "/api/auth/send-otp",
                                                                "/api/auth/setup-password")
                                                .permitAll()

                                                .requestMatchers(
                                                                "/api/signup/verify",
                                                                "/api/reset-password/verify")
                                                .permitAll()

                                                .requestMatchers(
                                                                "/api/signup/create-credentials",
                                                                "/api/reset-password/create-credentials")
                                                .authenticated()

                                                .requestMatchers(
                                                                "/api/accounts/**")
                                                .authenticated()

                                                .requestMatchers(
                                                                "/api/transfers/**")
                                                .authenticated()

                                                .anyRequest().authenticated())

                                .addFilterBefore(signupJwtFilter, UsernamePasswordAuthenticationFilter.class)
                                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                                .httpBasic(basic -> basic.disable())
                                .formLogin(login -> login.disable());

                return http.build();
        }
}