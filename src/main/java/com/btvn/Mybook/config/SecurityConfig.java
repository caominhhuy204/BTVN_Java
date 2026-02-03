package com.btvn.Mybook.config;

import com.btvn.Mybook.entities.User;
import com.btvn.Mybook.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepository;
    private final GoogleOAuth2UserService googleOAuth2UserService;
    private final GoogleOidcUserService googleOidcUserService;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            User u = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user: " + username));

            var authorities = u.getRoles().stream()
                    .map(r -> new SimpleGrantedAuthority(r.getName())) // ví dụ: ROLE_ADMIN / ROLE_USER
                    .toList();

            return new org.springframework.security.core.userdetails.User(
                    u.getUsername(), u.getPassword(), authorities
            );
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // ✅ Giữ CSRF (chuẩn). Logout sẽ cần token trong form.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/oauth2/**", "/login/oauth2/**", "/css/**", "/js/**", "/images/**", "/error/**").permitAll()

                        // Admin
                        .requestMatchers("/books/new", "/books/*/edit").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/books", "/books/*", "/books/*/delete").hasRole("ADMIN")

                        // User + Admin
                        .requestMatchers(HttpMethod.GET, "/books/**", "/cart/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/cart/**").hasAnyRole("USER", "ADMIN")

                        // API
                        .requestMatchers("/api/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .defaultSuccessUrl("/books", true)
                        .failureUrl("/auth/login?error")
                        .permitAll()
                )
                .oauth2Login(oauth -> oauth
                        .loginPage("/auth/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(googleOAuth2UserService)
                                .oidcUserService(googleOidcUserService)
                        )
                        .defaultSuccessUrl("/books", true)
                )
                .logout(logout -> logout
                        // ✅ Đồng bộ với layout: POST /logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/auth/login?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .exceptionHandling(ex -> ex.accessDeniedPage("/error/403"));

        return http.build();
    }
}
