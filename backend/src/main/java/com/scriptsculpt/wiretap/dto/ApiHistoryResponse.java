package com.scriptsculpt.wiretap.dto;

public class ApiHistoryResponse {
    private Long id;
    private String url;
    private String method;
    private int statusCode;;
    private long timeTaken;

    public ApiHistoryResponse(Long id, String url, String method, int statusCode, long timeTaken) {
        this.id = id;
        this.url = url;
        this.method = method;
        this.statusCode = statusCode;
        this.timeTaken = timeTaken;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public int getStatusCode() {
        return this.statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public long getTimeTaken() {
        return this.timeTaken;
    }

    public void setTimeTaken(long timeTaken) {
        this.timeTaken = timeTaken;
    }
}
