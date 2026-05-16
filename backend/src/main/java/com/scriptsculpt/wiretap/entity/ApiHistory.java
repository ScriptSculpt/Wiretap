package com.scriptsculpt.wiretap.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class ApiHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String url;
    private String method;
    private String requestId;

    @Column(columnDefinition="TEXT")
    private String requestBody;

    @Column(length = 2000)
    private String headers;

    @Column(columnDefinition="TEXT")
    private String responseBody;

    private int statusCode;
    private long timeTaken;

    private LocalDateTime timestamp;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;



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

    public String getHeaders() {
        return this.headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
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

    public LocalDateTime getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
