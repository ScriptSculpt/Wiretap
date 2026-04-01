package com.scriptsculpt.wiretap.service;

import com.scriptsculpt.wiretap.dto.ApiRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ApiService {
    public ResponseEntity<String> callAPI(ApiRequest request) {
        RestTemplate restTemplate = new RestTemplate();

        HttpMethod method = HttpMethod.valueOf(request.getMethod());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(request.getBody(), headers);

        return restTemplate.exchange(
                request.getUrl(),
                method,
                entity,
                String.class
        );
    }
}
