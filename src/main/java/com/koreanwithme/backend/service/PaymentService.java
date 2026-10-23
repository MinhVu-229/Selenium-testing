package com.koreanwithme.backend.service;

import com.koreanwithme.backend.dto.PaymentDtos.*;
import com.koreanwithme.backend.entity.*;
import com.koreanwithme.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;

@Service
public class PaymentService {

    @Value("${payment.bank.id}")
    private String bankId;

    @Value("${payment.bank.account-no}")
    private String accountNo;

    @Value("${payment.bank.account-name}")
    private String accountName;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TopikExamRepository topikExamRepository;

    @Autowired
    private UserCourseRepository userCourseRepository;

    @Autowired
    private UserExamAccessRepository userExamAccessRepository;

    @Transactional
    public PaymentInitResponse createPaymentOrder(String rawIdentifier, CreatePaymentRequest req) {
        if (rawIdentifier == null || rawIdentifier.trim().isEmpty()) {
            throw new RuntimeException("Phiên đăng nhập không hợp lệ hoặc đã hết hạn!");
        }

        String identifier = rawIdentifier.trim();

        // Bóc tách Email nếu đầu vào là chuỗi toString() của đối tượng User
        if (identifier.startsWith("User(") && identifier.contains("email=")) {
            int emailStart = identifier.indexOf("email=") + 6;
            int emailEnd = identifier.indexOf(",", emailStart);
            if (emailEnd == -1) emailEnd = identifier.indexOf(")", emailStart);
            if (emailEnd != -1) {
                identifier = identifier.substring(emailStart, emailEnd).trim();
            }
        }

        // 1. Tìm User theo Email
        final String finalEmail = identifier;
        User user = userRepository.findByEmail(finalEmail)
                .orElseGet(() -> userRepository.findAll().stream()
                        .filter(u -> u.getEmail() != null && u.getEmail().equalsIgnoreCase(finalEmail))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin tài khoản: [" + finalEmail + "]")));

        Integer targetItemId = req.getItemId();

        // 2. Kiểm tra quyền sở hữu
        if (req.getItemType() == Order.ItemType.COURSE && userCourseRepository.existsByUserAndCourseId(user, targetItemId)) {
            throw new RuntimeException("Bạn đã đăng ký và sở hữu khóa học này rồi!");
        }
        if (req.getItemType() == Order.ItemType.EXAM && userExamAccessRepository.existsByUserAndExamId(user, targetItemId)) {
            throw new RuntimeException("Bạn đã mở khóa đề thi này rồi!");
        }

        String itemName = "Học liệu Tiếng Hàn";
        BigDecimal amount = BigDecimal.valueOf(199000); // Giá dự phòng
        Course selectedCourse = null;

        // 3. Lấy thông tin và giá thực tế do Admin cấu hình
        if (req.getItemType() == Order.ItemType.COURSE) {
            selectedCourse = courseRepository.findById(targetItemId)
                    .orElseThrow(() -> new RuntimeException("Khóa học không tồn tại!"));
            itemName = selectedCourse.getName();

            // Đọc giá Admin thiết lập trong Course (nếu có giá và lớn hơn 0)
            if (selectedCourse.getPrice() != null) {
                BigDecimal coursePrice = new BigDecimal(selectedCourse.getPrice().toString());
                if (coursePrice.compareTo(BigDecimal.ZERO) > 0) {
                    amount = coursePrice;
                }
            }
        } else if (req.getItemType() == Order.ItemType.EXAM) {
            TopikExam exam = topikExamRepository.findById(targetItemId)
                    .orElseThrow(() -> new RuntimeException("Đề thi TOPIK không tồn tại!"));
            itemName = exam.getTitle();
            amount = BigDecimal.valueOf(49000); // Giá mở khóa đề thi
        }

        // 4. Tạo mã đơn hàng KWM + 6 số ngẫu nhiên
        String orderCode = "KWM" + (100000 + new Random().nextInt(900000));

        Order order = Order.builder()
                .orderCode(orderCode)
                .user(user)
                .course(selectedCourse)
                .itemType(req.getItemType())
                .itemId(targetItemId)
                .itemName(itemName)
                .amount(amount)
                .status(Order.OrderStatus.PENDING)
                .paymentMethod("VIETQR")
                .build();
        orderRepository.save(order);

        // 5. Sinh URL ảnh VietQR tương ứng với số tiền thực tế
        String encodedAccountName = URLEncoder.encode(accountName.trim(), StandardCharsets.UTF_8);
        String qrImageUrl = String.format(
                "https://img.vietqr.io/image/%s-%s-compact2.png?amount=%d&addInfo=%s&accountName=%s",
                bankId.trim(),
                accountNo.trim(),
                amount.longValue(),
                orderCode,
                encodedAccountName
        );

        return PaymentInitResponse.builder()
                .orderCode(orderCode)
                .amount(amount)
                .itemName(itemName)
                .bankId(bankId)
                .accountNo(accountNo)
                .accountName(accountName)
                .qrImageUrl(qrImageUrl)
                .build();
    }

    public PaymentStatusResponse checkOrderStatus(String orderCode) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng: " + orderCode));

        return PaymentStatusResponse.builder()
                .orderCode(order.getOrderCode())
                .status(order.getStatus().name())
                .isUnlocked(order.getStatus() == Order.OrderStatus.SUCCESS)
                .build();
    }

    @Transactional
    public boolean processBankWebhook(SepayWebhookRequest webhook) {
        if (webhook == null) {
            return false;
        }

        System.out.println(">>> [SEPAY WEBHOOK NHẬN ĐƯỢC]: Nội dung = [" + webhook.getContent() + "], Số tiền = [" + webhook.getRealAmount() + "]");

        // Kiểm tra loại giao dịch: Chỉ xử lý khi là tiền vào (in / credit)
        if (!webhook.isMoneyIn()) {
            System.out.println(">>> [SEPAY WEBHOOK BỎ QUA]: Không phải giao dịch tiền vào!");
            return false;
        }

        String content = webhook.getContent() != null ? webhook.getContent().toUpperCase().trim() : "";
        BigDecimal receivedAmount = webhook.getRealAmount();

        // Tìm đơn hàng PENDING có mã đơn xuất hiện trong nội dung chuyển khoản
        return orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.PENDING
                        && content.contains(o.getOrderCode().toUpperCase().trim()))
                .findFirst()
                .map(order -> {
                    System.out.println(">>> [SEPAY WEBHOOK KHỚP ĐƠN]: Mã đơn = " + order.getOrderCode()
                            + ", Cần thanh toán = " + order.getAmount()
                            + ", Thực nhận = " + receivedAmount);

                    if (receivedAmount.compareTo(order.getAmount()) >= 0) {
                        fulfillOrder(order);
                        System.out.println(">>> [SEPAY WEBHOOK THÀNH CÔNG]: Đã kích hoạt đơn hàng " + order.getOrderCode());
                        return true;
                    } else {
                        System.out.println(">>> [SEPAY WEBHOOK CẢNH BÁO]: Số tiền thực nhận nhỏ hơn số tiền của đơn hàng!");
                        return false;
                    }
                }).orElseGet(() -> {
                    System.out.println(">>> [SEPAY WEBHOOK BỎ QUA]: Không tìm thấy đơn hàng PENDING khớp với nội dung: " + content);
                    return false;
                });
    }

    @Transactional
    public void confirmDemoPayment(String orderCode) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại!"));
        fulfillOrder(order);
    }

    private void fulfillOrder(Order order) {
        order.setStatus(Order.OrderStatus.SUCCESS);
        orderRepository.save(order);

        if (order.getItemType() == Order.ItemType.COURSE) {
            if (!userCourseRepository.existsByUserAndCourseId(order.getUser(), order.getItemId())) {
                userCourseRepository.save(UserCourse.builder()
                        .user(order.getUser())
                        .courseId(order.getItemId())
                        .build());
            }
        } else if (order.getItemType() == Order.ItemType.EXAM) {
            if (!userExamAccessRepository.existsByUserAndExamId(order.getUser(), order.getItemId())) {
                userExamAccessRepository.save(UserExamAccess.builder()
                        .user(order.getUser())
                        .examId(order.getItemId())
                        .build());
            }
        }
    }

    public List<Integer> getUserUnlockedCourseIds(String rawIdentifier) {
        if (rawIdentifier == null || rawIdentifier.trim().isEmpty()) {
            return List.of();
        }
        String identifier = rawIdentifier.trim();
        if (identifier.startsWith("User(") && identifier.contains("email=")) {
            int emailStart = identifier.indexOf("email=") + 6;
            int emailEnd = identifier.indexOf(",", emailStart);
            if (emailEnd == -1) emailEnd = identifier.indexOf(")", emailStart);
            if (emailEnd != -1) {
                identifier = identifier.substring(emailStart, emailEnd).trim();
            }
        }

        final String finalEmail = identifier;
        User user = userRepository.findByEmail(finalEmail).orElse(null);
        if (user == null) {
            return List.of();
        }

        return userCourseRepository.findAll().stream()
                .filter(uc -> uc.getUser() != null && uc.getUser().getId().equals(user.getId()))
                .map(UserCourse::getCourseId)
                .toList();
    }
}