package com.scriptsculpt.wiretap.dto;

import java.util.List;

public class RetryResponse {
    private int total;
    private int succeed;
    private int failed;
    private int skipped;

    private List<Long> successIds;
    private List<Long> failedIds;

    public RetryResponse(int total, int succeed, int failed, int skipped, List<Long> successIds, List<Long> failedIds) {
        this.total = total;
        this.succeed = succeed;
        this.failed = failed;
        this.skipped = skipped;
        this.successIds = successIds;
        this.failedIds = failedIds;
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

    public List<Long> getSuccessIds() {
        return this.successIds;
    }

    public void setSuccessIds(List<Long> successIds) {
        this.successIds = successIds;
    }

    public List<Long> getFailedIds() {
        return this.failedIds;
    }

    public void setFailedIds(List<Long> failedIds) {
        this.failedIds = failedIds;
    }
}
