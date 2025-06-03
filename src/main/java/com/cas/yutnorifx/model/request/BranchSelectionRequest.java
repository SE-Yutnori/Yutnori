package com.cas.yutnorifx.model.request;

import com.cas.yutnorifx.model.entity.*;
import java.util.List;

public class BranchSelectionRequest {
    private final Token token;
    private final List<BoardNode> branchOptions;
    private final String requestId; // 요청 구분을 위한 ID
    
    public BranchSelectionRequest(Token token, List<BoardNode> branchOptions, String requestId) {
        this.token = token;
        this.branchOptions = branchOptions;
        this.requestId = requestId;
    }
    
    public Token getToken() {
        return token;
    }
    
    public List<BoardNode> getBranchOptions() {
        return branchOptions;
    }
    
    public String getRequestId() {
        return requestId;
    }
} 