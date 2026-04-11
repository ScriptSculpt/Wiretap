package com.scriptsculpt.wiretap.dto;

public class ApiResponse {
    private String responseBody;
    private int statusCode;
    private long timeTaken;
    private String requestId;

    ApiResponse() {

    }

    public ApiResponse(String responseBody, int statusCode, long timeTaken, String requestId) {
        this.responseBody = responseBody;
        this.statusCode = statusCode;
        this.timeTaken = timeTaken;
        this.requestId = requestId;
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

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public long getTimeTaken() {
        return this.timeTaken;
    }

    public void setTimeTaken(long timeTaken) {
        this.timeTaken = timeTaken;
    }

    public String getRequestId() {
        return this.requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
