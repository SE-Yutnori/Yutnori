package com.cas.yutnoriswing.view;

import com.cas.yutnoriswing.model.BoardNode;
import com.cas.yutnoriswing.model.Player;
import com.cas.yutnoriswing.model.Token;
import com.cas.yutnoriswing.model.TokenState;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.List;

public class BoardView extends JPanel {
    private final List<BoardNode> nodes;
    private final List<Player> players;
    private final Color[] playerColors = {
            Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA
    };
    
    // 첫 번째 노드 (시작 노드)
    private final BoardNode startNode;

    //임의로 화면 비율 조정 가능
    private static final int SPACING = 120;
    private static final int OFFSET = 10;

    public BoardView(List<BoardNode> nodes, List<Player> players) {
        this.nodes = nodes;
        this.players = players;
        
        // 시작 노드 찾기 (Edge 0-0)
        this.startNode = nodes.stream()
                .filter(node -> node.getName().matches("Edge\\d+-0"))
                .findFirst()
                .orElse(null);
        
        // 정사각형 비율로 변경: 6x6 크기로 설정
        setPreferredSize(new Dimension(SPACING * 6, SPACING * 6));
        setBackground(Color.WHITE);
    }

    public void refresh() {
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 노드 간 연결선 및 분기점 연결선 그리기
        for (BoardNode node : nodes) {
            List<BoardNode> nextNodes = node.getNextNodes();
            int size = nextNodes.size();
            int x1 = (int) (node.getX() * SPACING + OFFSET);
            int y1 = (int) (node.getY() * SPACING + OFFSET);

            // 분기점이거나 시작 노드인 경우 화살표로 그리기
            if (size >= 2 || node.equals(startNode)) {
                for (int k = 1; k < size - 1; k++) {
                    BoardNode mid = nextNodes.get(k);
                    int mx = (int) (mid.getX() * SPACING + OFFSET);
                    int my = (int) (mid.getY() * SPACING + OFFSET);
                    g2d.setColor(Color.DARK_GRAY);
                    g2d.setStroke(new BasicStroke(1));
                    g2d.draw(new Line2D.Double(x1, y1, mx, my));
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

                    g2d.setColor(Color.DARK_GRAY);
                    g2d.setStroke(new BasicStroke(3));
                    g2d.draw(new Line2D.Double(x1, y1, tx, ty));
                    drawArrowHead(g2d, x1, y1, tx, ty);

                    //해당 비율 조정하면서 분기 방향 글자 원에 안 씹히게 조절 가능..
                    int midX = (2 * x1 + 4 * x2) / 6;
                    int midY = (2 * y1 + 3 * y2) / 5;
                    String label = next.getName().contains("Edge")
                            ? "1"
                            : next.getName().replace("ToCenter", "");
                    g2d.setFont(new Font("Arial", Font.BOLD, 18));
                    g2d.setColor(Color.RED);
                    g2d.drawString(label, midX, midY);
                }
            } else if (size == 1) {
                BoardNode only = nextNodes.get(0);
                int x2 = (int) (only.getX() * SPACING + OFFSET);
                int y2 = (int) (only.getY() * SPACING + OFFSET);

                g2d.setColor(Color.DARK_GRAY);
                g2d.setStroke(new BasicStroke(1));
                g2d.draw(new Line2D.Double(x1, y1, x2, y2));
            }
        }

        // 노드 (원 모양으로) 그리기
        for (BoardNode node : nodes) {
            int x = (int) (node.getX() * SPACING + OFFSET);
            int y = (int) (node.getY() * SPACING + OFFSET);

            int out = 56;
            int radius = 36;
            // 중요한 노드들 (Center, 모서리 시작점, 모서리 끝점)은 이중 원으로 표시
            boolean isImportantNode = node.getName().equals("Center") || 
                                    node.getName().matches("Edge\\d+-0") || 
                                    node.getName().matches("Edge\\d+-5");
            
            if (isImportantNode) {
                int outX = x - out / 2;
                int outY = y - out / 2;
                g2d.setColor(Color.WHITE);
                g2d.fill(new Ellipse2D.Double(outX, outY, out, out));
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(1));
                g2d.draw(new Ellipse2D.Double(outX, outY, out, out));

                int in = 36;
                int inX = x - in / 2;
                int inY = y - in / 2;
                g2d.setColor(Color.WHITE);
                g2d.fill(new Ellipse2D.Double(inX, inY, in, in));
                g2d.setColor(Color.BLACK);
                g2d.draw(new Ellipse2D.Double(inX, inY, in, in));
            } else {
                int drawX = x - radius / 2;
                int drawY = y - radius / 2;
                g2d.setColor(Color.WHITE);
                g2d.fill(new Ellipse2D.Double(drawX, drawY, radius, radius));
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(1));
                g2d.draw(new Ellipse2D.Double(drawX, drawY, radius, radius));
            }

            // 시작 노드는 이름 표시하기 (나머지도 아래 주석 풀면 이름 다 보임)
            if (node.equals(startNode)) {
                g2d.setFont(new Font("Arial", Font.BOLD, 16));
                g2d.setColor(Color.RED);
                int textX = x - 15;
                int textY = y - out/2;
                g2d.drawString("start", textX, textY);
            }
            
            // // 각 BoardNode의 이름 표시하는 코드들 (디버깅용) 추후 삭제 예정
            // g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            // g2d.setColor(Color.BLUE);
            // FontMetrics fm = g2d.getFontMetrics();
            // int nameX = x - fm.stringWidth(node.getName()) / 2; // 텍스트 중앙 정렬
            // int nameY = y + (isImportantNode ? out/2 + 15 : radius/2 + 15);
            // g2d.drawString(node.getName(), nameX, nameY);
            
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

            g2d.setColor(color);
            g2d.fill(new Ellipse2D.Double(cx, cy, 20, 20));
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(1));
            g2d.draw(new Ellipse2D.Double(cx, cy, 20, 20));

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
            
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            g2d.setColor(Color.BLACK);
            FontMetrics fm = g2d.getFontMetrics();
            int labelX = cx + 10 - fm.stringWidth(label) / 2;
            int labelY = cy + 35;
            g2d.drawString(label, labelX, labelY);
        }
    }

    private void drawArrowHead(Graphics2D g2d, int x1, int y1, int x2, int y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        
        int arrowLength = 10;
        double arrowAngle = Math.PI / 6;
        
        int x3 = (int) (x2 - arrowLength * Math.cos(angle - arrowAngle));
        int y3 = (int) (y2 - arrowLength * Math.sin(angle - arrowAngle));
        int x4 = (int) (x2 - arrowLength * Math.cos(angle + arrowAngle));
        int y4 = (int) (y2 - arrowLength * Math.sin(angle + arrowAngle));
        
        g2d.setColor(Color.DARK_GRAY);
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(new Line2D.Double(x2, y2, x3, y3));
        g2d.draw(new Line2D.Double(x2, y2, x4, y4));
    }
} 