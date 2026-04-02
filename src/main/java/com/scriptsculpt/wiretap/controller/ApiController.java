package com.scriptsculpt.wiretap.controller;


import com.scriptsculpt.wiretap.dto.ApiHistoryResponse;
import com.scriptsculpt.wiretap.dto.ApiRequest;
import com.scriptsculpt.wiretap.entity.ApiHistory;
import com.scriptsculpt.wiretap.service.ApiService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {
    private  final ApiService apiService;
    public ApiController(ApiService apiService) {
        this.apiService = apiService;
    }

    @PostMapping("/call")
    public ResponseEntity<String> callApi(@RequestBody ApiRequest request) {
        return apiService.callAPI(request);
    }

    @GetMapping("/history")
    public ResponseEntity<List<ApiHistoryResponse>> getHistory() {
        List<ApiHistoryResponse> history = apiService.getAllHistory();
        return ResponseEntity.ok(history);
    }

    @GetMapping("/history/failed")
    public ResponseEntity<List<ApiHistory>> getFailed() {
        List<ApiHistory> failed = apiService.getFailedHistory();
        return  ResponseEntity.ok(failed);
    }

    @GetMapping("/history/slow")
    public ResponseEntity<List<ApiHistory>> getSlow(@RequestParam("threshold") long threshold) {
        List<ApiHistory> slow = apiService.getSlowHistory(threshold);
        return  ResponseEntity.ok(slow);
    }

    @GetMapping("/history/page")
    public ResponseEntity<Page<ApiHistory>> getPageHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "timestamp") String sortField,
            @RequestParam(defaultValue = "true") boolean desc
    ) {
        Page<ApiHistory> history = apiService.getPaginatedHistory(page, size, sortField, desc);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/historyNew")
    public ResponseEntity<Page<ApiHistoryResponse>> getHistoryNew(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "timestamp") String sortField,
            @RequestParam(defaultValue = "true") boolean desc,
            @RequestParam(required = false) Integer statusCode,
            @RequestParam(required = false) Integer minTime
    ) {
        Page<ApiHistoryResponse> history = apiService.getHistory(statusCode, minTime, page, size, sortField, desc);
        return ResponseEntity.ok(history);
    }
}
