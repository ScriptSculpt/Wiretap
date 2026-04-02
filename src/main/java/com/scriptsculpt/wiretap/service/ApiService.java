package com.scriptsculpt.wiretap.service;

import com.scriptsculpt.wiretap.dto.ApiHistoryResponse;
import com.scriptsculpt.wiretap.dto.ApiRequest;
import com.scriptsculpt.wiretap.entity.ApiHistory;
import com.scriptsculpt.wiretap.repository.ApiHistoryRespository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;


@Service
public class ApiService {

    private final ApiHistoryRespository repository;

    private final RestTemplate restTemplate;

    public ApiService(ApiHistoryRespository repository, RestTemplate restTemplate) {
        this.repository = repository;
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<String> callAPI(ApiRequest request) {
//        RestTemplate restTemplate = new RestTemplate();

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

    public List<ApiHistoryResponse> getAllHistory() {
        List<ApiHistory> history = repository.findAll();

        return history.stream().map(hist -> new ApiHistoryResponse(
                hist.getId(),
                hist.getUrl(),
                hist.getMethod(),
                hist.getStatusCode(),
                hist.getTimestamp()
        )).toList();
    }

    public List<ApiHistory> getFailedHistory() {
        return repository.findByStatusCodeGreaterThanEqual(400);
    }

    public List<ApiHistory> getSlowHistory(long threshold) {
        return repository.findByTimestampGreaterThan(threshold);
    }


    /*
    Type Page to ensure that the frontend can easily access the page info like size, total elements and total pages.
    * */
    public Page<ApiHistory> getPaginatedHistory(int page, int size, String sortField, boolean desc) {
        Sort sort = desc ? Sort.by(sortField).descending() : Sort.by(sortField).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return  repository.findAll(pageable);
    }

    public Page<ApiHistoryResponse> getHistory(Integer statusCode, Integer minTime, int page, int size, String sortField, boolean desc) {
        Page<ApiHistory> historyPage;

        Sort sort = desc ? Sort.by(sortField).descending() : Sort.by(sortField).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        if(statusCode != null) {
            historyPage = repository.findByStatusCode(statusCode, pageable);
        }
        else {
            historyPage = repository.findAll(pageable);
        }

        return historyPage.map(hist -> new ApiHistoryResponse(
                hist.getId(),
                hist.getUrl(),
                hist.getMethod(),
                hist.getStatusCode(),
                hist.getTimestamp()
        ));
    }
}
