package com.btvn.CaoMinhHuy.services;

import com.btvn.CaoMinhHuy.entities.Book;
import com.btvn.CaoMinhHuy.entities.Order;
import com.btvn.CaoMinhHuy.models.Cart;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private BookService bookService;

    private Book createBook(double price) {
        Book b = new Book();
        b.setTitle("Sample");
        b.setAuthor("Author");
        b.setPrice(price);
        b.setStock(5);
        return bookService.save(b);
    }

    private Cart cartWith(Book b) {
        Cart cart = new Cart();
        cart.add(b.getId(), b.getTitle(), b.getPrice());
        return cart;
    }

    @Test
    @DisplayName("Tạo đơn có áp dụng phí ship và giảm giá")
    void createFromCartShouldApplyShippingAndDiscount() {
        Book b = createBook(10000.0);
        Cart cart = cartWith(b);

        Order order = orderService.createFromCart(cart, null, "Tester", "0909", "123 Street", "COD", 15000.0, 5000.0, null);

        assertThat(order.getShippingName()).isEqualTo("Tester");
        assertThat(order.getPaymentMethod()).isEqualTo("COD");
        assertThat(order.getShippingFee()).isEqualTo(15000.0);
        assertThat(order.getDiscount()).isEqualTo(5000.0);
        assertThat(order.getTotalAmount()).isEqualTo(10000.0 + 15000.0 - 5000.0);
    }

    @Test
    @DisplayName("Yêu cầu đủ thông tin giao hàng")
    void createFromCartShouldRequireShippingInfo() {
        Book b = createBook(5000.0);
        Cart cart = cartWith(b);

        assertThatThrownBy(() -> orderService.createFromCart(cart, null, "", "0909", "addr", "COD", 0.0, 0.0, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> orderService.createFromCart(cart, null, "Tester", "", "addr", "COD", 0.0, 0.0, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> orderService.createFromCart(cart, null, "Tester", "0909", "", "COD", 0.0, 0.0, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Admin cập nhật trạng thái và ghi chú")
    void updateStatusShouldPersistNoteAndTimestamp() {
        Book b = createBook(7000.0);
        Order order = orderService.createFromCart(cartWith(b), null, "Tester", "0909", "123 Street", "COD", 0.0, 0.0, null);
        assertThat(order.getStatus()).isEqualTo("CREATED");

        Order updated = orderService.updateStatus(order.getId(), "CONFIRMED", "Đã gọi xác nhận");

        assertThat(updated.getStatus()).isEqualTo("CONFIRMED");
        assertThat(updated.getAdminNote()).contains("xác nhận");
        assertThat(updated.getStatusUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Áp dụng mã giảm giá hoạt động")
    void applyCouponShouldUseAmount() {
        Book b = createBook(20000.0);
        Cart cart = cartWith(b);

        // Seed có SAVE20 = 20000
        Order order = orderService.createFromCart(cart, null, "Tester", "0909", "123 Street", "COD", 20000.0, 0.0, "SAVE20");

        assertThat(order.getCouponCode()).isEqualTo("SAVE20");
        assertThat(order.getDiscount()).isEqualTo(20000.0);
        // total = 20000 item + 20000 ship - 20000 coupon = 20000
        assertThat(order.getTotalAmount()).isEqualTo(20000.0);
    }
}
