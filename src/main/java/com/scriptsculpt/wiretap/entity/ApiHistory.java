package com.scriptsculpt.wiretap.entity;

import jakarta.persistence.*;

@Entity
public class ApiHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String url;
    private String method;

    @Column(length=5000)
    private String requestBody;

    @Column(length=10000)
    private String responseBody;

    private int statusCode;
    private long timestamp;

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

    public long getTimestamp() {
        return this.timestamp;
    }

    public  void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
