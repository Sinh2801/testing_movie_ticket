package com.movieticketbooking.config;

import jakarta.servlet.http.HttpServletRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * Lớp cấu hình và tiện ích cho cổng thanh toán VNPay
 */
public class VnPayConfig {
    // URL thanh toán môi trường thử nghiệm (Sandbox) của VNPay
    public static String vnp_PayUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    
    // URL mà VNPay sẽ gọi lại sau khi người dùng thực hiện thanh toán xong
    public static String vnp_ReturnUrl = "http://localhost:8081/api/payments/vnpay/callback";
    
    // Mã định danh Website của bạn trên cổng VNPay Sandbox
    public static String vnp_TmnCode = "CGXZ8008"; 
    
    // Chuỗi mã bảo mật bí mật dùng để mã hóa và kiểm tra chữ ký (Checksum/HMAC SHA512)
    public static String secretKey = "SECRETKEYGOESHERE"; 

    /**
     * Hàm băm dữ liệu theo chuẩn HMAC SHA512 để tạo chữ ký bảo mật cho VNPay
     * Tránh bị can thiệp hay chỉnh sửa dữ liệu trên URL giao dịch
     */
    public static String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                return null;
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes(StandardCharsets.UTF_8);
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }
    }

    /**
     * Hàm lấy địa chỉ IP của thiết bị khách hàng đang gửi yêu cầu đặt vé
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ipAdress;
        try {
            ipAdress = request.getHeader("X-FORWARDED-FOR");
            if (ipAdress == null) {
                ipAdress = request.getRemoteAddr();
            }
        } catch (Exception e) {
            ipAdress = "127.0.0.1";
        }
        return ipAdress;
    }

    /**
     * Hàm tạo chuỗi ngẫu nhiên dùng tạo mã giao dịch ngẫu nhiên nếu cần
     */
    public static String getRandomNumber(int len) {
        Random rnd = new Random();
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
