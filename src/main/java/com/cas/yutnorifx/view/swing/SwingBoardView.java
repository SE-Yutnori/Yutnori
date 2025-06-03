package com.cas.yutnorifx.view.swing;

import com.cas.yutnorifx.model.entity.*;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.List;
import java.util.ArrayList;


public class SwingBoardView extends JPanel {
    private final List<BoardNode> nodes;
    private final List<Player> players;
    private final Color[] playerColors = {
            Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA
    };
    
    private final BoardNode startNode;

    private static final double BASE_SPACING = 120.0;  // 180 → 120으로 축소
    private static final double BASE_OFFSET = 20.0;    // 10 → 20으로 여백 증가
    private static final double BASE_WIDTH = BASE_SPACING * 6;   // 8 → 6으로 폭 축소
    private static final double BASE_HEIGHT = BASE_SPACING * 4;  // 5 → 4로 높이 축소

    public SwingBoardView(List<BoardNode> nodes, List<Player> players) {
        this.nodes = nodes;
        this.players = players;
        
        this.startNode = nodes.stream()
                .filter(node -> node.getName().matches("Edge\\d+-0"))
                .findFirst()
                .orElse(null);
        
        setPreferredSize(new Dimension((int)BASE_WIDTH, (int)BASE_HEIGHT));
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double currentWidth = getWidth();
        double currentHeight = getHeight();
        double scaleX = currentWidth / BASE_WIDTH;
        double scaleY = currentHeight / BASE_HEIGHT;
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
                    g2d.setColor(Color.DARK_GRAY);
                    g2d.setStroke(new BasicStroke((float)(1 * scale)));
                    g2d.drawLine((int)x1, (int)y1, (int)mx, (int)my);
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

                    g2d.setColor(Color.DARK_GRAY);
                    g2d.setStroke(new BasicStroke((float)(3 * scale)));
                    g2d.drawLine((int)x1, (int)y1, (int)tx, (int)ty);
                    drawArrowHead(g2d, x1, y1, tx, ty, spacing);

                    double midX = (2 * x1 + 3 * x2) / 5;
                    double midY = (2 * y1 + 3 * y2) / 5;
                    String label = next.getName().contains("Edge")
                            ? "1"
                            : next.getName().replace("ToCenter", "");
                    g2d.setFont(new Font("Arial", Font.BOLD, Math.max(1, (int)(spacing * 0.15))));
                    g2d.setColor(Color.RED);
                    g2d.drawString(label, (int)midX, (int)midY);
                }
            } else if (size == 1) {
                BoardNode only = nextNodes.get(0);
                double x2 = only.getX() * spacing + offset;
                double y2 = only.getY() * spacing + offset;

                g2d.setColor(Color.DARK_GRAY);
                g2d.setStroke(new BasicStroke((float)(1 * scale)));
                g2d.drawLine((int)x1, (int)y1, (int)x2, (int)y2);
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
                g2d.setColor(Color.WHITE);
                g2d.fillOval((int)outX, (int)outY, (int)out, (int)out);
                g2d.setColor(Color.BLACK);
                g2d.drawOval((int)outX, (int)outY, (int)out, (int)out);

                double in = spacing * 0.3;
                double inX = x - in / 2;
                double inY = y - in / 2;
                g2d.setColor(Color.WHITE);
                g2d.fillOval((int)inX, (int)inY, (int)in, (int)in);
                g2d.setColor(Color.BLACK);
                g2d.drawOval((int)inX, (int)inY, (int)in, (int)in);
            } else {
                double drawX = x - radius / 2;
                double drawY = y - radius / 2;
                g2d.setColor(Color.WHITE);
                g2d.fillOval((int)drawX, (int)drawY, (int)radius, (int)radius);
                g2d.setColor(Color.BLACK);
                g2d.drawOval((int)drawX, (int)drawY, (int)radius, (int)radius);
            }

            if (node.equals(startNode)) {
                g2d.setFont(new Font("Arial", Font.BOLD, Math.max(1, (int)(spacing * 0.13))));
                g2d.setColor(Color.RED);
                double textX = x - spacing * 0.125;
                double textY = y - out/2;
                g2d.drawString("start", (int)textX, (int)textY);
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

            g2d.setColor(color);
            g2d.fillOval((int)cx, (int)cy, (int)tokenSize, (int)tokenSize);
            g2d.setColor(Color.BLACK);
            g2d.drawOval((int)cx, (int)cy, (int)tokenSize, (int)tokenSize);

            String label = ownerName + " - " + String.join(",", allIndices);

            g2d.setFont(new Font("Arial", Font.BOLD, Math.max(1, (int)(spacing * 0.125))));
            g2d.drawString(label, (int)(cx - spacing * 0.042), (int)(cy - spacing * 0.042));
        }
    }

    private void drawArrowHead(Graphics2D g2d, double x1, double y1, double x2, double y2, double spacing) {
        double theta = Math.atan2(y2 - y1, x2 - x1);
        double rho = theta + Math.toRadians(30);
        double x = x2 - spacing * 0.067 * Math.cos(rho);
        double y = y2 - spacing * 0.067 * Math.sin(rho);
        g2d.drawLine((int)x2, (int)y2, (int)x, (int)y);

        rho = theta - Math.toRadians(30);
        x = x2 - spacing * 0.067 * Math.cos(rho);
        y = y2 - spacing * 0.067 * Math.sin(rho);
        g2d.drawLine((int)x2, (int)y2, (int)x, (int)y);
    }

    public void refresh() {
        repaint();
    }
} 