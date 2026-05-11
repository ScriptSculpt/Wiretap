package com.scriptsculpt.wiretap.controller;


import com.scriptsculpt.wiretap.dto.ApiHistoryResponse;
import com.scriptsculpt.wiretap.dto.ApiRequest;
import com.scriptsculpt.wiretap.dto.ApiResponse;
import com.scriptsculpt.wiretap.dto.RetryResponse;
import com.scriptsculpt.wiretap.entity.ApiHistory;
import com.scriptsculpt.wiretap.service.ApiService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {
    private  final ApiService apiService;
    public ApiController(ApiService apiService) {
        this.apiService = apiService;
    }


    @PostMapping("/execute")
    public ResponseEntity<ApiResponse> execute(@Valid @RequestBody ApiRequest request) {
        ApiResponse response = apiService.executeApi(request);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }


    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>>  getHistory(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            @RequestParam(name = "sortField", defaultValue = "timestamp") String sortField,
            @RequestParam(name = "desc", defaultValue = "true") boolean desc,
            @RequestParam(name = "status", required = false) Integer status,
            @RequestParam(name = "minThreshold", required = false) Long minThreshold,
            @RequestParam(name = "maxThreshold", required = false) Long maxThreshold,
            @RequestParam(name = "method", required = false) String method,
            @RequestParam(name = "url", required = false) String url,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "ids", required = false) List<Long> ids
    ) {

        Sort sort = desc ? Sort.by(sortField).descending() : Sort.by(sortField).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ApiHistoryResponse> history = apiService.getHistory(status, minThreshold, maxThreshold, method, url, search, pageable, ids);

        Map<String, Object> response = new HashMap<>();
        response.put("content", history.getContent());
        response.put("page", history.getNumber());
        response.put("size", history.getSize());
        response.put("totalElements", history.getTotalElements());
        response.put("totalPages", history.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/{id}/retry")
    public ResponseEntity<ApiResponse> retry(@PathVariable(name = "id") Long id) {
        ApiResponse response = apiService.retryApi(id);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/history/retry-failed")
    public ResponseEntity<RetryResponse> retryFailed() {
        RetryResponse response = apiService.retryFailedApis();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/history/{id}/delete")
    public ResponseEntity<String> deleteApi(@PathVariable(name="id") Long id) {
        apiService.deleteById(id);
        return ResponseEntity.ok("Deleted record with id "+ id);
    }

    @DeleteMapping("/history/delete")
    public ResponseEntity<String> deleteByStatus(@RequestParam(name = "status") String status) {
        int deleted = apiService.deleteByStatus(status);
        if(deleted == 0) {
            throw new IllegalArgumentException("No records found for status: " + status);
        }
        return ResponseEntity.ok("Deleted " + deleted + " records with status "+ status);
    }

    @DeleteMapping("/history/delete-all")
    public ResponseEntity<String> deleteAll(
            @RequestParam(name = "confirm", required = true) String confirm
    ) {
        if(!"YES".equalsIgnoreCase(confirm)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid confirmation");
        }
        long deleted = apiService.deleteAll();
        return ResponseEntity.ok("All " + deleted + " records deleted");
    }

}
