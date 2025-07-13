package com.datn.teeshirt.Config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
// import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.core.annotation.Order;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.datn.teeshirt.Security.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // @SuppressWarnings("deprecation")
    // @Bean
    // public PasswordEncoder noPasswordEncoder() {
    // return NoOpPasswordEncoder.getInstance();
    // }

    @Bean
    public LogoutSuccessHandler customerLogoutSuccessHandler() {
        return new LogoutSuccessHandler() {
            @Override
            public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                    org.springframework.security.core.Authentication authentication) throws IOException {
                response.sendRedirect("/login");
            }
        };
    }

    @Bean
    public LogoutSuccessHandler adminLogoutSuccessHandler() {
        return new LogoutSuccessHandler() {
            @Override
            public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                    org.springframework.security.core.Authentication authentication) throws IOException {
                response.sendRedirect("/admin/login");
            }
        };
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // Filter chain cho admin
    @SuppressWarnings("removal")
    @Bean
    @Order(1)
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/admin/**")
                .authorizeHttpRequests(authz -> authz
                        .anyRequest().hasRole("ADMIN"))
                .formLogin(form -> form
                        .loginPage("/admin/login")
                        .loginProcessingUrl("/admin/perform-admin-login")
                        .defaultSuccessUrl("/admin", true)
                        .failureUrl("/admin/login?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/admin/login")
                        .permitAll())
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers
                        .frameOptions().sameOrigin()
                        .contentTypeOptions().disable());

        return http.build();
    }

    // Filter chain cho client
    @Bean
    @Order(2)
    public SecurityFilterChain clientFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/**")
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/static/**", "/admin_css/**", "/admin_js/**", "/customer_css/**",
                                "/customer_js/**", "/images/**")
                        .permitAll()
                        .requestMatchers("/", "/home", "/product/**", "/about", "/contact", "/account/login",
                                "/account/forgot-password")
                        .permitAll()
                        .requestMatchers("/account/**", "/cart/**", "/shopping-cart/**", "/account/wishlist").hasRole("CUSTOMER")
                        .anyRequest().permitAll())
                .formLogin(form -> form
                        .loginPage("/account/login")
                        .loginProcessingUrl("/perform-login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/account/login?error=true")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll())
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}