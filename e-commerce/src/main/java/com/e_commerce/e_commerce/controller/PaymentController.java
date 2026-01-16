package com.e_commerce.e_commerce.controller;

import com.e_commerce.e_commerce.configuration.VnPayConfig;
import com.e_commerce.e_commerce.dto.response.ApiResponse;
import com.e_commerce.e_commerce.entity.Order;
import com.e_commerce.e_commerce.enums.CheckoutStatus;
import com.e_commerce.e_commerce.enums.ErrorCode;
import com.e_commerce.e_commerce.exception.AppException;
import com.e_commerce.e_commerce.repository.OrderRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PaymentController {
    VnPayConfig vnPayConfig;
    OrderRepository orderRepository;

    @GetMapping("/create_payment")
    ApiResponse<?> createPayment(
            HttpServletRequest req,
            @RequestParam String orderId
    ) throws UnsupportedEncodingException {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String orderType = "other";
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));
        long amount = order.getTotalPrice().multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValue();

        String vnp_TxnRef = orderId;
        String vnp_IpAddr = VnPayConfig.getIpAddress(req);

        String vnp_TmnCode = vnPayConfig.vnp_TmnCode;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");

//        String bankCode = req.getParameter("bankCode");
//        if (bankCode != null && !bankCode.isEmpty()) {
//            vnp_Params.put("vnp_BankCode", bankCode);
//        }
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", orderType);

//        String locate = req.getParameter("language");
//        if (locate != null && !locate.isEmpty()) {
//            vnp_Params.put("vnp_Locale", locate);
//        } else {
//            vnp_Params.put("vnp_Locale", "vn");
//        }
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                //Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = VnPayConfig.hmacSHA512(vnPayConfig.secretKey, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = vnPayConfig.vnp_PayUrl + "?" + queryUrl;
//        com.google.gson.JsonObject job = new JsonObject();
//        job.addProperty("code", "00");
//        job.addProperty("message", "success");
//        job.addProperty("data", paymentUrl);
//        Gson gson = new Gson();
//        resp.getWriter().write(gson.toJson(job));
        return ApiResponse.builder()
                .message("Successfully!")
                .result(paymentUrl)
                .build();
    }

    @GetMapping("/ipn")
    public ApiResponse<?> getPaymentInfo(HttpServletRequest request) throws UnsupportedEncodingException {
        String rspCode = null;
        String message = null;
        try {

        /*  IPN URL: Record payment results from VNPAY
        Implementation steps:
        Check checksum
        Find transactions (vnp_TxnRef) in the database (checkOrderId)
        Check the payment status of transactions before updating (checkOrderStatus)
        Check the amount (vnp_Amount) of transactions before updating (checkAmount)
        Update results to Database
        Return recorded results to VNPAY
        */

            Map<String, String> fields = new HashMap();
            for (Enumeration params = request.getParameterNames(); params.hasMoreElements(); ) {
                String fieldName = URLEncoder.encode((String) params.nextElement(), StandardCharsets.US_ASCII.toString());
                String fieldValue = URLEncoder.encode(request.getParameter(fieldName), StandardCharsets.US_ASCII.toString());
                log.info("Field name and field value: {}, {}", fieldName, fieldValue);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    fields.put(fieldName, fieldValue);
                }
            }

            String vnp_SecureHash = request.getParameter("vnp_SecureHash");
            if (fields.containsKey("vnp_SecureHashType")) {
                fields.remove("vnp_SecureHashType");
            }
            if (fields.containsKey("vnp_SecureHash")) {
                fields.remove("vnp_SecureHash");
            }

            // check key
            String signValue = VnPayConfig.hashAllFields(fields, vnPayConfig.secretKey);
            log.info("signValue: {}", signValue);
            log.info("vnp_SecureHash: {}", vnp_SecureHash);
            if (signValue.equals(vnp_SecureHash)) {
                boolean checkOrderId = true;
                Order order = null;
                try {
                    String vnp_TxnRef = fields.get("vnp_TxnRef");
                    order = orderRepository.findById(vnp_TxnRef).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));
                } catch (AppException e) {
                    checkOrderId = false;
                }

                boolean checkAmount = true;
                if (order == null) {
                    checkAmount = false;
                } else {
                    BigDecimal amount = BigDecimal.valueOf(Long.parseLong(fields.get("vnp_Amount")));
                    BigDecimal amountFromOrder = order.getTotalPrice().multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP);
                    if (amount.compareTo(amountFromOrder) != 0) {
                        checkAmount = false;
                    }
                }

                boolean checkOrderStatus = true;
                if (order == null || order.getCheckoutStatus() != CheckoutStatus.PENDING) {
                    checkOrderStatus = false;
                }

                if (checkOrderId) {
                    if (checkAmount) {
                        if (checkOrderStatus) {
                            if ("00".equals(request.getParameter("vnp_ResponseCode"))) {
                                order.setCheckoutStatus(CheckoutStatus.PAID);
                                orderRepository.save(order);
                                rspCode = "00";
                                message = "Confirm Success";
                            }
                        } else {
                            rspCode = "02";
                            message = "Order already confirmed";
                        }
                    } else {
                        rspCode = "04";
                        message = "Invalid Amount";
                    }
                } else {
                    rspCode = "01";
                    message = "Order not Found";
                }
            } else {
                rspCode = "97";
                message = "Invalid Checksum";
            }
        } catch (Exception e) {
            rspCode = "99";
            message = "Unknown error";
        }
        return ApiResponse.builder()
                .code(Integer.parseInt(rspCode))
                .message(message)
                .build();
    }
}
