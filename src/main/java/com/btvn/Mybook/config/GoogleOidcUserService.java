package com.btvn.Mybook.config;

import com.btvn.Mybook.entities.Role;
import com.btvn.Mybook.entities.User;
import com.btvn.Mybook.repositories.RoleRepository;
import com.btvn.Mybook.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class GoogleOidcUserService implements org.springframework.security.oauth2.client.userinfo.OAuth2UserService<OidcUserRequest, OidcUser> {
    private static final Logger log = LoggerFactory.getLogger(GoogleOidcUserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final OidcUserService delegate = new OidcUserService();

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = delegate.loadUser(userRequest);
        String email = oidcUser.getEmail();

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_user"),
                    "Khong lay duoc email tu Google OIDC"
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

        if (user.getRoles().stream().noneMatch(r -> "ROLE_USER".equals(r.getName()))) {
            user.getRoles().add(userRole);
            user = userRepository.save(user);
        }

        Set<GrantedAuthority> authorities = new HashSet<>();
        user.getRoles().forEach(r -> {
            String name = r.getName();
            if (name == null || name.isBlank()) return;
            if (!name.startsWith("ROLE_")) name = "ROLE_" + name;
            authorities.add(new SimpleGrantedAuthority(name));
        });

        log.info("Google OIDC login for {} with authorities {}", email, authorities);

        return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo(), "email");
    }
}
