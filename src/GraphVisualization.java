

package Project1;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;

// Main class
public class GraphVisualization {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Example adjacency matrix (5 nodes)
            int[][] adjacencyMatrix = {
                    {0, 1, 0, 1, 0, 0},
                    {1, 0, 1, 0, 0, 0},
                    {0, 1, 0, 0, 0, 0},
                    {1, 0, 0, 0, 1, 1},
                    {0, 0, 1, 0, 0, 0},
                    {0, 0, 1, 0, 0, 0}
            };

            new GraphFrame(adjacencyMatrix);
        });
    }
}

// Node class representing a graph vertex
class Node {
    private int id;
    private String label;
    private double x;
    private double y;
    private static final int RADIUS = 25;

    public Node(int id, double x, double y) {
        this.id = id;
        this.label = String.valueOf((char)('A' + id));
        this.x = x;
        this.y = y;
    }

    public int getId() { return id; }
    public String getLabel() { return label; }
    public double getX() { return x; }
    public double getY() { return y; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public int getRadius() { return RADIUS; }

    public boolean contains(Point p) {
        double dist = Math.sqrt(Math.pow(p.x - x, 2) + Math.pow(p.y - y, 2));
        return dist <= RADIUS;
    }
}

// Edge class representing a connection between nodes
class Edge {
    private Node start;
    private Node end;

    public Edge(Node start, Node end) {
        this.start = start;
        this.end = end;
    }

    public Node getStart() { return start; }
    public Node getEnd() { return end; }
}

// Graph class managing nodes and edges
class Graph {
    private java.util.List<Node> nodes;
    private java.util.List<Edge> edges;
    private int[][] adjacencyMatrix;

    public Graph(int[][] adjacencyMatrix) {
        this.adjacencyMatrix = adjacencyMatrix;
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
        initializeGraph();
    }

    private void initializeGraph() {
        int n = adjacencyMatrix.length;

        // Create nodes in circular layout
        double centerX = 400;
        double centerY = 300;
        double radius = 200;

        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n;
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            nodes.add(new Node(i, x, y));
        }

        // Create edges based on adjacency matrix
        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                if (adjacencyMatrix[i][j] != 0) {
                    edges.add(new Edge(nodes.get(i), nodes.get(j)));
                }
            }
        }
    }

    public java.util.List<Node> getNodes() { return nodes; }
    public java.util.List<Edge> getEdges() { return edges; }
    public int[][] getAdjacencyMatrix() { return adjacencyMatrix; }
}

// Panel for drawing the graph
class GraphPanel extends JPanel {
    private Graph graph;
    private Node draggedNode;
    private Point dragOffset;

    public GraphPanel(Graph graph) {
        this.graph = graph;
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.WHITE);

        // Mouse listeners for dragging nodes
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                for (Node node : graph.getNodes()) {
                    if (node.contains(e.getPoint())) {
                        draggedNode = node;
                        dragOffset = new Point(
                                (int)(e.getX() - node.getX()),
                                (int)(e.getY() - node.getY())
                        );
                        break;
                    }
                }
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                draggedNode = null;
            }
        });

        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                if (draggedNode != null) {
                    draggedNode.setX(e.getX() - dragOffset.x);
                    draggedNode.setY(e.getY() - dragOffset.y);
                    repaint();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw edges
        g2d.setColor(Color.GRAY);
        g2d.setStroke(new BasicStroke(2));
        for (Edge edge : graph.getEdges()) {
            Node start = edge.getStart();
            Node end = edge.getEnd();
            g2d.draw(new Line2D.Double(start.getX(), start.getY(), end.getX(), end.getY()));
        }

        // Draw nodes
        for (Node node : graph.getNodes()) {
            g2d.setColor(new Color(100, 150, 255));
            g2d.fill(new Ellipse2D.Double(
                    node.getX() - node.getRadius(),
                    node.getY() - node.getRadius(),
                    node.getRadius() * 2,
                    node.getRadius() * 2
            ));

            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            g2d.draw(new Ellipse2D.Double(
                    node.getX() - node.getRadius(),
                    node.getY() - node.getRadius(),
                    node.getRadius() * 2,
                    node.getRadius() * 2
            ));

            // Draw node label
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            String label = node.getLabel();
            FontMetrics fm = g2d.getFontMetrics();
            int textX = (int)(node.getX() - fm.stringWidth(label) / 2);
            int textY = (int)(node.getY() + fm.getAscent() / 2);
            g2d.drawString(label, textX, textY);
        }
    }
}

// Main frame
class GraphFrame extends JFrame {
    private Graph graph;
    private GraphPanel graphPanel;

    public GraphFrame(int[][] adjacencyMatrix) {
        this.graph = new Graph(adjacencyMatrix);
        setupUI();
    }

    private void setupUI() {
        setTitle("Graph Visualization");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        graphPanel = new GraphPanel(graph);
        add(graphPanel, BorderLayout.CENTER);

        // Info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel infoLabel = new JLabel("Nodes: " + graph.getNodes().size() +
                " | Edges: " + graph.getEdges().size() +
                " | Drag nodes to reposition");
        infoPanel.add(infoLabel);
        add(infoPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}