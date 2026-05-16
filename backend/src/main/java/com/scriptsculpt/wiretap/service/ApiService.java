package com.scriptsculpt.wiretap.service;

import com.scriptsculpt.wiretap.dto.ApiHistoryResponse;
import com.scriptsculpt.wiretap.dto.ApiRequest;
import com.scriptsculpt.wiretap.dto.ApiResponse;
import com.scriptsculpt.wiretap.dto.RetryResponse;
import com.scriptsculpt.wiretap.entity.ApiHistory;
import com.scriptsculpt.wiretap.entity.User;
import com.scriptsculpt.wiretap.repository.ApiHistoryRespository;
import com.scriptsculpt.wiretap.repository.UserRepository;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
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

    private final UserRepository userRepository;

    private final RestTemplate restTemplate;

    private static final Logger log = LoggerFactory.getLogger(ApiService.class);

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

    private final Set<String> activeRequestIds = ConcurrentHashMap.newKeySet();

    private final ObjectMapper mapper = new ObjectMapper();

    public ApiService(ApiHistoryRespository repository, RestTemplate restTemplate, UserRepository userRepository) {
        this.repository = repository;
        this.restTemplate = restTemplate;
        this.userRepository = userRepository;
    }


    public ApiResponse executeApi(ApiRequest request) {
        HttpMethod method = resolveMethod(request.getMethod());

        String apiRequestId = UUID.randomUUID().toString();
        long start = System.currentTimeMillis();

        LocalDateTime now = LocalDateTime.now();
        
        log.info("Executing API request. requestId={}, method={}, url={}", apiRequestId, method, request.getUrl());
        try {
            HttpHeaders headers = new HttpHeaders();
            if(request.getHeaders() != null) {
                request.getHeaders().forEach((key, value) -> headers.set(key, value));
            }

            System.out.println("Headers in execute: "+ request.getHeaders());
            System.out.println("Transformed Headers in execute: "+ headers);

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
                    request.getHeaders(),
                    response,
                    timeTaken,
                    apiRequestId,
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
            log.error("API request resulted in error response. requestId={}, method={}, url={}, statusCode={}", apiRequestId, method, request.getUrl(), ex.getStatusCode(), ex);
            ResponseEntity<String> response = ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());

            ApiHistory history = buildHistory(
                    request.getUrl(),
                    request.getMethod(),
                    request.getBody(),
                    request.getHeaders(),
                    response,
                    timeTaken,
                    apiRequestId,
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
            log.error("Error executing API request. requestId={}, method={}, url={}", apiRequestId, method, request.getUrl(), ex);
            ResponseEntity<String> response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());

            ApiHistory history = buildHistory(
                    request.getUrl(),
                    request.getMethod(),
                    request.getBody(),
                    request.getHeaders(),
                    response,
                    timeTaken,
                    apiRequestId,
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

    public Page<ApiHistoryResponse> getHistory(Integer status, Long minThreshold, Long maxThreshold, String method, String url, String search, Pageable pageable, List<Long> ids) {
        log.info("Fetching API history with filters - status: {}, timeTaken: {}-{}, method: {}, url: {}, search: {}, ids: {}, page: {}, size: {}", status, minThreshold, maxThreshold, method, url, search, ids, pageable.getPageNumber(), pageable.getPageSize());


        Specification<ApiHistory> spec = Specification
                .where(ApiHistorySpecification.belongsToUser(getCurrentUser().getId()))
                .and(ApiHistorySpecification.hasMethod(method))
                .and(ApiHistorySpecification.hasStatus(status))
                .and(ApiHistorySpecification.greaterThanEqualTimeTaken(minThreshold))
                .and(ApiHistorySpecification.lessThanEqualTimeTaken(maxThreshold))
                .and(ApiHistorySpecification.containsUrl(url))
                .and(ApiHistorySpecification.globalSearch(search))
                .and(ApiHistorySpecification.hasIds(ids));


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
        log.info("Deleting API history record with id: {}", id);
        if(!repository.existsById(id)) {
            throw new IllegalArgumentException("No record found with id: "+ id);
        }
        repository.deleteById(id);
    }

    @Transactional
    public int deleteByStatus(String status) {
        log.info("Deleting API history records with status: {}", status);
        if("SUCCEED".equalsIgnoreCase(status)) {
            return repository.deleteByStatusCodeLessThan(400);
        }else if("FAILED".equalsIgnoreCase(status)) {
            return repository.deleteByStatusCodeGreaterThanEqual(400);
        }
        throw new IllegalArgumentException("Invalid status: " + status);
    }

    public long deleteAll() {
        log.info("Deleting all API history records");
        long count = repository.count();
        if(count == 0) {
            throw new IllegalArgumentException("No records found");
        }
        repository.deleteAll();
        return count;
    }

    public ApiResponse retryApi(Long id) {
    log.info("Retrying API call with id: {}", id);
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
        log.info("Retrying failed API calls");
        List<ApiHistory> failedApis = repository.findByStatusCodeGreaterThanEqual(400);
        Map<String, ApiHistory> lastestFailedApis = new HashMap<>();

        for(ApiHistory history : failedApis) {
            String requestId = history.getRequestId();

            if(lastestFailedApis.containsKey(requestId)) {
                continue;
            }

            ApiHistory lastestHistory = repository.findTopByRequestIdOrderByIdDesc(requestId);

            // If not present update it only if the lastest history for the request id is failed
            if(lastestHistory.getStatusCode() >= 400) {
                lastestFailedApis.put(requestId, history);
            }
        }

        Collection<ApiHistory> failedApisToRetry = lastestFailedApis.values();

        // ExecutorService executorService = Executors.newFixedThreadPool(20);
        List<CompletableFuture<ResponseEntity<String>>> futures = new ArrayList<>();
        List<ApiHistory> historyList = new ArrayList<>();

        int skipped = 0;

        for(ApiHistory history : failedApisToRetry) {
            if(!isRetryable(history.getStatusCode())) {
                skipped++;
                continue;
            }
            historyList.add(history);
            futures.add(retry(history, false));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        int succeed = 0;
        int failed = 0;

        List<Long> successIds = new ArrayList<>();
        List<Long> failedIds = new ArrayList<>();

        for(int i=0; i< futures.size(); i++) {
            CompletableFuture<ResponseEntity<String>> future = futures.get(i);
            ApiHistory history = historyList.get(i);
            try {
                ResponseEntity<String> response = future.join();
                if(response.getStatusCode().is2xxSuccessful()) {
                    succeed++;
                    successIds.add(history.getId());
                }
                else  {
                    failed++;
                    failedIds.add(history.getId());
                }
            } catch (Exception ex) {
                failed++;
                failedIds.add(history.getId());
            }
        }
//        executorService.shutdown();

        int total = succeed + failed + skipped;
        return new RetryResponse(
                total,
                succeed,
                failed,
                skipped,
                successIds,
                failedIds
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

        return retryInternal(history, isManualRetry, 0);
    }

    private CompletableFuture<ResponseEntity<String>> retryInternal(ApiHistory history, boolean isManualRetry, int currentAttempt) {

        CompletableFuture<ResponseEntity<String>> future = new CompletableFuture<>();

        int retryCount = currentAttempt;

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

        HttpHeaders headers = new HttpHeaders();
        Map<String, String> headerMap = new HashMap<>();
        if(history.getHeaders() != null) {
            try{
                headerMap = mapper.readValue(history.getHeaders(), new TypeReference<>() {});
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
                headerMap,
                response,
                endTime-startTime,
                history.getRequestId(),
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
                CompletableFuture<ResponseEntity<String>> next = retryInternal(newHistory, false, retryCount+1);
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

    private static final Set<Integer> RETRYABLE_CODES = Set.of(500, 502, 503, 504, 429);

    private boolean isRetryable(int statusCode) {
        return RETRYABLE_CODES.contains(statusCode);
    }

    private ApiHistory buildHistory(String url, String method, String requestBody,  Map<String, String> requestHeaders ,ResponseEntity<String> response, long timeTaken, String requestId, LocalDateTime timestamp) {
        System.out.println("Building history: " + requestBody);

        ObjectMapper mapper = new ObjectMapper();
        String headers = mapper.writeValueAsString(requestHeaders);

        ApiHistory newHistory = new ApiHistory();
        newHistory.setUrl(url);
        newHistory.setMethod(method);
        newHistory.setRequestId(requestId);
        newHistory.setRequestBody(requestBody);
        newHistory.setHeaders(headers);
        newHistory.setResponseBody(truncate(response.getBody()));
        newHistory.setStatusCode(response.getStatusCode().value());
        newHistory.setTimeTaken(timeTaken);
        newHistory.setTimestamp(timestamp);
        newHistory.setUser(getCurrentUser());

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

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Authentication required");
        }

        String username = auth.getName();

        return userRepository.findByUsername(username).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated"));
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down scheduler");
        scheduler.shutdown();
    }
}
