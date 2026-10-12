package com.banking.dashboard;

import com.banking.database.AccountRepository;
import com.banking.database.DatabaseManager;
import com.banking.integration.PipelineResult;
import com.banking.integration.SecureBankingPipeline;
import com.banking.integration.SecureBankingRequest;
import com.banking.model.Account;
import com.banking.network.NetworkNode;
import com.banking.network.NodeType;
import com.banking.model.TransactionType;
import com.banking.network.*;
import com.banking.util.LoggerUtil;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Professional JavaFX Dashboard Application for the Secure Banking Network.
 * Provides real-time visual network topology monitoring, Dijkstra & Distance Vector routing visualization,
 * AES-GCM security metrics, CRC error detection, sliding window reliability status, real TCP socket monitoring,
 * live transaction processing, and database balance reflection.
 * Responsive design tailored for standard laptop screens (1366x768) and resizable displays.
 */
public class DashboardApplication extends Application {

    private DatabaseManager dbManager;
    private AccountRepository accountRepo;
    private NetworkTopology topology;
    private DijkstraRouter dijkstraRouter;
    private DistanceVectorRouter dvRouter;
    private SecureBankingPipeline pipeline;

    private final ExecutorService bgExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "dashboard-bg-worker");
        t.setDaemon(true);
        return t;
    });

    private Consumer<String> logListener;

    // UI Controls
    private Label acc1BalanceLabel;
    private Label acc2BalanceLabel;
    private TextField sourceAccField;
    private TextField destAccField;
    private TextField amountField;
    private Label routingSummaryLabel;
    private Label dvSummaryLabel;
    private TextArea activityLogArea;
    private Button btnProcess;
    private Button btnResetBalances;

    // Stage Status Checkmarks
    private final Map<String, Label> stageCheckmarkLabels = new LinkedHashMap<>();

    // Topology Canvas Map (NodeId -> NodeVisualComponent)
    private final Map<String, NodeVisual> nodeVisualMap = new LinkedHashMap<>();
    private final List<LinkVisual> linkVisualList = new ArrayList<>();
    private Pane topologyPane;
    private StackPane topologyContainer;

    private List<String> activePathNodeIds = new ArrayList<>();

    private static final double TOPOLOGY_BASE_WIDTH = 760.0;
    private static final double TOPOLOGY_BASE_HEIGHT = 420.0;

    private static class NodePos {
        double x, y;
        NodePos(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    private static class NodeVisual {
        NetworkNode node;
        double x, y;
        StackPane visualGroup;
        ShapeVisual shape;
        Label nameLabel;
        Label ipLabel;
        Label statusLabel;
        Color defaultStrokeColor;
        double defaultStrokeWidth;

        NodeVisual(NetworkNode node, double x, double y, StackPane visualGroup, ShapeVisual shape,
                   Label nameLabel, Label ipLabel, Label statusLabel,
                   Color defaultStrokeColor, double defaultStrokeWidth) {
            this.node = node;
            this.x = x;
            this.y = y;
            this.visualGroup = visualGroup;
            this.shape = shape;
            this.nameLabel = nameLabel;
            this.ipLabel = ipLabel;
            this.statusLabel = statusLabel;
            this.defaultStrokeColor = defaultStrokeColor;
            this.defaultStrokeWidth = defaultStrokeWidth;
        }
    }

    private static class ShapeVisual {
        javafx.scene.shape.Shape shapeComponent;
        ShapeVisual(javafx.scene.shape.Shape shapeComponent) {
            this.shapeComponent = shapeComponent;
        }
    }

    private static class LinkVisual {
        NetworkLink link;
        Line lineComponent;
        Label costLabel;
        LinkVisual(NetworkLink link, Line lineComponent, Label costLabel) {
            this.link = link;
            this.lineComponent = lineComponent;
            this.costLabel = costLabel;
        }
    }

    @Override
    public void init() throws Exception {
        // Initialize Database & Services
        dbManager = new DatabaseManager();
        dbManager.initializeSchema();
        dbManager.seedSampleData();
        accountRepo = new AccountRepository(dbManager);
        topology = BankingTopologyBuilder.createSampleTopology();
        dijkstraRouter = new DijkstraRouter();
        dvRouter = new DistanceVectorRouter();
        pipeline = new SecureBankingPipeline(dbManager, topology);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("SECURE BANKING NETWORK — Professional Monitoring Dashboard");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0f172a;"); // Dark slate background

        // TOP: Header Bar
        root.setTop(createHeaderBar());

        // CENTER: Network Topology & Routing Analysis
        root.setCenter(createCenterTopologyPanel());

        // RIGHT: Security, Reliability, Transaction & Stage Control Sidebar
        root.setRight(createSidebarControlPanel());

        // BOTTOM: System Log Feed
        root.setBottom(createActivityLogPanel());

        // Attach LoggerUtil listener for live feed
        logListener = log -> Platform.runLater(() -> {
            if (activityLogArea != null) {
                activityLogArea.appendText(log + "\n");
                activityLogArea.positionCaret(activityLogArea.getLength());
                activityLogArea.setScrollTop(Double.MAX_VALUE);
            }
        });
        LoggerUtil.addListener(logListener);

        // Safe 1200x700 default dimensions perfectly fit 1366x768 screens
        Scene scene = new Scene(root, 1200, 700);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(960);
        primaryStage.setMinHeight(580);
        primaryStage.setResizable(true);

        // Ensure dashboard opens centered on screen
        primaryStage.centerOnScreen();
        primaryStage.show();
        primaryStage.centerOnScreen();

        // Perform initial topology scale adjustment after layout and highlight shortest route
        Platform.runLater(() -> {
            if (topologyContainer != null) {
                rescaleTopology(topologyContainer.getWidth(), topologyContainer.getHeight());
            }
            handleFindShortestRoute();
        });

        LoggerUtil.info("Secure Banking Dashboard initialized successfully.");
        updateAccountBalances();
    }

    // ==========================================
    // TOP HEADER BAR
    // ==========================================
    private VBox createHeaderBar() {
        VBox header = new VBox(6);
        header.setPadding(new Insets(8, 16, 8, 16));
        header.setStyle("-fx-background-color: #1e293b; -fx-border-color: #334155; -fx-border-width: 0 0 1 0;");

        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(1);
        Label title = new Label("SECURE BANKING NETWORK");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#38bdf8")); // Bright Cyan

        Label subtitle = new Label("Secure & Reliable Banking Communication Monitoring System");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        subtitle.setTextFill(Color.web("#94a3b8"));
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Status Badges
        HBox statusBadges = new HBox(8);
        statusBadges.setAlignment(Pos.CENTER_RIGHT);
        statusBadges.getChildren().addAll(
                createBadge("SYSTEM", "ONLINE", "#10b981"),
                createBadge("TCP", "READY", "#06b6d4"),
                createBadge("DATABASE", "CONNECTED", "#10b981"),
                createBadge("SECURITY", "ACTIVE", "#8b5cf6")
        );

        topRow.getChildren().addAll(titleBox, spacer, statusBadges);
        header.getChildren().add(topRow);
        return header;
    }

    private HBox createBadge(String category, String status, String colorHex) {
        HBox badge = new HBox(5);
        badge.setAlignment(Pos.CENTER);
        badge.setPadding(new Insets(3, 8, 3, 8));
        badge.setStyle("-fx-background-color: #0f172a; -fx-border-color: " + colorHex + "; -fx-border-radius: 10; -fx-background-radius: 10;");

        Circle dot = new Circle(3.5, Color.web(colorHex));
        Label label = new Label(category + ": " + status);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        label.setTextFill(Color.web("#f8fafc"));

        badge.getChildren().addAll(dot, label);
        return badge;
    }

    // ==========================================
    // CENTER: NETWORK TOPOLOGY & ROUTING
    // ==========================================
    private VBox createCenterTopologyPanel() {
        VBox center = new VBox(8);
        center.setPadding(new Insets(10, 12, 10, 12));
        center.setStyle("-fx-background-color: #0f172a;");

        // Action Toolbar
        HBox actionToolbar = new HBox(10);
        actionToolbar.setAlignment(Pos.CENTER_LEFT);

        Button btnFindRoute = new Button("⚡ FIND SHORTEST ROUTE (DIJKSTRA)");
        btnFindRoute.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 6 12; -fx-font-size: 11px; -fx-cursor: hand;");
        btnFindRoute.setOnAction(e -> handleFindShortestRoute());

        Button btnResetRoute = new Button("↺ CLEAR ROUTE");
        btnResetRoute.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 6 10; -fx-font-size: 11px; -fx-cursor: hand;");
        btnResetRoute.setOnAction(e -> clearRouteHighlight());

        routingSummaryLabel = new Label("Selected Route: None (Click 'Find Shortest Route' to compute path)");
        routingSummaryLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        routingSummaryLabel.setTextFill(Color.web("#94a3b8"));

        actionToolbar.getChildren().addAll(btnFindRoute, btnResetRoute, routingSummaryLabel);

        // Topology Canvas
        topologyPane = new Pane();
        topologyPane.setPrefSize(TOPOLOGY_BASE_WIDTH, TOPOLOGY_BASE_HEIGHT);
        topologyPane.setMinSize(TOPOLOGY_BASE_WIDTH, TOPOLOGY_BASE_HEIGHT);
        topologyPane.setMaxSize(TOPOLOGY_BASE_WIDTH, TOPOLOGY_BASE_HEIGHT);

        buildTopologyVisual();

        // Responsive scalable container
        topologyContainer = new StackPane(topologyPane);
        topologyContainer.setAlignment(Pos.CENTER);
        topologyContainer.setStyle("-fx-background-color: #1e293b; -fx-border-color: #334155; -fx-border-radius: 8; -fx-background-radius: 8;");
        VBox.setVgrow(topologyContainer, Priority.ALWAYS);

        // Dynamic scale listeners for responsive resizing without clipping
        topologyContainer.widthProperty().addListener((obs, oldVal, newVal) -> rescaleTopology(newVal.doubleValue(), topologyContainer.getHeight()));
        topologyContainer.heightProperty().addListener((obs, oldVal, newVal) -> rescaleTopology(topologyContainer.getWidth(), newVal.doubleValue()));

        // Routing Engine Metrics Box
        HBox metricsBox = new HBox(16);
        metricsBox.setPadding(new Insets(8, 14, 8, 14));
        metricsBox.setStyle("-fx-background-color: #1e293b; -fx-border-color: #334155; -fx-border-radius: 8; -fx-background-radius: 8;");
        metricsBox.setAlignment(Pos.CENTER_LEFT);

        dvSummaryLabel = new Label("Routing Comparison | Link-State (Dijkstra): Ready | Distance Vector (Bellman-Ford): Ready");
        dvSummaryLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        dvSummaryLabel.setTextFill(Color.web("#38bdf8"));
        dvSummaryLabel.setWrapText(true);
        metricsBox.getChildren().add(dvSummaryLabel);

        center.getChildren().addAll(actionToolbar, topologyContainer, metricsBox);
        return center;
    }

    private void rescaleTopology(double containerWidth, double containerHeight) {
        if (containerWidth <= 20 || containerHeight <= 20 || topologyPane == null) return;
        double scaleX = (containerWidth - 20) / TOPOLOGY_BASE_WIDTH;
        double scaleY = (containerHeight - 20) / TOPOLOGY_BASE_HEIGHT;
        double scale = Math.min(scaleX, scaleY);
        // Clamp scale so topology remains sharp, legible, and responsive across displays
        scale = Math.max(0.50, Math.min(scale, 1.50));
        topologyPane.setScaleX(scale);
        topologyPane.setScaleY(scale);
    }

    private void buildTopologyVisual() {
        topologyPane.getChildren().clear();
        nodeVisualMap.clear();
        linkVisualList.clear();

        Map<String, NodePos> positions = new HashMap<>();
        // Perfectly balanced planar layout keeping all 10 nodes and links completely visible
        positions.put("NODE-B1", new NodePos(85, 95));
        positions.put("ROUTER-B1", new NodePos(275, 95));
        positions.put("NODE-ATM", new NodePos(85, 210));
        positions.put("ROUTER-B2", new NodePos(275, 350));
        positions.put("NODE-B2", new NodePos(85, 350));
        positions.put("ROUTER-HQ", new NodePos(485, 210));
        positions.put("NODE-HQ", new NodePos(485, 75));
        positions.put("NODE-CS", new NodePos(485, 350));
        positions.put("NODE-MGMT", new NodePos(675, 75));
        positions.put("NODE-DB", new NodePos(675, 210));

        // 1. Draw Links
        Set<String> drawnLinkKeys = new HashSet<>();
        for (NetworkLink link : topology.getLinks()) {
            String srcId = link.getSource().getNodeId();
            String destId = link.getDestination().getNodeId();
            String linkKey = srcId.compareTo(destId) < 0 ? srcId + "--" + destId : destId + "--" + srcId;

            if (drawnLinkKeys.contains(linkKey)) continue;
            drawnLinkKeys.add(linkKey);

            NodePos p1 = positions.get(srcId);
            NodePos p2 = positions.get(destId);
            if (p1 == null || p2 == null) continue;

            Line line = new Line(p1.x, p1.y, p2.x, p2.y);
            line.setStroke(Color.web("#64748b"));
            line.setStrokeWidth(2.0);
            line.setOpacity(0.90);

            double midX = (p1.x + p2.x) / 2;
            double midY = (p1.y + p2.y) / 2;
            Label costLabel = new Label("Cost: " + String.format("%.1f", link.getCost()));
            costLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
            costLabel.setTextFill(Color.web("#94a3b8"));
            costLabel.setStyle("-fx-background-color: #0f172a; -fx-padding: 2 6; -fx-background-radius: 4; -fx-border-color: #334155; -fx-border-radius: 4; -fx-border-width: 1.0;");
            costLabel.setPrefWidth(60);
            costLabel.setMinWidth(60);
            costLabel.setMaxWidth(60);
            costLabel.setAlignment(Pos.CENTER);
            costLabel.setLayoutX(midX - 30);
            costLabel.setLayoutY(midY - 10);

            topologyPane.getChildren().addAll(line, costLabel);
            linkVisualList.add(new LinkVisual(link, line, costLabel));
        }

        // 2. Draw Nodes
        for (NetworkNode node : topology.getNodes()) {
            NodePos pos = positions.get(node.getNodeId());
            if (pos == null) continue;

            StackPane nodeGroup = new StackPane();
            nodeGroup.setLayoutX(pos.x - 45);
            nodeGroup.setLayoutY(pos.y - 30);
            nodeGroup.setPrefSize(90, 60);

            javafx.scene.shape.Shape shapeComp;
            Color defaultStroke;
            double defaultStrokeWidth;

            if (node.getNodeType() == NodeType.ROUTER) {
                Circle circle = new Circle(28);
                circle.setFill(Color.web("#1e293b"));
                circle.setStroke(Color.web("#06b6d4"));
                circle.setStrokeWidth(2.5);
                shapeComp = circle;
                defaultStroke = Color.web("#06b6d4");
                defaultStrokeWidth = 2.5;
            } else if (node.getNodeType() == NodeType.SERVER) {
                Rectangle rect = new Rectangle(84, 52, Color.web("#312e81"));
                rect.setArcWidth(10);
                rect.setArcHeight(10);
                rect.setStroke(Color.web("#8b5cf6"));
                rect.setStrokeWidth(2.0);
                shapeComp = rect;
                defaultStroke = Color.web("#8b5cf6");
                defaultStrokeWidth = 2.0;
            } else if (node.getNodeType() == NodeType.ATM) {
                Rectangle rect = new Rectangle(78, 50, Color.web("#0891b2"));
                rect.setArcWidth(8);
                rect.setArcHeight(8);
                rect.setStroke(Color.web("#38bdf8"));
                rect.setStrokeWidth(2.0);
                shapeComp = rect;
                defaultStroke = Color.web("#38bdf8");
                defaultStrokeWidth = 2.0;
            } else {
                Rectangle rect = new Rectangle(78, 50, Color.web("#334155"));
                rect.setArcWidth(8);
                rect.setArcHeight(8);
                rect.setStroke(Color.web("#94a3b8"));
                rect.setStrokeWidth(2.0);
                shapeComp = rect;
                defaultStroke = Color.web("#94a3b8");
                defaultStrokeWidth = 2.0;
            }

            VBox labelBox = new VBox(1);
            labelBox.setAlignment(Pos.CENTER);
            Label nameL = new Label(node.getNodeName());
            nameL.setFont(Font.font("Segoe UI", FontWeight.BOLD, 9.5));
            nameL.setTextFill(Color.web("#f8fafc"));

            Label ipL = new Label(node.getIpAddress().getDottedDecimal());
            ipL.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 9));
            ipL.setTextFill(Color.web("#cbd5e1"));

            Label statusL = new Label("● ONLINE");
            statusL.setFont(Font.font("Segoe UI", FontWeight.BOLD, 8));
            statusL.setTextFill(Color.web("#10b981"));

            labelBox.getChildren().addAll(nameL, ipL, statusL);
            nodeGroup.getChildren().addAll(shapeComp, labelBox);

            topologyPane.getChildren().add(nodeGroup);
            nodeVisualMap.put(node.getNodeId(), new NodeVisual(node, pos.x, pos.y, nodeGroup, new ShapeVisual(shapeComp),
                    nameL, ipL, statusL, defaultStroke, defaultStrokeWidth));
        }
    }

    private void handleFindShortestRoute() {
        NetworkNode src = topology.getNode("NODE-B1");
        NetworkNode dest = topology.getNode("NODE-DB");

        DijkstraResult djResult = dijkstraRouter.findShortestPath(topology, src, dest);
        DistanceVectorResult dvResult = dvRouter.computeRouting(topology, src, dest);

        activePathNodeIds.clear();
        for (NetworkNode n : djResult.getPath()) {
            activePathNodeIds.add(n.getNodeId());
        }

        // Highlight visual route
        highlightRoute(activePathNodeIds);

        String pathStr = djResult.getFormattedPath();
        double cost = djResult.getTotalCost();
        String nextHopStr = djResult.getNextHop() != null ? djResult.getNextHop().getNodeName() : "DIRECT";

        routingSummaryLabel.setText(String.format("Shortest Route: %s | Cost: %.1f | Next Hop: %s", pathStr, cost, nextHopStr));
        routingSummaryLabel.setTextFill(Color.web("#38bdf8"));

        boolean match = Math.abs(djResult.getTotalCost() - dvResult.getTotalCost()) < 1e-6;
        dvSummaryLabel.setText(String.format("Routing Status | Dijkstra Cost: %.1f | Distance Vector Cost: %.1f | Convergence: %s | RESULT: %s",
                djResult.getTotalCost(), dvResult.getTotalCost(),
                dvResult.isConverged() ? "SUCCESS (" + dvResult.getIterations() + " iterations)" : "FAILED",
                match ? "ROUTES MATCH ✓" : "MISMATCH ✗"));
        dvSummaryLabel.setTextFill(match ? Color.web("#10b981") : Color.web("#ef4444"));

        LoggerUtil.info("Shortest route computed: " + pathStr + " (Cost: " + cost + ")");
    }

    private void highlightRoute(List<String> pathNodeIds) {
        // Highlight nodes along the path
        for (NodeVisual nv : nodeVisualMap.values()) {
            boolean inPath = pathNodeIds.contains(nv.node.getNodeId());
            if (inPath) {
                nv.shape.shapeComponent.setStroke(Color.web("#10b981"));
                nv.shape.shapeComponent.setStrokeWidth(3.5);
                nv.statusLabel.setText("● ACTIVE ROUTE");
                nv.statusLabel.setTextFill(Color.web("#10b981"));
            } else {
                nv.shape.shapeComponent.setStroke(nv.defaultStrokeColor);
                nv.shape.shapeComponent.setStrokeWidth(nv.defaultStrokeWidth);
                nv.statusLabel.setText("● ONLINE");
                nv.statusLabel.setTextFill(Color.web("#10b981"));
            }
        }

        // Highlight link connections along the path
        for (LinkVisual lv : linkVisualList) {
            String srcId = lv.link.getSource().getNodeId();
            String destId = lv.link.getDestination().getNodeId();

            boolean isPathLink = false;
            for (int i = 0; i < pathNodeIds.size() - 1; i++) {
                String u = pathNodeIds.get(i);
                String v = pathNodeIds.get(i + 1);
                if ((srcId.equals(u) && destId.equals(v)) || (srcId.equals(v) && destId.equals(u))) {
                    isPathLink = true;
                    break;
                }
            }

            if (isPathLink) {
                lv.lineComponent.setStroke(Color.web("#10b981"));
                lv.lineComponent.setStrokeWidth(4.5);
                lv.lineComponent.setOpacity(1.0);
                lv.costLabel.setTextFill(Color.web("#34d399"));
                lv.costLabel.setStyle("-fx-background-color: #064e3b; -fx-padding: 2 6; -fx-background-radius: 4; -fx-border-color: #10b981; -fx-border-radius: 4; -fx-border-width: 1.5;");
                lv.lineComponent.toFront();
                lv.costLabel.toFront();
            } else {
                lv.lineComponent.setStroke(Color.web("#64748b"));
                lv.lineComponent.setStrokeWidth(2.0);
                lv.lineComponent.setOpacity(0.90);
                lv.costLabel.setTextFill(Color.web("#94a3b8"));
                lv.costLabel.setStyle("-fx-background-color: #0f172a; -fx-padding: 2 6; -fx-background-radius: 4; -fx-border-color: #334155; -fx-border-radius: 4; -fx-border-width: 1.0;");
            }
        }

        // Ensure all node cards stay in front of lines
        for (NodeVisual nv : nodeVisualMap.values()) {
            nv.visualGroup.toFront();
        }
    }

    private void clearRouteHighlight() {
        activePathNodeIds.clear();
        for (NodeVisual nv : nodeVisualMap.values()) {
            nv.shape.shapeComponent.setStroke(nv.defaultStrokeColor);
            nv.shape.shapeComponent.setStrokeWidth(nv.defaultStrokeWidth);
            nv.statusLabel.setText("● ONLINE");
            nv.statusLabel.setTextFill(Color.web("#10b981"));
        }
        for (LinkVisual lv : linkVisualList) {
            lv.lineComponent.setStroke(Color.web("#64748b"));
            lv.lineComponent.setStrokeWidth(2.0);
            lv.lineComponent.setOpacity(0.90);
            lv.costLabel.setTextFill(Color.web("#94a3b8"));
            lv.costLabel.setStyle("-fx-background-color: #0f172a; -fx-padding: 2 6; -fx-background-radius: 4; -fx-border-color: #334155; -fx-border-radius: 4; -fx-border-width: 1.0;");
        }
        for (NodeVisual nv : nodeVisualMap.values()) {
            nv.visualGroup.toFront();
        }
        routingSummaryLabel.setText("Selected Route: None");
        routingSummaryLabel.setTextFill(Color.web("#94a3b8"));
    }

    // ==========================================
    // RIGHT SIDEBAR: CONTROLS & STATUS CARDS
    // ==========================================
    private ScrollPane createSidebarControlPanel() {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(10, 12, 10, 12));
        sidebar.setPrefWidth(370);
        sidebar.setMinWidth(340);
        sidebar.setStyle("-fx-background-color: #0f172a;");

        // 1. Account Summary & Transaction Card
        sidebar.getChildren().add(createTransactionControlCard());

        // 2. Pipeline Execution Checklist Card
        sidebar.getChildren().add(createPipelineStageCard());

        // 3. Security & Encryption Card
        sidebar.getChildren().add(createSecurityStatusCard());

        // 4. Reliable Transmission & TCP Card
        sidebar.getChildren().add(createReliabilityTcpCard());

        ScrollPane scroll = new ScrollPane(sidebar);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background-color: #0f172a; -fx-background: #0f172a; -fx-border-color: #334155; -fx-border-width: 0 0 0 1;");
        return scroll;
    }

    private VBox createTransactionControlCard() {
        VBox card = createCardContainer("BANKING TRANSACTION & BALANCE");

        // Account Balances Display
        GridPane balancesGrid = new GridPane();
        balancesGrid.setHgap(10);
        balancesGrid.setVgap(4);

        Label acc1Title = new Label("ACC10001 (Savings):");
        acc1Title.setTextFill(Color.web("#94a3b8"));
        acc1Title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));

        acc1BalanceLabel = new Label("₹50,000.00");
        acc1BalanceLabel.setTextFill(Color.web("#10b981"));
        acc1BalanceLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        Label acc2Title = new Label("ACC10002 (Current):");
        acc2Title.setTextFill(Color.web("#94a3b8"));
        acc2Title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));

        acc2BalanceLabel = new Label("₹25,000.00");
        acc2BalanceLabel.setTextFill(Color.web("#10b981"));
        acc2BalanceLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        balancesGrid.add(acc1Title, 0, 0);
        balancesGrid.add(acc1BalanceLabel, 1, 0);
        balancesGrid.add(acc2Title, 0, 1);
        balancesGrid.add(acc2BalanceLabel, 1, 1);

        // Inputs
        GridPane inputsGrid = new GridPane();
        inputsGrid.setHgap(8);
        inputsGrid.setVgap(6);

        Label srcLbl = new Label("Source Acc:");
        srcLbl.setTextFill(Color.web("#cbd5e1"));
        srcLbl.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        sourceAccField = new TextField("ACC10001");
        sourceAccField.setStyle("-fx-background-color: #0f172a; -fx-text-fill: white; -fx-border-color: #475569; -fx-border-radius: 4; -fx-padding: 4 8;");

        Label destLbl = new Label("Dest Acc:");
        destLbl.setTextFill(Color.web("#cbd5e1"));
        destLbl.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        destAccField = new TextField("ACC10002");
        destAccField.setStyle("-fx-background-color: #0f172a; -fx-text-fill: white; -fx-border-color: #475569; -fx-border-radius: 4; -fx-padding: 4 8;");

        Label amtLbl = new Label("Amount (₹):");
        amtLbl.setTextFill(Color.web("#cbd5e1"));
        amtLbl.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        amountField = new TextField("5000.00");
        amountField.setStyle("-fx-background-color: #0f172a; -fx-text-fill: white; -fx-border-color: #475569; -fx-border-radius: 4; -fx-padding: 4 8;");

        inputsGrid.add(srcLbl, 0, 0);
        inputsGrid.add(sourceAccField, 1, 0);
        inputsGrid.add(destLbl, 0, 1);
        inputsGrid.add(destAccField, 1, 1);
        inputsGrid.add(amtLbl, 0, 2);
        inputsGrid.add(amountField, 1, 2);

        btnProcess = new Button("🔒 PROCESS SECURE TRANSACTION");
        btnProcess.setMaxWidth(Double.MAX_VALUE);
        btnProcess.setStyle("-fx-background-color: #059669; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 8 12; -fx-background-radius: 6; -fx-cursor: hand;");
        btnProcess.setOnAction(e -> handleProcessTransaction());

        btnResetBalances = new Button("↺ Reset Demo Balances (₹50k / ₹25k)");
        btnResetBalances.setMaxWidth(Double.MAX_VALUE);
        btnResetBalances.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 8; -fx-background-radius: 4; -fx-cursor: hand;");
        btnResetBalances.setOnAction(e -> handleResetBalances());

        card.getChildren().addAll(balancesGrid, new Separator(), inputsGrid, btnProcess, btnResetBalances);
        return card;
    }

    private VBox createPipelineStageCard() {
        VBox card = createCardContainer("PIPELINE STAGE VERIFICATION");
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(4);

        String[] stages = {
            "Routing",
            "Encryption",
            "CRC Generation",
            "Reliable Delivery",
            "TCP Communication",
            "CRC Verification",
            "Decryption",
            "Banking Transaction",
            "Database Persistence"
        };

        stageCheckmarkLabels.clear();
        for (int i = 0; i < stages.length; i++) {
            Label stageLbl = new Label(stages[i] + ":");
            stageLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
            stageLbl.setTextFill(Color.web("#cbd5e1"));

            Label checkmark = new Label("READY");
            checkmark.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
            checkmark.setTextFill(Color.web("#94a3b8"));

            stageCheckmarkLabels.put(stages[i], checkmark);
            stageCheckmarkLabels.put(stages[i].toUpperCase(), checkmark);

            grid.add(stageLbl, 0, i);
            grid.add(checkmark, 1, i);
        }

        card.getChildren().add(grid);
        return card;
    }

    private VBox createSecurityStatusCard() {
        VBox card = createCardContainer("SECURITY STATUS");
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(4);

        addStatusRow(grid, 0, "AES-GCM Status:", "ACTIVE", "#10b981");
        addStatusRow(grid, 1, "Encryption:", "AES-256-GCM", "#38bdf8");
        addStatusRow(grid, 2, "CRC Checksum:", "CRC-16-CCITT", "#38bdf8");
        addStatusRow(grid, 3, "Integrity:", "VERIFIED", "#10b981");
        addStatusRow(grid, 4, "Authentication:", "GCM AUTH TAG (128-bit)", "#8b5cf6");

        card.getChildren().add(grid);
        return card;
    }

    private VBox createReliabilityTcpCard() {
        VBox card = createCardContainer("RELIABILITY & TCP PROTOCOL");
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(4);

        addStatusRow(grid, 0, "Sliding Window:", "ACTIVE (Size: 2)", "#10b981");
        addStatusRow(grid, 1, "Go-Back-N ARQ:", "ACTIVE", "#10b981");
        addStatusRow(grid, 2, "TCP Socket:", "127.0.0.1 (Dynamic Port)", "#06b6d4");
        addStatusRow(grid, 3, "Framing Protocol:", "4-Byte Length Prefix UTF-8", "#cbd5e1");
        addStatusRow(grid, 4, "Max Payload Limit:", "1 MB", "#cbd5e1");

        card.getChildren().add(grid);
        return card;
    }

    private void addStatusRow(GridPane grid, int row, String labelText, String valueText, String valueColorHex) {
        Label lbl = new Label(labelText);
        lbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        lbl.setTextFill(Color.web("#94a3b8"));

        Label val = new Label(valueText);
        val.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        val.setTextFill(Color.web(valueColorHex));

        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }

    private VBox createCardContainer(String titleText) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(10, 12, 10, 12));
        card.setStyle("-fx-background-color: #1e293b; -fx-border-color: #334155; -fx-border-radius: 8; -fx-background-radius: 8;");

        Label title = new Label(titleText);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        title.setTextFill(Color.web("#38bdf8"));

        card.getChildren().add(title);
        return card;
    }

    // ==========================================
    // BOTTOM: ACTIVITY LOG PANEL
    // ==========================================
    private VBox createActivityLogPanel() {
        VBox panel = new VBox(4);
        panel.setPadding(new Insets(6, 14, 8, 14));
        panel.setStyle("-fx-background-color: #0f172a; -fx-border-color: #334155; -fx-border-width: 1 0 0 0;");

        Label title = new Label("SYSTEM ACTIVITY LOG FEED");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        title.setTextFill(Color.web("#94a3b8"));

        activityLogArea = new TextArea();
        activityLogArea.setPrefHeight(120);
        activityLogArea.setMinHeight(80);
        activityLogArea.setMaxHeight(160);
        activityLogArea.setEditable(false);
        activityLogArea.setWrapText(false);
        activityLogArea.setStyle("-fx-control-inner-background: #020617; -fx-text-fill: #38bdf8; -fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 11px;");

        panel.getChildren().addAll(title, activityLogArea);
        return panel;
    }

    // ==========================================
    // LOGIC & PIPELINE INTEGRATION
    // ==========================================
    private void handleProcessTransaction() {
        String srcAcc = sourceAccField.getText().trim();
        String destAcc = destAccField.getText().trim();
        double amount;

        try {
            amount = Double.parseDouble(amountField.getText().trim());
        } catch (NumberFormatException e) {
            showAlert("Invalid Amount", "Please enter a valid numeric transfer amount.");
            return;
        }

        // Highlight route visually first
        handleFindShortestRoute();

        // Create Request & Execute Pipeline
        SecureBankingRequest request = new SecureBankingRequest(srcAcc, destAcc, amount, TransactionType.TRANSFER, "Dashboard Secure Transfer");

        if (btnProcess != null) {
            btnProcess.setDisable(true);
            btnProcess.setText("⏳ PROCESSING PIPELINE...");
        }

        for (Label check : stageCheckmarkLabels.values()) {
            check.setText("PENDING");
            check.setTextFill(Color.web("#38bdf8"));
        }

        LoggerUtil.info(String.format("Processing Secure Banking Pipeline: Transfer ₹%.2f from %s to %s", amount, srcAcc, destAcc));

        bgExecutor.submit(() -> {
            try {
                PipelineResult result = pipeline.execute(request);

                Platform.runLater(() -> {
                    // Update Checklist on JavaFX Application Thread
                    updateChecklist(result);

                    // Update Balances in UI
                    updateAccountBalances();

                    if (!result.isSuccessful()) {
                        LoggerUtil.warn("Pipeline Execution FAILED: " + result.getErrorMessage());
                    }

                    if (btnProcess != null) {
                        btnProcess.setDisable(false);
                        btnProcess.setText("🔒 PROCESS SECURE TRANSACTION");
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    LoggerUtil.error("Pipeline Execution Exception: " + ex.getMessage());
                    if (btnProcess != null) {
                        btnProcess.setDisable(false);
                        btnProcess.setText("🔒 PROCESS SECURE TRANSACTION");
                    }
                });
            }
        });
    }

    private void updateChecklist(PipelineResult result) {
        setCheckmark("Routing", result.isSuccessful() || (result.getRoutingPath() != null && !result.getRoutingPath().isEmpty()));
        setCheckmark("Encryption", result.isEncryptionSuccessful());
        setCheckmark("CRC Generation", result.isCrcGenerated());
        setCheckmark("Reliable Delivery", result.isReliableTransmissionSuccessful());
        setCheckmark("TCP Communication", result.isTcpSuccessful());
        setCheckmark("CRC Verification", result.isCrcVerified());
        setCheckmark("Decryption", result.isDecryptionSuccessful());
        setCheckmark("Banking Transaction", result.isBankingTransactionSuccessful());
        setCheckmark("Database Persistence", result.isDatabasePersistenceSuccessful());
    }

    private void setCheckmark(String stageKey, boolean success) {
        Label check = stageCheckmarkLabels.get(stageKey);
        if (check != null) {
            if (success) {
                if ("CRC Verification".equalsIgnoreCase(stageKey)) {
                    check.setText("PASSED");
                } else {
                    check.setText("SUCCESS");
                }
                check.setTextFill(Color.web("#10b981"));
            } else {
                check.setText("FAILED");
                check.setTextFill(Color.web("#ef4444"));
            }
        }
    }

    private void updateAccountBalances() {
        bgExecutor.submit(() -> {
            try (Connection conn = dbManager.getConnection()) {
                Optional<Account> acc1 = accountRepo.findByAccountNumber(conn, "ACC10001");
                Optional<Account> acc2 = accountRepo.findByAccountNumber(conn, "ACC10002");

                String bal1Str = acc1.map(account -> String.format("₹%.2f", account.getBalance())).orElse("₹0.00");
                String bal2Str = acc2.map(account -> String.format("₹%.2f", account.getBalance())).orElse("₹0.00");

                Platform.runLater(() -> {
                    if (acc1BalanceLabel != null) acc1BalanceLabel.setText(bal1Str);
                    if (acc2BalanceLabel != null) acc2BalanceLabel.setText(bal2Str);
                });
            } catch (SQLException e) {
                LoggerUtil.error("Failed to refresh account balances from SQLite: " + e.getMessage());
            }
        });
    }

    private void handleResetBalances() {
        if (btnResetBalances != null) {
            btnResetBalances.setDisable(true);
        }
        bgExecutor.submit(() -> {
            try (Connection conn = dbManager.getConnection()) {
                accountRepo.updateBalance(conn, "ACC10001", 50000.0);
                accountRepo.updateBalance(conn, "ACC10002", 25000.0);
                updateAccountBalances();
                LoggerUtil.info("Demo account balances reset to ACC10001 = ₹50,000.00 and ACC10002 = ₹25,000.00");
            } catch (SQLException e) {
                LoggerUtil.error("Failed to reset demo balances: " + e.getMessage());
            } finally {
                Platform.runLater(() -> {
                    if (btnResetBalances != null) {
                        btnResetBalances.setDisable(false);
                    }
                });
            }
        });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @Override
    public void stop() throws Exception {
        if (logListener != null) {
            LoggerUtil.removeListener(logListener);
        }
        if (bgExecutor != null) {
            bgExecutor.shutdownNow();
        }
        if (dbManager != null) {
            dbManager.close();
        }
        LoggerUtil.info("Dashboard application shut down cleanly.");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
