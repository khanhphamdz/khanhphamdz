package com.datn.teeshirt.Config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.core.userdetails.UserDetailsService;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // @Bean
    // public PasswordEncoder passwordEncoder() {
    // return new BCryptPasswordEncoder();
    // }

    @SuppressWarnings("deprecation")
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance(); // Sử dụng NoOpPasswordEncoder
    }

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
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomAuthenticationSuccessHandler successHandler)
            throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/static/**",
                                "/admin_css/**",
                                "/customer_css/**",
                                "/customer_js/**",
                                "/images/**")
                        .permitAll()
                        // Public endpoints
                        .requestMatchers("/", "/product/**", "/login", "/admin/login", "/register").permitAll()

                        // Customer endpoints
                        .requestMatchers("/account/**", "/cart/**", "/wishlist/**", "/checkout/**").hasRole("customer")

                        // Admin và Staff endpoints
                        // .requestMatchers("/admin/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers( "/admin/product-management").hasRole("ADMIN")
                        .requestMatchers("/api/**").permitAll()

                        // Default deny
                        .anyRequest().permitAll())
                .formLogin(form -> {
                    form
                            .loginProcessingUrl("/perform-login")
                            .loginPage("/customer/account/login-register")
                            .successHandler(successHandler)
                            .failureUrl("/customer/account/login-register?error")
                            .permitAll();
                })
                // Configure admin login separately
                .formLogin(form -> {
                    form
                            .loginProcessingUrl("/perform-admin-login")
                            .loginPage("/admin/login")
                            .successHandler(successHandler)
                            .failureUrl("/admin/login?error")
                            .permitAll();
                })
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessHandler(customerLogoutSuccessHandler())
                        .permitAll())
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/admin/logout"))
                        .logoutSuccessHandler(adminLogoutSuccessHandler())
                        .permitAll())
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/error/403"));

        return http.build();
    }
}