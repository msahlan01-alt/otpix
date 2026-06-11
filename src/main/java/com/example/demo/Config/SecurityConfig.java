package com.example.demo.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.demo.Service.UserService;

@Configuration
public class SecurityConfig {

        private final UserService userService;

        public SecurityConfig(UserService userService) {
                this.userService = userService;
        }

        @Bean
        public AuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
                DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userService);
                provider.setPasswordEncoder(passwordEncoder);
                return provider;
        }

        // Expose AuthenticationManager agar bisa dipakai di AuthController
        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http,
                        AuthenticationProvider authenticationProvider) throws Exception {
                return http
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/",
                                                                "/auth/login",
                                                                "/auth/login/otp",
                                                                "/auth/register",
                                                                "/auth/verify",
                                                                "/auth/resend-otp",
                                                                "/welcome",
                                                                "/tutorial",
                                                                "/tutorial/complete",
                                                                "/tutorial/skip",
                                                                "/css/**",
                                                                "/js/**",
                                                                "/images/**",
                                                                "/assets/**",
                                                                "/static/**",
                                                                "/favicon.ico",
                                                                "/error")
                                                .permitAll()
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/auth/login")
                                                .loginProcessingUrl("/auth/login-process") // diubah agar tidak bentrok
                                                                                           // dengan POST /auth/login
                                                                                           // kita
                                                .usernameParameter("username")
                                                .passwordParameter("password")
                                                .defaultSuccessUrl("/dashboard", true)
                                                .failureUrl("/auth/login?error=true")
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutUrl("/auth/logout")
                                                .logoutSuccessUrl("/auth/login?logout=true")
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID")
                                                .permitAll())
                                .authenticationProvider(authenticationProvider)
                                .build();
        }
}