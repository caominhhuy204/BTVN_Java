package com.btvn.CaoMinhHuy.services.impl;

import com.btvn.CaoMinhHuy.entities.Book;
import com.btvn.CaoMinhHuy.entities.Order;
import com.btvn.CaoMinhHuy.entities.OrderItem;
import com.btvn.CaoMinhHuy.models.Cart;
import com.btvn.CaoMinhHuy.models.CartItem;
import com.btvn.CaoMinhHuy.repositories.CouponRepository;
import com.btvn.CaoMinhHuy.repositories.OrderRepository;
import com.btvn.CaoMinhHuy.repositories.UserRepository;
import com.btvn.CaoMinhHuy.services.BookService;
import com.btvn.CaoMinhHuy.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final BookService bookService;
    private final CouponRepository couponRepository;

    private static final Set<String> ALLOWED_STATUS = Set.of("CREATED", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED", "REFUNDED");

    @Override
    @Transactional
    public Order createFromCart(Cart cart,
                                String username,
                                String shippingName,
                                String shippingPhone,
                                String shippingAddress,
                                String paymentMethod,
                                Double shippingFee,
                                Double discount,
                                String couponCode) {
        if (cart == null || cart.isEmpty()) {
            throw new IllegalArgumentException("Gio hang trong, khong the tao don");
        }
        if (shippingName == null || shippingName.isBlank()) {
            throw new IllegalArgumentException("Ten nguoi nhan khong duoc de trong");
        }
        if (shippingPhone == null || shippingPhone.isBlank()) {
            throw new IllegalArgumentException("So dien thoai giao hang khong duoc de trong");
        }
        if (shippingAddress == null || shippingAddress.isBlank()) {
            throw new IllegalArgumentException("Dia chi giao hang khong duoc de trong");
        }
        String payment = (paymentMethod == null || paymentMethod.isBlank()) ? "COD" : paymentMethod.trim();

        double safeShippingFee = Math.max(0, shippingFee != null ? shippingFee : 0.0);
        double safeDiscount = Math.max(0, discount != null ? discount : 0.0);
        String appliedCoupon = null;
        if (couponCode != null && !couponCode.isBlank()) {
            var couponOpt = couponRepository.findByCodeIgnoreCase(couponCode.trim())
                    .filter(c -> c.isActive() && c.getAmount() != null && c.getAmount() > 0);
            if (couponOpt.isPresent()) {
                var coupon = couponOpt.get();
                var now = LocalDateTime.now();

                boolean inWindow = (coupon.getStartAt() == null || !now.isBefore(coupon.getStartAt()))
                        && (coupon.getEndAt() == null || !now.isAfter(coupon.getEndAt()));

                if (!inWindow) {
                    throw new IllegalArgumentException("Ma giam gia chua bat dau hoac da het han");
                }
                Integer left = coupon.getQuantity();
                if (left != null && left <= 0) {
                    throw new IllegalArgumentException("Ma giam gia da het luot su dung");
                }

                safeDiscount = Math.max(safeDiscount, coupon.getAmount());
                appliedCoupon = coupon.getCode();
                if (left != null) {
                    coupon.setQuantity(left - 1);
                    couponRepository.save(coupon);
                }
            } else {
                throw new IllegalArgumentException("Ma giam gia khong hop le hoac da het thoi gian giam gia");
            }
        }

        Order order = new Order();
        if (username != null) {
            userRepository.findByUsername(username).ifPresent(order::setUser);
        }
        order.setShippingName(shippingName.trim());
        order.setShippingPhone(shippingPhone.trim());
        order.setShippingAddress(shippingAddress.trim());
        order.setPaymentMethod(payment);
        order.setShippingFee(safeShippingFee);
        order.setDiscount(safeDiscount);
        order.setCouponCode(appliedCoupon);

        double total = 0;
        for (CartItem item : cart.getItems().values()) {
            OrderItem oi = new OrderItem();
            oi.setBookId(item.getBookId());
            oi.setTitle(item.getTitle());
            oi.setPrice(item.getPrice());
            oi.setQuantity(item.getQuantity());

            // Lay tac gia & tru ton kho (neu con ton tai)
            try {
                Book b = bookService.findById(item.getBookId());
                if (b.getStock() != null && b.getStock() < item.getQuantity()) {
                    throw new IllegalArgumentException("Sach '" + b.getTitle() + "' khong du ton kho");
                }
                b.setStock((b.getStock() == null ? 0 : b.getStock()) - item.getQuantity());
                bookService.save(b);
                oi.setAuthor(b.getAuthor());
            } catch (Exception ignored) {
                oi.setAuthor(null);
            }

            total += oi.getLineTotal();
            order.addItem(oi);
        }

        double finalTotal = Math.max(0, total + safeShippingFee - safeDiscount);

        order.setTotalAmount(finalTotal);
        order.setStatus("CREATED");
        order.setStatusUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order updateStatus(Long id, String status, String adminNote) {
        if (!ALLOWED_STATUS.contains(status)) {
            throw new IllegalArgumentException("Trang thai khong hop le: " + status);
        }
        Order order = findById(id);
        String previous = order.getStatus();

        if (previous != null && (previous.equals("CANCELLED") || previous.equals("REFUNDED"))) {
            if (!previous.equals(status)) {
                throw new IllegalArgumentException("Don da " + previous.toLowerCase() + ", khong the doi trang thai khac");
            }
        }
        if (status.equals("CANCELLED")) {
            if (previous != null && previous.equals("DELIVERED")) {
                throw new IllegalArgumentException("Don da giao thanh cong, khong the huy");
            }
        }
        if (status.equals("REFUNDED")) {
            if (previous != null && !(previous.equals("DELIVERED") || previous.equals("CANCELLED"))) {
                throw new IllegalArgumentException("Chi hoan tien don da giao hoac da huy");
            }
        }

        order.setStatus(status);
        if (adminNote != null) {
            String trimmed = adminNote.trim();
            order.setAdminNote(trimmed.isEmpty() ? null : trimmed);
        }
        order.setStatusUpdatedAt(LocalDateTime.now());
        if ((status.equals("CANCELLED") || status.equals("REFUNDED"))
                && (previous == null || !(previous.equals("CANCELLED") || previous.equals("REFUNDED")))) {
            restockItems(order);
        }
        return orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findForUser(String username) {
        return orderRepository.findByUser_UsernameOrderByCreatedAtDesc(username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay don hang id=" + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Order findByIdForUser(Long id, String username) {
        Order order = findById(id);
        if (order.getUser() != null) {
            if (username == null || !order.getUser().getUsername().equalsIgnoreCase(username)) {
                throw new IllegalArgumentException("Khong tim thay don hang");
            }
        }
        return order;
    }

    private void restockItems(Order order) {
        order.getItems().forEach(it -> {
            try {
                Book b = bookService.findById(it.getBookId());
                b.setStock((b.getStock() == null ? 0 : b.getStock()) + it.getQuantity());
                bookService.save(b);
            } catch (Exception ignored) {
                // book co the da bi xoa, bo qua
            }
        });
    }
}
