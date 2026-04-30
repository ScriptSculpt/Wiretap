package com.scriptsculpt.wiretap.dto;

public class RetryResponse {
    private int total;
    private int succeed;
    private int failed;
    private int skipped;

    public RetryResponse(int total, int succeed, int failed, int skipped) {
        this.total = total;
        this.succeed = succeed;
        this.failed = failed;
        this.skipped = skipped;
    }

    public int getTotal() {
        return this.total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getSucceed() {
        return this.succeed;
    }

    public void setSucceed(int succeed) {
        this.succeed = succeed;
    }

    public int getFailed() {
        return this.failed;
    }

    public void setFailed(int failed) {
        this.failed = failed;
    }

    public int getSkipped() {
        return this.skipped;
    }

    public void setSkipped(int skipped) {
        this.skipped = skipped;
    }
}
