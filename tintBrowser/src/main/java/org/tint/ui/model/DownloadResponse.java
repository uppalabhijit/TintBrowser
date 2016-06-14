package org.tint.ui.model;

/**
 * User: Abhijit
 * Date: 2016-06-12
 */
public class DownloadResponse {
    private final int status;
    private final int failureReason;
    private final String localUri;

    public DownloadResponse(int status, int failureReason, String localUri) {
        this.status = status;
        this.failureReason = failureReason;
        this.localUri = localUri;
    }

    public int getStatus() {
        return status;
    }

    public int getFailureReason() {
        return failureReason;
    }

    public String getLocalUri() {
        return localUri;
    }
}
