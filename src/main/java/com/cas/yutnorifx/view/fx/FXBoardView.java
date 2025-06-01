package com.cas.yutnorifx.view.fx;

import com.cas.yutnorifx.model.entity.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;
import java.util.ArrayList;

/**
 * JavaFX 기반 보드 뷰 (동적 크기 조정 지원)
 */
public class FXBoardView extends Pane {
    private final List<BoardNode> nodes;
    private final List<Player> players;
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final Color[] playerColors = {
            Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA
    };
    
    // 첫 번째 노드 (시작 노드) 참조
    private final BoardNode startNode;

    // 기본 크기 (스케일링 기준) - 더 컴팩트하게 조정
    private static final double BASE_SPACING = 180.0;  // 180 → 120으로 축소
    private static final double BASE_OFFSET = 20.0;    // 10 → 20으로 여백 증가
    private static final double BASE_WIDTH = BASE_SPACING * 8;   // 8 → 6으로 폭 축소
    private static final double BASE_HEIGHT = BASE_SPACING * 5;  // 5 → 4로 높이 축소

    public FXBoardView(List<BoardNode> nodes, List<Player> players) {
        this.nodes = nodes;
        this.players = players;
        this.canvas = new Canvas(BASE_WIDTH, BASE_HEIGHT);
        this.gc = canvas.getGraphicsContext2D();
        getChildren().add(canvas);
        
        // 시작 노드 찾기 (첫 번째 모서리 노드)
        this.startNode = nodes.stream()
                .filter(node -> node.getName().matches("Edge\\d+-0"))
                .findFirst()
                .orElse(null);
        
        // 크기 변경 리스너 등록
        this.widthProperty().addListener((obs, oldVal, newVal) -> {
            resizeCanvas();
            refresh();
        });
        
        this.heightProperty().addListener((obs, oldVal, newVal) -> {
            resizeCanvas();
            refresh();
        });
        
        refresh();
    }
    
    private void resizeCanvas() {
        double currentWidth = getWidth();
        double currentHeight = getHeight();
        
        if (currentWidth > 0 && currentHeight > 0) {
            canvas.setWidth(currentWidth);
            canvas.setHeight(currentHeight);
        }
    }

    public void refresh() {
        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();
        
        gc.clearRect(0, 0, canvasWidth, canvasHeight);
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvasWidth, canvasHeight);

        // 스케일 팩터 계산
        double scaleX = canvasWidth / BASE_WIDTH;
        double scaleY = canvasHeight / BASE_HEIGHT;
        double scale = Math.min(scaleX, scaleY); // 비율 유지
        
        double spacing = BASE_SPACING * scale;
        double offset = BASE_OFFSET * scale;

        // 노드 간 연결선 및 분기점 연결선 그리기
        for (BoardNode node : nodes) {
            List<BoardNode> nextNodes = node.getNextNodes();
            int size = nextNodes.size();
            double x1 = node.getX() * spacing + offset;
            double y1 = node.getY() * spacing + offset;

            // 분기점이거나 시작 노드인 경우 특별한 화살표 그리기
            if (size >= 2 || node.equals(startNode)) {
                for (int k = 1; k < size - 1; k++) {
                    BoardNode mid = nextNodes.get(k);
                    double mx = mid.getX() * spacing + offset;
                    double my = mid.getY() * spacing + offset;
                    gc.setStroke(Color.DARKGRAY);
                    gc.setLineWidth(1 * scale);
                    gc.strokeLine(x1, y1, mx, my);
                }

                int[] idx = new int[]{0, size - 1};
                for (int j : idx) {
                    if (j < 0 || j >= size) continue;
                    BoardNode next = nextNodes.get(j);
                    double x2 = next.getX() * spacing + offset;
                    double y2 = next.getY() * spacing + offset;

                    double dist = Math.hypot(x2 - x1, y2 - y1);
                    double ratio = (dist - spacing * 0.18) / dist; // 화살표 끝부분 여백도 비례
                    double tx = x1 + (x2 - x1) * ratio;
                    double ty = y1 + (y2 - y1) * ratio;

                    gc.setStroke(Color.DARKGRAY);
                    gc.setLineWidth(3 * scale);
                    gc.strokeLine(x1, y1, tx, ty);
                    drawArrowHead(x1, y1, tx, ty, spacing);

                    double midX = (2 * x1 + 3 * x2) / 5;
                    double midY = (2 * y1 + 3 * y2) / 5;
                    String label = next.getName().contains("Edge")
                            ? "1"
                            : next.getName().replace("ToCenter", "");
                    gc.setFont(Font.font("Arial", FontWeight.BOLD, spacing * 0.15)); // 라벨 폰트도 비례
                    gc.setFill(Color.RED);
                    gc.fillText(label, midX, midY);
                }
            } else if (size == 1) {
                BoardNode only = nextNodes.get(0);
                double x2 = only.getX() * spacing + offset;
                double y2 = only.getY() * spacing + offset;

                gc.setStroke(Color.DARKGRAY);
                gc.setLineWidth(1 * scale);
                gc.strokeLine(x1, y1, x2, y2);
            }
        }

        // 노드 (원) 그리기
        for (BoardNode node : nodes) {
            double x = node.getX() * spacing + offset;
            double y = node.getY() * spacing + offset;
            String name = node.getName();

            // 원 크기를 BASE_SPACING에 비례하도록 동적 계산
            double out = spacing * 0.47;     // 중요 노드 외곽원 (BASE_SPACING의 47%)
            double radius = spacing * 0.3;   // 일반 노드 원 (BASE_SPACING의 30%)
            // 중요한 노드들 (Center, 모서리 시작점, 모서리 끝점)은 특별한 원으로 표시
            boolean isImportantNode = name.equals("Center") || 
                                    name.matches("Edge\\d+-0") || 
                                    name.matches("Edge\\d+-5"); // 현재는 6개 노드(0~5)로 고정되어 있음
            
            if (isImportantNode) {
                double outX = x - out / 2;
                double outY = y - out / 2;
                gc.setFill(Color.WHITE);
                gc.fillOval(outX, outY, out, out);
                gc.setStroke(Color.BLACK);
                gc.strokeOval(outX, outY, out, out);

                double in = spacing * 0.3;   // 중요 노드 내부원 (BASE_SPACING의 30%)
                double inX = x - in / 2;
                double inY = y - in / 2;
                gc.setFill(Color.WHITE);
                gc.fillOval(inX, inY, in, in);
                gc.setStroke(Color.BLACK);
                gc.strokeOval(inX, inY, in, in);
            } else {
                double drawX = x - radius / 2;
                double drawY = y - radius / 2;
                gc.setFill(Color.WHITE);
                gc.fillOval(drawX, drawY, radius, radius);
                gc.setStroke(Color.BLACK);
                gc.strokeOval(drawX, drawY, radius, radius);
            }

            // 시작 노드 표시 (동적으로 찾은 시작 노드)
            if (node.equals(startNode)) {
                gc.setFont(Font.font("Arial", FontWeight.BOLD, spacing * 0.13)); // 폰트도 비례
                gc.setFill(Color.RED);
                double textX = x - spacing * 0.125;
                double textY = y - out/2;
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

            // 모든 대표 토큰들의 정보를 수집
            List<String> allIndices = new ArrayList<>();
            String ownerName = null;
            Color color = null;
            
            for (Token representativeToken : activeTokens) {
                Player owner = representativeToken.getOwner();
                
                // 첫 번째 토큰의 소유자로 색상 결정
                if (ownerName == null) {
                    ownerName = owner.getName();
                    int playerIndex = players.indexOf(owner);
                    color = playerColors[playerIndex % playerColors.length];
                }
                
                // 대표 토큰의 인덱스 추가
                allIndices.add(representativeToken.getName().split("-")[1]);
                
                // 업힌 토큰들의 인덱스도 추가
                for (Token stacked : representativeToken.getStackedTokens()) {
                    allIndices.add(stacked.getName().split("-")[1]);
                }
            }

            double cx = node.getX() * spacing + offset - spacing * 0.083;
            double cy = node.getY() * spacing + offset - spacing * 0.083;
            double tokenSize = spacing * 0.167; // 토큰 크기도 비례

            gc.setFill(color);
            gc.fillOval(cx, cy, tokenSize, tokenSize);
            gc.setStroke(Color.BLACK);
            gc.strokeOval(cx, cy, tokenSize, tokenSize);

            // 모든 토큰들의 이름을 표시
            String label = ownerName + " - " + String.join(",", allIndices);

            gc.setFont(Font.font("Arial", FontWeight.BOLD, spacing * 0.125)); // 토큰 라벨 폰트도 비례
            gc.fillText(label, cx - spacing * 0.042, cy - spacing * 0.042);
        }
    }

    private void drawArrowHead(double x1, double y1, double x2, double y2, double spacing) {
        double theta = Math.atan2(y2 - y1, x2 - x1);
        double rho = theta + Math.toRadians(30);
        double x = x2 - spacing * 0.067 * Math.cos(rho); // 화살표 크기도 비례
        double y = y2 - spacing * 0.067 * Math.sin(rho);
        gc.strokeLine(x2, y2, x, y);

        rho = theta - Math.toRadians(30);
        x = x2 - spacing * 0.067 * Math.cos(rho);
        y = y2 - spacing * 0.067 * Math.sin(rho);
        gc.strokeLine(x2, y2, x, y);
    }
} 