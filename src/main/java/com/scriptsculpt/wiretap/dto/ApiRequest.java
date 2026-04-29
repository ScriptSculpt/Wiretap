package com.scriptsculpt.wiretap.dto;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;


public class ApiRequest {

    @NotBlank(message = "URL must not be empty")
    private String url;

    @NotBlank(message = "HTTP Method must not be empty")
    @Pattern(
            regexp = "GET|POST|PUT|DELETE|PATCH",
            flags =  Pattern.Flag.CASE_INSENSITIVE,
            message = "Invalid HTTP Method"
    )
    private String method;
    private String body;

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return this.method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getBody() {
        return  this.body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
