package com.scriptsculpt.wiretap.controller;


import com.scriptsculpt.wiretap.dto.ApiRequest;
import com.scriptsculpt.wiretap.service.ApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
