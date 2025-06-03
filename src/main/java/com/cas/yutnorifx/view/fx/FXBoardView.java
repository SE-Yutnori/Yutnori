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

public class FXBoardView extends Pane {
    private final List<BoardNode> nodes;
    private final List<Player> players;
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final Color[] playerColors = {
            Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA
    };
    
    private final BoardNode startNode;

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
        
        this.startNode = nodes.stream()
                .filter(node -> node.getName().matches("Edge\\d+-0"))
                .findFirst()
                .orElse(null);
        
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

        double scaleX = canvasWidth / BASE_WIDTH;
        double scaleY = canvasHeight / BASE_HEIGHT;
        double scale = Math.min(scaleX, scaleY); // 비율 유지
        
        double spacing = BASE_SPACING * scale;
        double offset = BASE_OFFSET * scale;

        for (BoardNode node : nodes) {
            List<BoardNode> nextNodes = node.getNextNodes();
            int size = nextNodes.size();
            double x1 = node.getX() * spacing + offset;
            double y1 = node.getY() * spacing + offset;

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
                    double ratio = (dist - spacing * 0.18) / dist;
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

        for (BoardNode node : nodes) {
            double x = node.getX() * spacing + offset;
            double y = node.getY() * spacing + offset;
            String name = node.getName();

            double out = spacing * 0.47;
            double radius = spacing * 0.3;
            boolean isImportantNode = name.equals("Center") ||
                                    name.matches("Edge\\d+-0") || 
                                    name.matches("Edge\\d+-5");
            
            if (isImportantNode) {
                double outX = x - out / 2;
                double outY = y - out / 2;
                gc.setFill(Color.WHITE);
                gc.fillOval(outX, outY, out, out);
                gc.setStroke(Color.BLACK);
                gc.strokeOval(outX, outY, out, out);

                double in = spacing * 0.3;
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

            if (node.equals(startNode)) {
                gc.setFont(Font.font("Arial", FontWeight.BOLD, spacing * 0.13));
                gc.setFill(Color.RED);
                double textX = x - spacing * 0.125;
                double textY = y - out/2;
                gc.fillText("start", textX, textY);
            }
        }

        for (BoardNode node : nodes) {
            List<Token> tokens = node.getTokens();
            List<Token> activeTokens = tokens.stream()
                    .filter(t -> t.getState() == TokenState.ACTIVE)
                    .toList();

            if (activeTokens.isEmpty()) continue;

            List<String> allIndices = new ArrayList<>();
            String ownerName = null;
            Color color = null;
            
            for (Token representativeToken : activeTokens) {
                Player owner = representativeToken.getOwner();
                
                if (ownerName == null) {
                    ownerName = owner.getName();
                    int playerIndex = players.indexOf(owner);
                    color = playerColors[playerIndex % playerColors.length];
                }
                
                allIndices.add(representativeToken.getName().split("-")[1]);
                
                for (Token stacked : representativeToken.getStackedTokens()) {
                    allIndices.add(stacked.getName().split("-")[1]);
                }
            }

            double cx = node.getX() * spacing + offset - spacing * 0.083;
            double cy = node.getY() * spacing + offset - spacing * 0.083;
            double tokenSize = spacing * 0.167;

            gc.setFill(color);
            gc.fillOval(cx, cy, tokenSize, tokenSize);
            gc.setStroke(Color.BLACK);
            gc.strokeOval(cx, cy, tokenSize, tokenSize);

            String label = ownerName + " - " + String.join(",", allIndices);

            gc.setFont(Font.font("Arial", FontWeight.BOLD, spacing * 0.125));
            gc.fillText(label, cx - spacing * 0.042, cy - spacing * 0.042);
        }
    }

    private void drawArrowHead(double x1, double y1, double x2, double y2, double spacing) {
        double theta = Math.atan2(y2 - y1, x2 - x1);
        double rho = theta + Math.toRadians(30);
        double x = x2 - spacing * 0.067 * Math.cos(rho);
        double y = y2 - spacing * 0.067 * Math.sin(rho);
        gc.strokeLine(x2, y2, x, y);

        rho = theta - Math.toRadians(30);
        x = x2 - spacing * 0.067 * Math.cos(rho);
        y = y2 - spacing * 0.067 * Math.sin(rho);
        gc.strokeLine(x2, y2, x, y);
    }
} 