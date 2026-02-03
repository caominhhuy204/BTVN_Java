package com.btvn.Mybook.config;

import com.btvn.Mybook.entities.Category;
import com.btvn.Mybook.entities.Role;
import com.btvn.Mybook.entities.User;
import com.btvn.Mybook.repositories.CategoryRepository;
import com.btvn.Mybook.repositories.RoleRepository;
import com.btvn.Mybook.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(RoleRepository roleRepository,
                      UserRepository userRepository,
                      CategoryRepository categoryRepository,
                      PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

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
        categoryRepository.findByName("Ky nang").orElseGet(() -> categoryRepository.save(new Category(null, "Ky nang")));
        categoryRepository.findByName("Ngoai ngu").orElseGet(() -> categoryRepository.save(new Category(null, "Ngoai ngu")));

        // Debug: log all users and their roles to verify authorities at startup
        userRepository.findAll().forEach(u ->
                log.info("User: {} | roles={}", u.getUsername(),
                        u.getRoles().stream().map(Role::getName).toList())
        );
    }
}
