package com.btvn.Mybook.config;

import com.btvn.Mybook.entities.*;
import com.btvn.Mybook.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_ADMIN")));
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_USER")));

        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRoles(Set.of(adminRole, userRole));
            userRepository.save(admin);
        }

        categoryRepository.findByName("CNTT").orElseGet(() -> categoryRepository.save(new Category(null, "CNTT")));
        categoryRepository.findByName("Kỹ năng").orElseGet(() -> categoryRepository.save(new Category(null, "Kỹ năng")));
        categoryRepository.findByName("Ngoại ngữ").orElseGet(() -> categoryRepository.save(new Category(null, "Ngoại ngữ")));
    }
}