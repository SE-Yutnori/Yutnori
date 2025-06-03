package com.cas.yutnorifx.model.request;

public class YutTestResponse {
    private final String requestId;
    private final int selectedYutResult;
    private final boolean cancelled;
    
    public YutTestResponse(String requestId, int selectedYutResult) {
        this.requestId = requestId;
        this.selectedYutResult = selectedYutResult;
        this.cancelled = false;
    }
    
    public YutTestResponse(String requestId, boolean cancelled) {
        this.requestId = requestId;
        this.selectedYutResult = -999;
        this.cancelled = cancelled;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public int getSelectedYutResult() {
        return selectedYutResult;
    }
    
    public boolean isCancelled() {
        return cancelled;
    }
} 