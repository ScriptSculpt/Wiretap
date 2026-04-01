package com.scriptsculpt.wiretap.dto;


public class ApiRequest {
    private String url;
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
