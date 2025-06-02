package com.cas.yutnorifx.view;

import com.cas.yutnorifx.model.BoardNode;
import com.cas.yutnorifx.model.Player;
import com.cas.yutnorifx.model.Token;
import com.cas.yutnorifx.model.TokenState;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

public class BoardView extends Pane {
    private final List<BoardNode> nodes;
    private final List<Player> players;
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final Color[] playerColors = {
            Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA
    };
    
    // 첫 번째 노드 (시작 노드) 참조
    private final BoardNode startNode;

    private static final int SPACING = 140;
    private static final int OFFSET = 10;

    public BoardView(List<BoardNode> nodes, List<Player> players) {
        this.nodes = nodes;
        this.players = players;
        this.canvas = new Canvas(SPACING * 6, SPACING * 6);
        this.gc = canvas.getGraphicsContext2D();
        getChildren().add(canvas);
        
        // 시작 노드 찾기 (첫 번째 모서리 노드)
        this.startNode = nodes.stream()
                .filter(node -> node.getName().matches("Edge\\d+-0"))
                .findFirst()
                .orElse(null);
        
        refresh();
    }

    public void refresh() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // 노드 간 연결선 및 분기점 연결선 그리기
        for (BoardNode node : nodes) {
            List<BoardNode> nextNodes = node.getNextNodes();
            int size = nextNodes.size();
            int x1 = (int) (node.getX() * SPACING + OFFSET);
            int y1 = (int) (node.getY() * SPACING + OFFSET);

            // 분기점이거나 시작 노드인 경우 특별한 화살표 그리기
            if (size >= 2 || node.equals(startNode)) {
                for (int k = 1; k < size - 1; k++) {
                    BoardNode mid = nextNodes.get(k);
                    int mx = (int) (mid.getX() * SPACING + OFFSET);
                    int my = (int) (mid.getY() * SPACING + OFFSET);
                    gc.setStroke(Color.DARKGRAY);
                    gc.setLineWidth(1);
                    gc.strokeLine(x1, y1, mx, my);
                }

                int[] idx = new int[]{0, size - 1};
                for (int j : idx) {
                    if (j < 0 || j >= size) continue;
                    BoardNode next = nextNodes.get(j);
                    int x2 = (int) (next.getX() * SPACING + OFFSET);
                    int y2 = (int) (next.getY() * SPACING + OFFSET);

                    double dist = Math.hypot(x2 - x1, y2 - y1);
                    double ratio = (dist - 22) / dist;
                    int tx = (int) (x1 + (x2 - x1) * ratio);
                    int ty = (int) (y1 + (y2 - y1) * ratio);

                    gc.setStroke(Color.DARKGRAY);
                    gc.setLineWidth(3);
                    gc.strokeLine(x1, y1, tx, ty);
                    drawArrowHead(x1, y1, tx, ty);

                    int midX = (2 * x1 + 3 * x2) / 5;
                    int midY = (2 * y1 + 3 * y2) / 5;
                    String label = next.getName().contains("Edge")
                            ? "1"
                            : next.getName().replace("ToCenter", "");
                    gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
                    gc.setFill(Color.RED);
                    gc.fillText(label, midX, midY);
                }
            } else if (size == 1) {
                BoardNode only = nextNodes.get(0);
                int x2 = (int) (only.getX() * SPACING + OFFSET);
                int y2 = (int) (only.getY() * SPACING + OFFSET);

                gc.setStroke(Color.DARKGRAY);
                gc.setLineWidth(1);
                gc.strokeLine(x1, y1, x2, y2);
            }
        }

        // 노드 (원) 그리기
        for (BoardNode node : nodes) {
            int x = (int) (node.getX() * SPACING + OFFSET);
            int y = (int) (node.getY() * SPACING + OFFSET);

            int out = 56;
            int inner = 36;
            // 중요한 노드들 (Center, 모서리 시작점, 모서리 끝점)은 특별한 원으로 표시
            boolean isImportantNode = node.getName().equals("Center") || 
                                    node.getName().matches("Edge\\d+-0") || 
                                    node.getName().matches("Edge\\d+-5"); // 현재는 6개 노드(0~5)로 고정되어 있음
            
            if (isImportantNode) {
                int outX = x - out / 2;
                int outY = y - out / 2;
                gc.setFill(Color.WHITE);
                gc.fillOval(outX, outY, out, out);
                gc.setStroke(Color.BLACK);
                gc.strokeOval(outX, outY, out, out);

                int inX = x - inner / 2;
                int inY = y - inner / 2;
                gc.setFill(Color.WHITE);
                gc.fillOval(inX, inY, inner, inner);
                gc.setStroke(Color.BLACK);
                gc.strokeOval(inX, inY, inner, inner);
            } else {
                int drawX = x - inner / 2;
                int drawY = y - inner / 2;
                gc.setFill(Color.WHITE);
                gc.fillOval(drawX, drawY, inner, inner);
                gc.setStroke(Color.BLACK);
                gc.strokeOval(drawX, drawY, inner, inner);
            }

            // 시작 노드 표시 (동적으로 찾은 시작 노드)
            if (node.equals(startNode)) {
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
                gc.setFill(Color.RED);
                int textX = x - 15;
                int textY = y - out/2;
                gc.fillText("start", textX, textY);
            }
        }

        // 말 표시
        for (BoardNode node : nodes) {
            List<Token> tokens = node.getTokens();
            List<Token> activeTokens = tokens.stream()
                    .filter(t -> t.getState() == TokenState.ACTIVE)
                    .toList();

            if (activeTokens.isEmpty()) continue;

            Token top = activeTokens.get(0);
            Player owner = top.getOwner();
            int playerIndex = players.indexOf(owner);
            Color color = playerColors[playerIndex % playerColors.length];

            int cx = (int) (node.getX() * SPACING + OFFSET - 10);
            int cy = (int) (node.getY() * SPACING + OFFSET - 10);

            gc.setFill(color);
            gc.fillOval(cx, cy, 20, 20);
            gc.setStroke(Color.BLACK);
            gc.strokeOval(cx, cy, 20, 20);

            // 대표 토큰과 업힌 토큰들의 이름을 모두 표시
            String baseName = top.getName().split("-")[0];
            List<String> allIndices = new java.util.ArrayList<>();
            
            // 노드에 실제로 있는 토큰들의 인덱스 추가
            for (Token t : activeTokens) {
                allIndices.add(t.getName().split("-")[1]);
            }
            
            // 업힌 토큰들의 인덱스도 추가
            for (Token t : activeTokens) {
                for (Token stacked : t.getStackedTokens()) {
                    allIndices.add(stacked.getName().split("-")[1]);
                }
            }

            String label = baseName + " - " + String.join(",", allIndices);

            gc.setFont(Font.font("Arial", FontWeight.BOLD, 15));
            gc.fillText(label, cx - 5, cy - 5);
        }
    }

    private void drawArrowHead(int x1, int y1, int x2, int y2) {
        double theta = Math.atan2(y2 - y1, x2 - x1);
        double rho = theta + Math.toRadians(30);
        int x = (int) (x2 - 8 * Math.cos(rho));
        int y = (int) (y2 - 8 * Math.sin(rho));
        gc.strokeLine(x2, y2, x, y);

        rho = theta - Math.toRadians(30);
        x = (int) (x2 - 8 * Math.cos(rho));
        y = (int) (y2 - 8 * Math.sin(rho));
        gc.strokeLine(x2, y2, x, y);
    }
} 