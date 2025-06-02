package com.cas.yutnoriswing.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 게임 보드의 노드 구조만을 관리하는 클래스
 */
public class Board {
    private List<BoardNode> nodes;
    private BoardNode startNode;
    private final int sides;
    private final float radius;
    
    public Board(int sides, float radius) {
        if (sides < 3) {
            throw new IllegalArgumentException("보드는 최소 3각형 이상이어야 합니다.");
        }
        
        this.sides = sides;
        this.radius = radius;
        initializeBoard();
    }
    
    private void initializeBoard() {
        this.nodes = new ArrayList<>();
        
        // Center 노드 생성
        float centerX = 2.5f;
        float centerY = 2.5f;
        BoardNode center = new BoardNode("Center", centerX, centerY, sides);
        nodes.add(center);
        
        // 외곽 변 노드와 센터로 향하는 노드 배열 생성
        BoardNode[][] edgeNodes = new BoardNode[sides][6];
        BoardNode[] toCenterPath1 = new BoardNode[sides];
        BoardNode[] toCenterPath2 = new BoardNode[sides];
        
        createEdgeNodes(edgeNodes, centerX, centerY);
        createCenterPathNodes(edgeNodes, center, toCenterPath1, toCenterPath2, centerX, centerY);
        connectNodes(edgeNodes, center, toCenterPath1, toCenterPath2);
        
        // 동적으로 첫 번째 모서리 노드를 시작 노드로 설정
        this.startNode = edgeNodes[0][0];
    }
    
    private void createEdgeNodes(BoardNode[][] edgeNodes, float centerX, float centerY) {
        for (int i = 0; i < sides; i++) {
            double angle1 = 2 * Math.PI * i / sides - Math.PI / 2;
            double angle2 = 2 * Math.PI * (i + 1) / sides - Math.PI / 2;
            
            float x1 = (float)(Math.cos(angle1) * radius + centerX);
            float y1 = (float)(Math.sin(angle1) * radius + centerY);
            float x2 = (float)(Math.cos(angle2) * radius + centerX);
            float y2 = (float)(Math.sin(angle2) * radius + centerY);
            
            for (int j = 0; j < 6; j++) {
                float t = j / 5f;
                float x = x1 + (x2 - x1) * t;
                float y = y1 + (y2 - y1) * t;
                
                if (j == 0 && i > 0) {
                    edgeNodes[i][j] = edgeNodes[i - 1][5];
                } else {
                    BoardNode node = new BoardNode("Edge" + i + "-" + j, x, y, sides);
                    edgeNodes[i][j] = node;
                    nodes.add(node);
                }
            }
        }
    }
    
    private void createCenterPathNodes(BoardNode[][] edgeNodes, BoardNode center,
                                     BoardNode[] toCenterPath1, BoardNode[] toCenterPath2,
                                     float centerX, float centerY) {
        for (int i = 0; i < sides; i++) {
            float cx = edgeNodes[i][0].getX();
            float cy = edgeNodes[i][0].getY();
            
            float dx = centerX - cx;
            float dy = centerY - cy;
            
            float midX1 = cx + dx / 3f;
            float midY1 = cy + dy / 3f;
            float midX2 = cx + dx * 2 / 3f;
            float midY2 = cy + dy * 2 / 3f;
            
            BoardNode path1 = new BoardNode("ToCenter" + i + "-1", midX1, midY1, sides);
            BoardNode path2 = new BoardNode("ToCenter" + i + "-2", midX2, midY2, sides);
            
            toCenterPath1[i] = path1;
            toCenterPath2[i] = path2;
            
            nodes.add(path1);
            nodes.add(path2);
        }
    }
    
    private void connectNodes(BoardNode[][] edgeNodes, BoardNode center,
                            BoardNode[] toCenterPath1, BoardNode[] toCenterPath2) {
        for (int i = 0; i < sides; i++) {
            // 외곽 노드 연결
            for (int j = 0; j < 5; j++) {
                edgeNodes[i][j].addNextNode(edgeNodes[i][j + 1]);
            }
            
            // 꼭짓점과 센터 노드 연결
            if (i != 0 && i <= (sides + 1) / 2) {
                edgeNodes[i][0].addNextNode(toCenterPath1[i]);
                toCenterPath1[i].addNextNode(toCenterPath2[i]);
                toCenterPath2[i].addNextNode(center);
            } else {
                center.addNextNode(toCenterPath2[i]);
                toCenterPath2[i].addNextNode(toCenterPath1[i]);
                if (i == 0) {
                    toCenterPath1[i].addNextNode(edgeNodes[sides - 1][5]);
                } else {
                    toCenterPath1[i].addNextNode(edgeNodes[i - 1][5]);
                }
            }
        }
    }

    // 기본적인 getter 메서드들
    public List<BoardNode> getNodes() {
        return new ArrayList<>(nodes);
    }
    
    public BoardNode getStartNode() {
        return startNode;
    }
    
    public BoardNode findNodeByName(String nodeName) {
        return nodes.stream()
                .filter(node -> nodeName.equals(node.getName()))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 현재 노드의 이전 노드를 찾는 메서드
     * @param currentNode 현재 노드
     * @return 이전 노드 (없으면 null)
     */
    public BoardNode findPreviousNode(BoardNode currentNode) {
        if (currentNode == null) return null;
        
        // 모든 노드를 순회하면서 currentNode를 nextNode로 가지는 노드 찾기
        for (BoardNode node : nodes) {
            if (node.getNextNodes().contains(currentNode)) {
                return node;
            }
        }
        return null; // 이전 노드가 없음 (시작 노드인 경우)
    }
    
    public int getSides() {
        return sides;
    }
    
    public float getRadius() {
        return radius;
    }
} 