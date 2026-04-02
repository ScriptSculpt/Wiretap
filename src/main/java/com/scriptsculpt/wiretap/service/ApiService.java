package com.scriptsculpt.wiretap.service;

import com.scriptsculpt.wiretap.dto.ApiRequest;
import com.scriptsculpt.wiretap.entity.ApiHistory;
import com.scriptsculpt.wiretap.repository.ApiHistoryRespository;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ApiService {

    private final ApiHistoryRespository repository;;

    public ApiService(ApiHistoryRespository repository) {
        this.repository = repository;
    }

    public ResponseEntity<String> callAPI(ApiRequest request) {
        RestTemplate restTemplate = new RestTemplate();

        HttpMethod method = HttpMethod.valueOf(request.getMethod());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(request.getBody(), headers);

        long start = System.currentTimeMillis();

        ResponseEntity<String> response = restTemplate.exchange(
                request.getUrl(),
                method,
                entity,
                String.class
        );

        long end = System.currentTimeMillis();

        ApiHistory history = new ApiHistory();
        history.setUrl(request.getUrl());
        history.setMethod(request.getMethod());
        history.setRequestBody(request.getBody());
        history.setResponseBody(response.getBody());
        history.setStatusCode(response.getStatusCode().value());
        history.setTimestamp(end - start);

        repository.save(history);

        return  response;
    }
}
