package com.ridelink.ride_service.client;

import com.ridelink.ride_service.dto.DriverDTO;
import com.ridelink.ride_service.exception.ServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DriverServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.driver-service.url}")
    private String driverServiceUrl;

    /**
     * Interservice interaction #1:
     * Ride Service -> Driver Service: "Give me available drivers near pickup."
     */
    public List<DriverDTO> getEligibleDrivers(String serviceArea) {
        String url = UriComponentsBuilder
                .fromHttpUrl(driverServiceUrl + "/api/drivers/eligible")
                .queryParamIfPresent("serviceArea",
                        java.util.Optional.ofNullable(serviceArea))
                .toUriString();
        try {
            ResponseEntity<List<DriverDTO>> resp = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<>() {});
            return resp.getBody() == null ? List.of() : resp.getBody();
        } catch (RestClientException ex) {
            throw new ServiceUnavailableException("Driver Service unavailable: " + ex.getMessage());
        }
    }

    public DriverDTO getDriverById(Long driverId) {
        try {
            return restTemplate.getForObject(
                    driverServiceUrl + "/api/drivers/" + driverId, DriverDTO.class);
        } catch (RestClientException ex) {
            throw new ServiceUnavailableException("Driver Service unavailable: " + ex.getMessage());
        }
    }
}