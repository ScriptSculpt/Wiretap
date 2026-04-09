package com.scriptsculpt.wiretap.entity;

import jakarta.persistence.*;

@Entity
public class ApiHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String url;
    private String method;
    private String requestId;

    @Column(length=5000)
    private String requestBody;

    @Column(length=10000)
    private String responseBody;

    private int statusCode;
    private long timeTaken;

    private int retryCount;

    public Long getId() {
        return  id;
    }

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

    public String getRequestId() {
        return this.requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestBody() {
        return this.requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public String getResponseBody() {
        return this.responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public void  setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public long getTimeTaken() {
        return this.timeTaken;
    }

    public void setTimeTaken(long timeTaken) {
        this.timeTaken = timeTaken;
    }

    public int getRetryCount() {
        return this.retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
}
