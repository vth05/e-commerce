package com.e_commerce.e_commerce.service;

import com.e_commerce.e_commerce.dto.request.*;
import com.e_commerce.e_commerce.dto.response.*;
import com.e_commerce.e_commerce.enums.ErrorCode;
import com.e_commerce.e_commerce.exception.AppException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class GhnService {
    WebClient ghnWebClient;

    public GhnCalculateFeeResponse calculateFee(GhnCalculateFeeRequest request) {
        try {
            GhnRawResponse<GhnCalculateFeeResponse> raw = ghnWebClient.post()
                    .uri("/v2/shipping-order/fee")
                    .bodyValue(request)
                    .retrieve()
                    // converts the GHN JSON response body into the structured Java object GhnRawResponse<GhnCalculateFeeResponse>
                    .bodyToMono(new ParameterizedTypeReference<GhnRawResponse<GhnCalculateFeeResponse>>() {
                    })
                    .block();

            checkRaw(raw);

            return raw.getData();
        } catch (WebClientResponseException e) {
            throw handleWebClientError(e);
        }
    }

    public GhnCreateOrderResponse createOrder(GhnCreateOrderRequest request) {
        try {
            GhnRawResponse<GhnCreateOrderResponse> raw = ghnWebClient.post()
                    .uri("/v2/shipping-order/create")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<GhnRawResponse<GhnCreateOrderResponse>>() {
                    })
                    .block();

            checkRaw(raw);

            return raw.getData();
        } catch (WebClientResponseException e) {
            throw handleWebClientError(e);
        }
    }

    public List<GhnCancelOrderResponse> cancelOrders(GhnCancelOrderRequest request) {
        try {
            GhnRawResponse<List<GhnCancelOrderResponse>> raw = ghnWebClient.post()
                    .uri("/v2/switch-status/cancel")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<GhnRawResponse<List<GhnCancelOrderResponse>>>() {
                    })
                    .block();

            checkRaw(raw);

            return raw.getData();
        } catch (WebClientResponseException e) {
            throw handleWebClientError(e);
        }
    }

    public GhnExpectedDeliveryTimeResponse calculateExpectedDeliveryTime(GhnExpectedDeliveryTimeRequest request) {
        try {
            GhnRawResponse<GhnExpectedDeliveryTimeResponse> raw = ghnWebClient.post()
                    .uri("/v2/shipping-order/leadtime")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<GhnRawResponse<GhnExpectedDeliveryTimeResponse>>() {
                    })
                    .block();

            checkRaw(raw);

            return raw.getData();
        } catch (WebClientResponseException e) {
            throw handleWebClientError(e);
        }
    }

    public List<GhnReturnOrderResponse> returnOrder(GhnReturnOrderRequest request) {
        try {
            GhnRawResponse<List<GhnReturnOrderResponse>> raw = ghnWebClient.post()
                    .uri("/v2/switch-status/return")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<GhnRawResponse<List<GhnReturnOrderResponse>>>() {
                    })
                    .block();

            checkRaw(raw);

            return raw.getData();
        } catch (WebClientResponseException e) {
            throw handleWebClientError(e);
        }
    }

    public GhnOrderInfoResponse getOrderInfo(GhnOrderInfoRequest request) {
        try {
            GhnRawResponse<GhnOrderInfoResponse> raw = ghnWebClient.post()
                    .uri("/v2/shipping-order/detail")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<GhnRawResponse<GhnOrderInfoResponse>>() {
                    })
                    .block();

            checkRaw(raw);

            return raw.getData();
        } catch (WebClientResponseException e) {
            throw handleWebClientError(e);
        }
    }

    private RuntimeException handleWebClientError(WebClientResponseException e) {
        String errorBody = e.getResponseBodyAsString();
        log.error("--- CHI TIẾT LỖI TỪ GHN ---");
        log.error("Http status: " + e.getStatusCode());
        log.error("Message: " + errorBody);
        log.error("---------------------------");
        return new AppException(ErrorCode.GHN_HTTP_ERROR);
    }

    private void checkRaw(GhnRawResponse<?> raw) {
        if (raw == null) {
            throw new AppException(ErrorCode.GHN_EMPTY_RESPONSE_BODY);
        }
        if (raw.getCode() != 200) {
            throw new RuntimeException(raw.getMessage());
        }
        if (raw.getData() == null) {
            throw new AppException(ErrorCode.GHN_DATA_NULL);
        }
    }
}
