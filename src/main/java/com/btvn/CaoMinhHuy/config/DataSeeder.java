package com.btvn.CaoMinhHuy.config;

import com.btvn.CaoMinhHuy.entities.Book;
import com.btvn.CaoMinhHuy.entities.Category;
import com.btvn.CaoMinhHuy.entities.Coupon;
import com.btvn.CaoMinhHuy.entities.Role;
import com.btvn.CaoMinhHuy.entities.User;
import com.btvn.CaoMinhHuy.repositories.BookRepository;
import com.btvn.CaoMinhHuy.repositories.CategoryRepository;
import com.btvn.CaoMinhHuy.repositories.CouponRepository;
import com.btvn.CaoMinhHuy.repositories.RoleRepository;
import com.btvn.CaoMinhHuy.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;
    private final CouponRepository couponRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(RoleRepository roleRepository,
                      UserRepository userRepository,
                      CategoryRepository categoryRepository,
                      BookRepository bookRepository,
                      CouponRepository couponRepository,
                      PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.bookRepository = bookRepository;
        this.couponRepository = couponRepository;
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

        // Chỉ seed danh mục mặc định khi bảng đang trống để không khôi phục danh mục đã bị xóa
        if (categoryRepository.count() == 0) {
            categoryRepository.save(new Category(null, "CNTT"));
            categoryRepository.save(new Category(null, "Ky nang"));
            categoryRepository.save(new Category(null, "Ngoai ngu"));
        }
        // Seed 10 sách mẫu nếu chưa có sách
        if (bookRepository.count() == 0) {
            Category cntt = categoryRepository.findByName("CNTT").orElse(null);
            Category kynang = categoryRepository.findByName("Ky nang").orElse(null);
            Category ngonngu = categoryRepository.findByName("Ngoai ngu").orElse(null);

            bookRepository.save(new Book(null, "Clean Code", "Robert C. Martin", 15.5, "Hướng dẫn viết mã sạch.", "/images/sample/cleancode.png", 12, cntt, false));
            bookRepository.save(new Book(null, "Java Concurrency", "Brian Goetz", 18.0, "Lập trình đa luồng với Java.", "/images/sample/javaconcurrency.png", 8, cntt, false));
            bookRepository.save(new Book(null, "Spring in Action", "Craig Walls", 20.0, "Thực hành Spring Boot và Spring MVC.", "/images/sample/springinaction.png", 10, cntt, false));
            bookRepository.save(new Book(null, "Effective Java", "Joshua Bloch", 22.0, "Best practices Java hiện đại.", "/images/sample/effectivejava.png", 9, cntt, false));
            bookRepository.save(new Book(null, "The Pragmatic Programmer", "Andy Hunt", 17.5, "Tư duy lập trình thực dụng.", "/images/sample/pragprog.png", 11, kynang, false));
            bookRepository.save(new Book(null, "Atomic Habits", "James Clear", 13.0, "Xây thói quen tích cực.", "/images/sample/atomic.png", 20, kynang, false));
            bookRepository.save(new Book(null, "Deep Work", "Cal Newport", 12.0, "Kỹ năng làm việc sâu.", "/images/sample/deepwork.png", 14, kynang, false));
            bookRepository.save(new Book(null, "English Grammar", "Raymond Murphy", 10.0, "Ngữ pháp tiếng Anh cơ bản.", "/images/sample/grammar.png", 25, ngonngu, false));
            bookRepository.save(new Book(null, "TOEIC Practice", "ETS", 11.0, "Đề luyện TOEIC chọn lọc.", "/images/sample/toeic.png", 18, ngonngu, false));
            bookRepository.save(new Book(null, "IELTS Writing", "Simon", 12.5, "Chiến lược Writing Task 1 & 2.", "/images/sample/ielts.png", 15, ngonngu, false));
        }

        // Cleanup placeholder "Đã gỡ danh mục" nếu không còn sách trỏ tới
        categoryRepository.findByName("Đã gỡ danh mục").ifPresent(c -> {
            long relatedBooks = bookRepository.countByCategory_Id(c.getId());
            if (relatedBooks == 0) {
                categoryRepository.delete(c);
            }
        });

        if (!couponRepository.existsByCodeIgnoreCase("SAVE20")) {
            Coupon c = new Coupon();
            c.setCode("SAVE20");
            c.setAmount(20000.0);
            c.setActive(true);
            c.setStartAt(LocalDateTime.now().minusDays(1));
            c.setEndAt(LocalDateTime.now().plusMonths(1));
            c.setQuantity(100);
            couponRepository.save(c);
        }

        // Debug: log all users and their roles to verify authorities at startup
        userRepository.findAll().forEach(u ->
                log.info("User: {} | roles={}", u.getUsername(),
                        u.getRoles().stream().map(Role::getName).toList())
        );
    }
}
