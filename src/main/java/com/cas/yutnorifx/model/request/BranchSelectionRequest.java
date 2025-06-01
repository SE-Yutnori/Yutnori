package com.cas.yutnorifx.model.request;

import com.cas.yutnorifx.model.entity.*;
import java.util.List;

/**
 * 분기 선택 요청 정보를 담는 클래스
 */
public class BranchSelectionRequest {
    private final Token token;
    private final List<BoardNode> branchOptions;
    private final String requestId; // 요청을 구분하기 위한 고유 ID
    
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