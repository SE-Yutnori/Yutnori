package com.cas.yutnorifx.model.request;

import com.cas.yutnorifx.model.entity.*;

public class BranchSelectionResponse {
    private final String requestId;
    private final BoardNode selectedBranch;
    private final boolean cancelled;
    
    public BranchSelectionResponse(String requestId, BoardNode selectedBranch) {
        this.requestId = requestId;
        this.selectedBranch = selectedBranch;
        this.cancelled = false;
    }
    
    public BranchSelectionResponse(String requestId, boolean cancelled) {
        this.requestId = requestId;
        this.selectedBranch = null;
        this.cancelled = cancelled;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public BoardNode getSelectedBranch() {
        return selectedBranch;
    }
    
    public boolean isCancelled() {
        return cancelled;
    }
} 