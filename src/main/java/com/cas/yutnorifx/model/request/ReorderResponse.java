package com.cas.yutnorifx.model.request;

import java.util.ArrayList;
import java.util.List;

public class ReorderResponse {
    private final String requestId;
    private final List<Integer> reorderedResults;
    private final boolean cancelled;
    
    public ReorderResponse(String requestId, List<Integer> reorderedResults) {
        this.requestId = requestId;
        this.reorderedResults = new ArrayList<>(reorderedResults);
        this.cancelled = false;
    }
    
    public ReorderResponse(String requestId, boolean cancelled) {
        this.requestId = requestId;
        this.reorderedResults = null;
        this.cancelled = cancelled;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public List<Integer> getReorderedResults() {
        return reorderedResults != null ? new ArrayList<>(reorderedResults) : null;
    }
    
    public boolean isCancelled() {
        return cancelled;
    }
} 