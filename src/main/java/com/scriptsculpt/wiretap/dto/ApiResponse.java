package com.scriptsculpt.wiretap.dto;

import tools.jackson.databind.ObjectMapper;

public class ApiResponse {
    private Object responseBody;
    private int statusCode;
    private long timeTaken;
    private String requestId;

    private static final ObjectMapper mapper = new ObjectMapper();

    ApiResponse() {

    }

    public ApiResponse(String responseBody, int statusCode, long timeTaken, String requestId) {
        this.responseBody = parseBody(responseBody);
        this.statusCode = statusCode;
        this.timeTaken = timeTaken;
        this.requestId = requestId;
    }

    private Object parseBody(String body) {
        if(body == null) return null;

        body = body.trim();
        if(!(body.startsWith("{") || body.startsWith("["))) {
            return body;
        }

        try{
            return mapper.readValue(body, Object.class);
        } catch (Exception e) {
            // Fallback to string
            return body;
        }
    }

    public Object getResponseBody() {
        return this.responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = parseBody(responseBody);
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
