package com.btvn.Mybook.config;

import com.btvn.Mybook.entities.Role;
import com.btvn.Mybook.entities.User;
import com.btvn.Mybook.repositories.RoleRepository;
import com.btvn.Mybook.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class GoogleOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private static final Logger log = LoggerFactory.getLogger(GoogleOAuth2UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauthUser = delegate.loadUser(userRequest);
        String email = oauthUser.getAttribute("email");

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_user"),
                    "Khong lay duoc email tu Google"
            );
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_USER")));

        User user = userRepository.findByUsername(email)
                .orElseGet(() -> {
                    User u = new User();
                    u.setUsername(email);
                    u.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                    u.getRoles().add(userRole);
                    return userRepository.save(u);
                });

        // Bảo đảm user cũ (nếu từng tạo mà thiếu role) vẫn có quyền USER
        if (user.getRoles().stream().noneMatch(r -> "ROLE_USER".equals(r.getName()))) {
            user.getRoles().add(userRole);
            user = userRepository.save(user);
        }

        Set<GrantedAuthority> authorities = new HashSet<>();
        user.getRoles().forEach(r -> {
            String name = r.getName();
            if (name == null || name.isBlank()) return;
            // Bảo đảm đúng format ROLE_*
            if (!name.startsWith("ROLE_")) {
                name = "ROLE_" + name;
            }
            authorities.add(new SimpleGrantedAuthority(name));
        });

        log.info("Google login for {} with authorities {}", email, authorities);

        return new DefaultOAuth2User(authorities, oauthUser.getAttributes(), "email");
    }
}
