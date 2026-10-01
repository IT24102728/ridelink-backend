package com.ridelink.ride_service.client;

import com.ridelink.ride_service.dto.FareEstimateRequestDTO;
import com.ridelink.ride_service.dto.FareEstimateResponseDTO;
import com.ridelink.ride_service.dto.PaymentRequestDTO;
import com.ridelink.ride_service.exception.ServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class FareServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.fare-service.url}")
    private String fareServiceUrl;

    /**
     * Interservice interaction #2:
     * Ride Service -> Fare & Payment Service: estimate fare.
     */
    public FareEstimateResponseDTO estimateFare(FareEstimateRequestDTO req) {
        try {
            return restTemplate.postForObject(
                    fareServiceUrl + "/api/fares/estimate",
                    req, FareEstimateResponseDTO.class);
        } catch (RestClientException ex) {
            throw new ServiceUnavailableException("Fare Service unavailable: " + ex.getMessage());
        }
    }

    /**
     * Ride Service -> Fare & Payment Service: record simulated payment.
     */
    public Long pay(PaymentRequestDTO req) {
        try {
            var resp = restTemplate.postForObject(
                    fareServiceUrl + "/api/payments", req, java.util.Map.class);
            return resp == null ? null : Long.valueOf(resp.get("paymentId").toString());
        } catch (RestClientException ex) {
            throw new ServiceUnavailableException("Payment Service unavailable: " + ex.getMessage());
        }
    }
}