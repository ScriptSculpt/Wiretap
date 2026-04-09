package com.scriptsculpt.wiretap.service;

import com.scriptsculpt.wiretap.dto.ApiHistoryResponse;
import com.scriptsculpt.wiretap.dto.ApiRequest;
import com.scriptsculpt.wiretap.entity.ApiHistory;
import com.scriptsculpt.wiretap.repository.ApiHistoryRespository;
import com.scriptsculpt.wiretap.specification.ApiHistorySpecification;
import jakarta.annotation.PreDestroy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


@Service
public class ApiService {

    private static final int MAX_RETRIES = 3;

    private final ApiHistoryRespository repository;

    private final RestTemplate restTemplate;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

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

        ResponseEntity<String> response;

        try {
            response = restTemplate.exchange(
                    request.getUrl(),
                    method,
                    entity,
                    String.class
            );
        } catch (HttpClientErrorException |HttpServerErrorException ex) {
            response = ResponseEntity
                    .status(ex.getStatusCode())
                    .body(ex.getResponseBodyAsString());
        } catch (Exception ex) {
            response = ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ex.getMessage());
        }

        long end = System.currentTimeMillis();

        ApiHistory history = new ApiHistory();
        history.setUrl(request.getUrl().toLowerCase());
        history.setMethod(request.getMethod().toUpperCase());
        history.setRequestId(UUID.randomUUID().toString());
        history.setRequestBody(request.getBody());
        history.setResponseBody(response.getBody());
        history.setStatusCode(response.getStatusCode().value());
        history.setTimeTaken(end - start);
        history.setRetryCount(0);

        repository.save(history);

        return response;
    }

    public List<ApiHistoryResponse> getAllHistory() {
        List<ApiHistory> history = repository.findAll();

        return history.stream().map(hist -> new ApiHistoryResponse(
                hist.getId(),
                hist.getUrl(),
                hist.getMethod(),
                hist.getStatusCode(),
                hist.getTimeTaken()
        )).toList();
    }

//    public List<ApiHistory> getFailedHistory() {
//        return repository.findByStatusCodeGreaterThanEqual(400);
//    }
//
//    public List<ApiHistory> getSlowHistory(long threshold) {
//        return repository.findByTimestampGreaterThan(threshold);
//    }


    /*
    Type Page to ensure that the frontend can easily access the page info like size, total elements and total pages.
    * */
    public Page<ApiHistory> getPaginatedHistory(int page, int size, String sortField, boolean desc) {
        Sort sort = desc ? Sort.by(sortField).descending() : Sort.by(sortField).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return  repository.findAll(pageable);
    }

//    public Page<ApiHistoryResponse> getHistory(Integer statusCode, Integer minTime, int page, int size, String sortField, boolean desc) {
//        Page<ApiHistory> historyPage;
//
//        Sort sort = desc ? Sort.by(sortField).descending() : Sort.by(sortField).ascending();
//        Pageable pageable = PageRequest.of(page, size, sort);
//
//        if(statusCode != null) {
//            historyPage = repository.findByStatusCode(statusCode, pageable);
//        }
//        else {
//            historyPage = repository.findAll(pageable);
//        }
//
//        return historyPage.map(hist -> new ApiHistoryResponse(
//                hist.getId(),
//                hist.getUrl(),
//                hist.getMethod(),
//                hist.getStatusCode(),
//                hist.getTimestamp()
//        ));
//    }

    public Page<ApiHistoryResponse> getHistory(Integer status, Long minThreshold, Long maxThreshold, String method, String url, String search, Pageable pageable) {

        Specification<ApiHistory> spec = Specification
                .where(ApiHistorySpecification.hasMethod(method))
                .and(ApiHistorySpecification.hasStatus(status))
                .and(ApiHistorySpecification.greaterThanEqualTimeTaken(minThreshold))
                .and(ApiHistorySpecification.lessThanEqualTimeTaken(maxThreshold))
                .and(ApiHistorySpecification.containsUrl(url))
                .and(ApiHistorySpecification.globalSearch(search));


        Page<ApiHistory> historyPage = repository.findAll(spec,pageable);
        return historyPage.map(hist -> new ApiHistoryResponse(
                hist.getId(),
                hist.getUrl(),
                hist.getMethod(),
                hist.getStatusCode(),
                hist.getTimeTaken()
        ));
    }




    public ResponseEntity<String> retryApi(Long id) {
        ApiHistory history = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid apiId"));
        CompletableFuture<ResponseEntity<String>> response= retry(history, true);
        return response.join();
    }

    public ResponseEntity<String> retryFailedApis() {
        List<ApiHistory> failedApis = repository.findByStatusCodeGreaterThanEqual(400);
        Map<String, ApiHistory> lastestFailedApis = new HashMap<>();

        for(ApiHistory history : failedApis) {
            String requestId = history.getRequestId();

            // If not present or current is newer than lastest, update it
            if(!lastestFailedApis.containsKey(requestId) || history.getId() > lastestFailedApis.get(requestId).getId()) {
                lastestFailedApis.put(requestId, history);
            }
        }

        Collection<ApiHistory> failedApisToRetry = lastestFailedApis.values();

//        ExecutorService executorService = Executors.newFixedThreadPool(20);
        List<CompletableFuture<ResponseEntity<String>>> futures = new ArrayList<>();

        for(ApiHistory history : failedApisToRetry) {
            if(history.getRetryCount() >= MAX_RETRIES) continue;
            futures.add(retry(history, false));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        int succeed = 0;
        int failed = 0;

        for(CompletableFuture<ResponseEntity<String>> future : futures) {
            try {
                ResponseEntity<String> response = future.join();
                if(response.getStatusCode().is2xxSuccessful()) {
                    succeed++;
                }
                else  {

                    failed++;
                }
            } catch (Exception ex) {
                failed++;
            }
        }
//        executorService.shutdown();

        return ResponseEntity.ok("Total: " + failedApis.size() + ", Succeed: " + succeed + ", Failed: " + failed);
    }


    private CompletableFuture<ResponseEntity<String>> retry(ApiHistory history, boolean isMaunualRetry) {

        CompletableFuture<ResponseEntity<String>> future = new CompletableFuture<>();

        int retryCount = history.getRetryCount();

        if(!isMaunualRetry && retryCount >= MAX_RETRIES) {
            future.complete(
                    ResponseEntity
                            .status(history.getStatusCode())
                            .body(history.getResponseBody())
            );
            return future;
        }



        String url = history.getUrl();
        HttpMethod method = HttpMethod.valueOf(history.getMethod());
        String body = history.getRequestBody();

        HttpEntity <String> entity = "GET".equalsIgnoreCase(history.getMethod()) ? new HttpEntity<>((String) null) : new HttpEntity<>(body);

        long startTime = System.currentTimeMillis();
        ResponseEntity<String> response;

        try {
            response = restTemplate.exchange(
                    url,
                    method,
                    entity,
                    String.class
            );
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            response = ResponseEntity
                    .status(ex.getStatusCode())
                    .body(ex.getResponseBodyAsString());
        } catch (Exception ex) {
            response = ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ex.getMessage());
        }

        long endTime = System.currentTimeMillis();

        ApiHistory newHistory = new ApiHistory();
        newHistory.setUrl(url);
        newHistory.setMethod(history.getMethod());
        newHistory.setRequestId(history.getRequestId());
        newHistory.setRequestBody(body);
        newHistory.setResponseBody(response.getBody());
        newHistory.setStatusCode(response.getStatusCode().value());
        newHistory.setTimeTaken(endTime - startTime);
        newHistory.setRetryCount(isMaunualRetry ? 0 : retryCount + 1);

        repository.save(newHistory);

        if(response.getStatusCode().is2xxSuccessful()) {
            future.complete(response);
            return future;
        }

        if(!isMaunualRetry) {
            long delay = (long) Math.pow(2, retryCount) * 1000;

            scheduler.schedule(() -> {
                CompletableFuture<ResponseEntity<String>> next = retry(newHistory, false);
                next.thenAccept(result -> future.complete(result));
            }, delay, TimeUnit.MILLISECONDS);
        }

        return future;
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
    }
}
