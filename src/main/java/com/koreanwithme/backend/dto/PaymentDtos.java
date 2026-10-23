package com.koreanwithme.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.koreanwithme.backend.entity.Order;
import lombok.*;

import java.math.BigDecimal;

public class PaymentDtos {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatePaymentRequest {
        private Order.ItemType itemType; // COURSE hoặc EXAM
        private Integer itemId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentInitResponse {
        private String orderCode;
        private BigDecimal amount;
        private String itemName;
        private String bankId;
        private String accountNo;
        private String accountName;
        private String qrImageUrl;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentStatusResponse {
        private String orderCode;
        private String status;

        @JsonProperty("isUnlocked")
        private boolean isUnlocked;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SepayWebhookRequest {
        private Long id;
        private String gateway;
        private String accountNumber;
        private String content;

        @JsonProperty("transferType")
        private String transferType;

        @JsonProperty("transfer_type")
        private String transferTypeSnake;

        private BigDecimal transferAmount;
        private BigDecimal amount; // Dự phòng trường hợp SePay gửi field amount thay vì transferAmount
        private String referenceCode;

        /**
         * Lấy số tiền thực nhận từ Webhook (ưu tiên transferAmount, fallback sang amount)
         */
        public BigDecimal getRealAmount() {
            if (transferAmount != null) {
                return transferAmount;
            }
            if (amount != null) {
                return amount;
            }
            return BigDecimal.ZERO;
        }

        /**
         * Kiểm tra giao dịch có phải là tiền vào tài khoản (in / credit)
         */
        public boolean isMoneyIn() {
            String type = transferType != null ? transferType : transferTypeSnake;
            return type == null || "in".equalsIgnoreCase(type) || "credit".equalsIgnoreCase(type);
        }
    }
}