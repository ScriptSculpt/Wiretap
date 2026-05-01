package com.scriptsculpt.wiretap.service;

import com.scriptsculpt.wiretap.dto.ApiHistoryResponse;
import com.scriptsculpt.wiretap.dto.ApiRequest;
import com.scriptsculpt.wiretap.dto.ApiResponse;
import com.scriptsculpt.wiretap.dto.RetryResponse;
import com.scriptsculpt.wiretap.entity.ApiHistory;
import com.scriptsculpt.wiretap.repository.ApiHistoryRespository;
import com.scriptsculpt.wiretap.specification.ApiHistorySpecification;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


@Service
public class ApiService {

    private static final int MAX_RETRIES = 3;

    private static final int MAX_BODY_SIZE=10000;

    private final ApiHistoryRespository repository;

    private final RestTemplate restTemplate;

    private static final Logger log = LoggerFactory.getLogger(ApiService.class);

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

    private final Set<String> activeRequestIds = ConcurrentHashMap.newKeySet();

    private final ObjectMapper mapper = new ObjectMapper();

    public ApiService(ApiHistoryRespository repository, RestTemplate restTemplate) {
        this.repository = repository;
        this.restTemplate = restTemplate;
    }


    public ApiResponse executeApi(ApiRequest request) {
        HttpMethod method = resolveMethod(request.getMethod());

        String apiRequestId = UUID.randomUUID().toString();
        long start = System.currentTimeMillis();

        LocalDateTime now = LocalDateTime.now();

        try {
            HttpHeaders headers = new HttpHeaders();
            if(request.getHeaders() != null) {
                request.getHeaders().forEach((key, value) -> headers.set(key, value));
            }

            boolean hasBody = request.getBody() != null && !request.getBody().isBlank();
            HttpEntity<String> entity = hasBody ? new HttpEntity<>(request.getBody(), headers) : new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    request.getUrl(),
                    method,
                    entity,
                    String.class
            );

            long timeTaken = System.currentTimeMillis() - start;

            ApiHistory history = buildHistory(
                    request.getUrl(),
                    request.getMethod(),
                    request.getBody(),
                    response,
                    timeTaken,
                    apiRequestId,
                    0,
                    now
            );

            saveHistory(history);

            return new ApiResponse(
                    response.getBody(),
                    response.getStatusCode().value(),
                    timeTaken,
                    apiRequestId
            );
        } catch (HttpStatusCodeException ex) {
            long timeTaken = System.currentTimeMillis() - start;
//            saveErrorHistory(request, ex, timeTaken, apiRequestId, 0);

            ResponseEntity<String> response = ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());

            ApiHistory history = buildHistory(
                    request.getUrl(),
                    request.getMethod(),
                    request.getBody(),
                    response,
                    timeTaken,
                    apiRequestId,
                    0,
                    now
            );

            saveHistory(history);

            return new ApiResponse(
                    ex.getResponseBodyAsString(),
                    ex.getStatusCode().value(),
                    timeTaken,
                    apiRequestId
            );
        } catch (Exception ex) {
            long timeTaken = System.currentTimeMillis() - start;
//            saveGenericErrorHistory(request, ex, timeTaken, apiRequestId, 0);

            ResponseEntity<String> response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());

            ApiHistory history = buildHistory(
                    request.getUrl(),
                    request.getMethod(),
                    request.getBody(),
                    response,
                    timeTaken,
                    apiRequestId,
                    0,
                    now
            );

            saveHistory(history);

            return new ApiResponse(
                    ex.getMessage(),
                    500,
                    timeTaken,
                    apiRequestId
            );
        }
    }

//    public List<ApiHistoryResponse> getAllHistory() {
//        List<ApiHistory> history = repository.findAll();
//
//        return history.stream().map(hist -> new ApiHistoryResponse(
//                hist.getId(),
//                hist.getUrl(),
//                hist.getMethod(),
//                hist.getStatusCode(),
//                hist.getTimeTaken()
//        )).toList();
//    }

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
//    public Page<ApiHistory> getPaginatedHistory(int page, int size, String sortField, boolean desc) {
//        Sort sort = desc ? Sort.by(sortField).descending() : Sort.by(sortField).ascending();
//        Pageable pageable = PageRequest.of(page, size, sort);
//        return  repository.findAll(pageable);
//    }

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
                hist.getTimeTaken(),
                hist.getTimestamp()
        ));
    }

    public void deleteById(Long id) {
        if(!repository.existsById(id)) {
            throw new IllegalArgumentException("No record found with id: "+ id);
        }
        repository.deleteById(id);
    }

    @Transactional
    public int deleteByStatus(String status) {
        if("SUCCEED".equalsIgnoreCase(status)) {
            return repository.deleteByStatusCodeLessThan(400);
        }else if("FAILED".equalsIgnoreCase(status)) {
            return repository.deleteByStatusCodeGreaterThanEqual(400);
        }
        throw new IllegalArgumentException("Invalid status: " + status);
    }

    public long deleteAll() {
        long count = repository.count();
        if(count == 0) {
            throw new IllegalArgumentException("No records found");
        }
        repository.deleteAll();
        return count;
    }

    public ApiResponse retryApi(Long id) {
        ApiHistory history = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid apiId"));

        long start = System.currentTimeMillis();
        CompletableFuture<ResponseEntity<String>> future= retry(history, true);

        try {
            ResponseEntity<String> res = future.join();

            long timeTaken = System.currentTimeMillis() - start;

            return new ApiResponse(
                    res.getBody(),
                    res.getStatusCode().value(),
                    timeTaken,
                    history.getRequestId()
            );
        } catch (Exception ex){
            Throwable cause = (ex.getCause() != null) ? ex.getCause() : ex;
            throw new RuntimeException("Retry failed due to external API error", cause);
        }
    }

    public RetryResponse retryFailedApis() {
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

        // ExecutorService executorService = Executors.newFixedThreadPool(20);
        List<CompletableFuture<ResponseEntity<String>>> futures = new ArrayList<>();

        int skipped = 0;

        for(ApiHistory history : failedApisToRetry) {
            if(history.getRetryCount() >= MAX_RETRIES) {
                skipped++;
                continue;
            }
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

        int total = futures.size() + skipped;
        return new RetryResponse(
                total,
                succeed,
                failed,
                skipped
        );
    }


    private CompletableFuture<ResponseEntity<String>> retry(ApiHistory history, boolean isManualRetry) {
        String requestId = history.getRequestId();

        boolean acquired = activeRequestIds.add(requestId);

        if(!acquired) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.CONFLICT).body("Retry already in progress for request id: " + requestId)
            );
        }

        return retryInternal(history, isManualRetry);
    }

    private CompletableFuture<ResponseEntity<String>> retryInternal(ApiHistory history, boolean isManualRetry) {

        CompletableFuture<ResponseEntity<String>> future = new CompletableFuture<>();

        int retryCount = history.getRetryCount();

        if(!isManualRetry && retryCount >= MAX_RETRIES) {
            future.complete(
                    ResponseEntity
                            .status(history.getStatusCode())
                            .body(history.getResponseBody())
            );
            activeRequestIds.remove(history.getRequestId());
            return future;
        }



        String url = history.getUrl();
        HttpMethod method = resolveMethod(history.getMethod());
        String body = history.getRequestBody();

//        HttpEntity <String> entity = "GET".equalsIgnoreCase(history.getMethod()) ? new HttpEntity<>((String) null) : new HttpEntity<>(body);

        HttpHeaders headers = new HttpHeaders();
        if(history.getHeaders() != null) {
            try{
                Map<String, String> headerMap = mapper.readValue(history.getHeaders(), new TypeReference<>() {});
                headerMap.forEach((key, value) -> headers.set(key, value));
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse headers", e);
            }

        }

        boolean hasBody = body != null && !body.isEmpty();

        if(hasBody && headers.getContentType() == null) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }

        HttpEntity<String> entity = hasBody ? new HttpEntity<>(body, headers) : new HttpEntity<>(headers);

        LocalDateTime now = LocalDateTime.now();
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

        ApiHistory newHistory = buildHistory(
                url,
                history.getMethod(),
                body,
                response,
                endTime-startTime,
                history.getRequestId(),
                isManualRetry ? 0 : retryCount + 1,
                now
        );

        saveHistory(newHistory);

        if(response.getStatusCode().is2xxSuccessful()) {
            future.complete(response);
            activeRequestIds.remove(history.getRequestId());
            return future;
        }

        if(!isManualRetry) {
            // Adding exponential backoff when not manual retry
            // Delay doubles every time
            long delay = (long) Math.pow(2, retryCount) * 1000;

            scheduler.schedule(() -> {
                CompletableFuture<ResponseEntity<String>> next = retryInternal(newHistory, false);
                next.whenComplete((result,ex) -> {
                    if(ex != null) {
                        future.complete(
                                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage())
                        );
                    }
                    else future.complete(result);
                });
            }, delay, TimeUnit.MILLISECONDS);
        }
        else {
            future.complete(response);
            activeRequestIds.remove(history.getRequestId());
        }

        return future;
    }

    private ApiHistory buildHistory(String url, String method, String requestBody, ResponseEntity<String> response, long timeTaken, String requestId, int retryCount, LocalDateTime timestamp) {
        ApiHistory newHistory = new ApiHistory();
        newHistory.setUrl(url);
        newHistory.setMethod(method);
        newHistory.setRequestId(requestId);
        newHistory.setRequestBody(requestBody);
        newHistory.setResponseBody(truncate(response.getBody()));
        newHistory.setStatusCode(response.getStatusCode().value());
        newHistory.setTimeTaken(timeTaken);
        newHistory.setRetryCount(retryCount);
        newHistory.setTimestamp(timestamp);

        return  newHistory;
    }

    private void saveHistory(ApiHistory history) {
        try {
            repository.save(history);
        } catch (Exception e) {
            // catch db related exceptions
            // Output will look like `Failed to save history for requestId=abc123, url=http://example.com`
            log.error("Failed to save history for requestId={}, url={}", history.getRequestId(), history.getUrl(), e);
        }
    }

    // Truncate the larger responses before storing in db
    private String truncate(String body) {
        if(body==null) return null;

        if(body.length()>MAX_BODY_SIZE) return body.substring(0, MAX_BODY_SIZE) + "...[TRUNCATED]";

        return body;
    }

    private HttpMethod resolveMethod(String method) {
        try {
            return HttpMethod.valueOf(method.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid HTTP method: " + method);
        }
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
    }
}
