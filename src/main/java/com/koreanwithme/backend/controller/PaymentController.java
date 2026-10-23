package com.koreanwithme.backend.controller;

import com.koreanwithme.backend.dto.PaymentDtos.*;
import com.koreanwithme.backend.entity.User;
import com.koreanwithme.backend.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Value("${payment.webhook.secure-token}")
    private String secureToken;

    @PostMapping("/create")
    public ResponseEntity<PaymentInitResponse> createPayment(
            Authentication authentication,
            @RequestBody CreatePaymentRequest request) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userEmail = null;
        Object principal = authentication.getPrincipal();

        // 1. Trích xuất chính xác Email người dùng từ Principal
        if (principal instanceof User user) {
            userEmail = user.getEmail();
        } else if (principal instanceof UserDetails userDetails) {
            userEmail = userDetails.getUsername();
        } else if (principal instanceof String str) {
            userEmail = str;
        } else {
            userEmail = authentication.getName();
        }

        return ResponseEntity.ok(paymentService.createPaymentOrder(userEmail, request));
    }

    @GetMapping("/check-status/{orderCode}")
    public ResponseEntity<PaymentStatusResponse> checkStatus(@PathVariable String orderCode) {
        return ResponseEntity.ok(paymentService.checkOrderStatus(orderCode));
    }

    @PostMapping("/webhook/sepay")
    public ResponseEntity<Map<String, Object>> handleSepayWebhook(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody SepayWebhookRequest request) {

        Map<String, Object> response = new HashMap<>();

        // 1. Xác thực bảo mật Token từ SePay
        if (secureToken != null && !secureToken.trim().isEmpty()
                && authHeader != null && !authHeader.contains(secureToken.trim())) {
            response.put("success", false);
            response.put("message", "Mã xác thực không hợp lệ!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // 2. Xử lý cộng quyền học và cập nhật trạng thái đơn hàng
        boolean success = paymentService.processBankWebhook(request);

        // 3. BẮT BUỘC: Trả về JSON {"success": true} để SePay xác nhận giao dịch thành công
        response.put("success", true);
        response.put("message", success ? "Kích hoạt đơn hàng thành công" : "Bỏ qua (giao dịch không khớp)");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/demo-confirm/{orderCode}")
    public ResponseEntity<String> demoConfirm(@PathVariable String orderCode) {
        paymentService.confirmDemoPayment(orderCode);
        return ResponseEntity.ok("Mở khóa thành công ở chế độ Thử nghiệm!");
    }
}