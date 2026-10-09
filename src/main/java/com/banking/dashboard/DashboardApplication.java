package com.banking.dashboard;

import com.banking.database.AccountRepository;
import com.banking.database.DatabaseManager;
import com.banking.integration.PipelineResult;
import com.banking.integration.SecureBankingPipeline;
import com.banking.integration.SecureBankingRequest;
import com.banking.model.Account;
import com.banking.model.NetworkNode;
import com.banking.model.NodeType;
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
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * Professional JavaFX Dashboard Application for the Secure Banking Network.
 * Provides real-time visual network topology monitoring, Dijkstra & Distance Vector routing visualization,
 * AES-GCM security metrics, CRC error detection, sliding window reliability status, real TCP socket monitoring,
 * live transaction processing, and database balance reflection.
 */
public class DashboardApplication extends Application {

    private DatabaseManager dbManager;
    private AccountRepository accountRepo;
    private NetworkTopology topology;
    private DijkstraRouter dijkstraRouter;
    private DistanceVectorRouter dvRouter;
    private SecureBankingPipeline pipeline;

    // UI Controls
    private Label acc1BalanceLabel;
    private Label acc2BalanceLabel;
    private TextField sourceAccField;
    private TextField destAccField;
    private TextField amountField;
    private Label routingSummaryLabel;
    private Label dvSummaryLabel;
    private TextArea activityLogArea;

    // Stage Status Checkmarks
    private final Map<String, Label> stageCheckmarkLabels = new LinkedHashMap<>();

    // Topology Canvas Map (NodeId -> NodeVisualComponent)
    private final Map<String, NodeVisual> nodeVisualMap = new LinkedHashMap<>();
    private final List<LinkVisual> linkVisualList = new ArrayList<>();
    private Pane topologyPane;

    private List<String> activePathNodeIds = new ArrayList<>();

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

        NodeVisual(NetworkNode node, double x, double y, StackPane visualGroup, ShapeVisual shape, Label nameLabel, Label ipLabel, Label statusLabel) {
            this.node = node;
            this.x = x;
            this.y = y;
            this.visualGroup = visualGroup;
            this.shape = shape;
            this.nameLabel = nameLabel;
            this.ipLabel = ipLabel;
            this.statusLabel = statusLabel;
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
        LoggerUtil.addListener(log -> Platform.runLater(() -> {
            if (activityLogArea != null) {
                activityLogArea.appendText(log + "\n");
            }
        }));

        Scene scene = new Scene(root, 1420, 880);
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();

        LoggerUtil.info("Secure Banking Dashboard initialized successfully.");
        updateAccountBalances();
    }

    // ==========================================
    // TOP HEADER BAR
    // ==========================================
    private VBox createHeaderBar() {
        VBox header = new VBox(8);
        header.setPadding(new Insets(14, 20, 14, 20));
        header.setStyle("-fx-background-color: #1e293b; -fx-border-color: #334155; -fx-border-width: 0 0 1 0;");

        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label title = new Label("SECURE BANKING NETWORK");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#38bdf8")); // Bright Cyan

        Label subtitle = new Label("Secure & Reliable Banking Communication Monitoring System");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        subtitle.setTextFill(Color.web("#94a3b8"));
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Status Badges
        HBox statusBadges = new HBox(10);
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
        HBox badge = new HBox(6);
        badge.setAlignment(Pos.CENTER);
        badge.setPadding(new Insets(4, 10, 4, 10));
        badge.setStyle("-fx-background-color: #0f172a; -fx-border-color: " + colorHex + "; -fx-border-radius: 12; -fx-background-radius: 12;");

        Circle dot = new Circle(4, Color.web(colorHex));
        Label label = new Label(category + ": " + status);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        label.setTextFill(Color.web("#f8fafc"));

        badge.getChildren().addAll(dot, label);
        return badge;
    }

    // ==========================================
    // CENTER: NETWORK TOPOLOGY & ROUTING
    // ==========================================
    private VBox createCenterTopologyPanel() {
        VBox center = new VBox(12);
        center.setPadding(new Insets(16));
        center.setStyle("-fx-background-color: #0f172a;");

        // Action Toolbar
        HBox actionToolbar = new HBox(12);
        actionToolbar.setAlignment(Pos.CENTER_LEFT);

        Button btnFindRoute = new Button("⚡ FIND SHORTEST ROUTE (DIJKSTRA)");
        btnFindRoute.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 16; -fx-cursor: hand;");
        btnFindRoute.setOnAction(e -> handleFindShortestRoute());

        Button btnResetRoute = new Button("↺ CLEAR ROUTE");
        btnResetRoute.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 12; -fx-cursor: hand;");
        btnResetRoute.setOnAction(e -> clearRouteHighlight());

        routingSummaryLabel = new Label("Selected Route: None (Click 'Find Shortest Route' to compute path)");
        routingSummaryLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        routingSummaryLabel.setTextFill(Color.web("#94a3b8"));

        actionToolbar.getChildren().addAll(btnFindRoute, btnResetRoute, routingSummaryLabel);

        // Topology Canvas Container
        topologyPane = new Pane();
        topologyPane.setPrefSize(780, 460);
        topologyPane.setStyle("-fx-background-color: #1e293b; -fx-border-color: #334155; -fx-border-radius: 8; -fx-background-radius: 8;");

        buildTopologyVisual();

        // Routing Engine Metrics Box
        HBox metricsBox = new HBox(20);
        metricsBox.setPadding(new Insets(10, 16, 10, 16));
        metricsBox.setStyle("-fx-background-color: #1e293b; -fx-border-color: #334155; -fx-border-radius: 8; -fx-background-radius: 8;");
        metricsBox.setAlignment(Pos.CENTER_LEFT);

        dvSummaryLabel = new Label("Routing Comparison | Link-State (Dijkstra): Ready | Distance Vector (Bellman-Ford): Ready");
        dvSummaryLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        dvSummaryLabel.setTextFill(Color.web("#38bdf8"));
        metricsBox.getChildren().add(dvSummaryLabel);

        center.getChildren().addAll(actionToolbar, topologyPane, metricsBox);
        VBox.setVgrow(topologyPane, Priority.ALWAYS);
        return center;
    }

    private void buildTopologyVisual() {
        topologyPane.getChildren().clear();
        nodeVisualMap.clear();
        linkVisualList.clear();

        Map<String, NodePos> positions = new HashMap<>();
        positions.put("NODE-B1", new NodePos(80, 110));
        positions.put("ROUTER-B1", new NodePos(260, 110));
        positions.put("NODE-ATM", new NodePos(260, 250));
        positions.put("ROUTER-HQ", new NodePos(490, 200));
        positions.put("NODE-HQ", new NodePos(490, 70));
        positions.put("NODE-MGMT", new NodePos(690, 70));
        positions.put("NODE-DB", new NodePos(690, 200));
        positions.put("ROUTER-B2", new NodePos(260, 370));
        positions.put("NODE-B2", new NodePos(80, 370));
        positions.put("NODE-CS", new NodePos(490, 370));

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
            line.setStroke(Color.web("#475569"));
            line.setStrokeWidth(2.0);

            double midX = (p1.x + p2.x) / 2;
            double midY = (p1.y + p2.y) / 2;
            Label costLabel = new Label("Cost: " + String.format("%.1f", link.getCost()));
            costLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
            costLabel.setTextFill(Color.web("#94a3b8"));
            costLabel.setStyle("-fx-background-color: #0f172a; -fx-padding: 1 4; -fx-background-radius: 4;");
            costLabel.setLayoutX(midX - 18);
            costLabel.setLayoutY(midY - 8);

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
            String borderColor = "#38bdf8";

            if (node.getNodeType() == NodeType.ROUTER) {
                Circle circle = new Circle(28);
                circle.setFill(Color.web("#1e293b"));
                circle.setStroke(Color.web("#06b6d4"));
                circle.setStrokeWidth(2.5);
                shapeComp = circle;
            } else if (node.getNodeType() == NodeType.SERVER) {
                Rectangle rect = new Rectangle(80, 50, Color.web("#312e81"));
                rect.setArcWidth(10);
                rect.setArcHeight(10);
                rect.setStroke(Color.web("#10b981"));
                rect.setStrokeWidth(2.5);
                shapeComp = rect;
            } else if (node.getNodeType() == NodeType.ATM) {
                Rectangle rect = new Rectangle(75, 48, Color.web("#0891b2"));
                rect.setArcWidth(8);
                rect.setArcHeight(8);
                rect.setStroke(Color.web("#38bdf8"));
                rect.setStrokeWidth(2.0);
                shapeComp = rect;
            } else {
                Rectangle rect = new Rectangle(75, 48, Color.web("#334155"));
                rect.setArcWidth(8);
                rect.setArcHeight(8);
                rect.setStroke(Color.web("#94a3b8"));
                rect.setStrokeWidth(2.0);
                shapeComp = rect;
            }

            VBox labelBox = new VBox(1);
            labelBox.setAlignment(Pos.CENTER);
            Label nameL = new Label(node.getNodeName());
            nameL.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
            nameL.setTextFill(Color.web("#f8fafc"));

            Label ipL = new Label(node.getIpAddress().getDottedDecimal());
            ipL.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 9));
            ipL.setTextFill(Color.web("#94a3b8"));

            Label statusL = new Label("● ONLINE");
            statusL.setFont(Font.font("Segoe UI", FontWeight.BOLD, 8));
            statusL.setTextFill(Color.web("#10b981"));

            labelBox.getChildren().addAll(nameL, ipL, statusL);
            nodeGroup.getChildren().addAll(shapeComp, labelBox);

            topologyPane.getChildren().add(nodeGroup);
            nodeVisualMap.put(node.getNodeId(), new NodeVisual(node, pos.x, pos.y, nodeGroup, new ShapeVisual(shapeComp), nameL, ipL, statusL));
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
        // Reset all nodes
        for (NodeVisual nv : nodeVisualMap.values()) {
            boolean inPath = pathNodeIds.contains(nv.node.getNodeId());
            if (inPath) {
                nv.shape.shapeComponent.setStroke(Color.web("#10b981"));
                nv.shape.shapeComponent.setStrokeWidth(3.5);
            } else {
                nv.shape.shapeComponent.setStroke(Color.web("#475569"));
                nv.shape.shapeComponent.setStrokeWidth(2.0);
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
                lv.lineComponent.setStrokeWidth(4.0);
                lv.costLabel.setTextFill(Color.web("#10b981"));
            } else {
                lv.lineComponent.setStroke(Color.web("#475569"));
                lv.lineComponent.setStrokeWidth(2.0);
                lv.costLabel.setTextFill(Color.web("#94a3b8"));
            }
        }
    }

    private void clearRouteHighlight() {
        activePathNodeIds.clear();
        for (NodeVisual nv : nodeVisualMap.values()) {
            nv.shape.shapeComponent.setStroke(Color.web("#06b6d4"));
            nv.shape.shapeComponent.setStrokeWidth(2.0);
        }
        for (LinkVisual lv : linkVisualList) {
            lv.lineComponent.setStroke(Color.web("#475569"));
            lv.lineComponent.setStrokeWidth(2.0);
            lv.costLabel.setTextFill(Color.web("#94a3b8"));
        }
        routingSummaryLabel.setText("Selected Route: None");
        routingSummaryLabel.setTextFill(Color.web("#94a3b8"));
    }

    // ==========================================
    // RIGHT SIDEBAR: CONTROLS & STATUS CARDS
    // ==========================================
    private ScrollPane createSidebarControlPanel() {
        VBox sidebar = new VBox(14);
        sidebar.setPadding(new Insets(16));
        sidebar.setPrefWidth(430);
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
        scroll.setStyle("-fx-background-color: #0f172a; -fx-background: #0f172a; -fx-border-color: #334155; -fx-border-width: 0 0 0 1;");
        return scroll;
    }

    private VBox createTransactionControlCard() {
        VBox card = createCardContainer("BANKING TRANSACTION & BALANCE");

        // Account Balances Display
        GridPane balancesGrid = new GridPane();
        balancesGrid.setHgap(12);
        balancesGrid.setVgap(6);

        Label acc1Title = new Label("ACC10001 (Savings):");
        acc1Title.setTextFill(Color.web("#94a3b8"));
        acc1Title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        acc1BalanceLabel = new Label("₹50,000.00");
        acc1BalanceLabel.setTextFill(Color.web("#10b981"));
        acc1BalanceLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));

        Label acc2Title = new Label("ACC10002 (Current):");
        acc2Title.setTextFill(Color.web("#94a3b8"));
        acc2Title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        acc2BalanceLabel = new Label("₹25,000.00");
        acc2BalanceLabel.setTextFill(Color.web("#10b981"));
        acc2BalanceLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));

        balancesGrid.add(acc1Title, 0, 0);
        balancesGrid.add(acc1BalanceLabel, 1, 0);
        balancesGrid.add(acc2Title, 0, 1);
        balancesGrid.add(acc2BalanceLabel, 1, 1);

        // Inputs
        GridPane inputsGrid = new GridPane();
        inputsGrid.setHgap(10);
        inputsGrid.setVgap(8);

        Label srcLbl = new Label("Source Acc:");
        srcLbl.setTextFill(Color.web("#cbd5e1"));
        sourceAccField = new TextField("ACC10001");
        sourceAccField.setStyle("-fx-background-color: #0f172a; -fx-text-fill: white; -fx-border-color: #475569; -fx-border-radius: 4;");

        Label destLbl = new Label("Dest Acc:");
        destLbl.setTextFill(Color.web("#cbd5e1"));
        destAccField = new TextField("ACC10002");
        destAccField.setStyle("-fx-background-color: #0f172a; -fx-text-fill: white; -fx-border-color: #475569; -fx-border-radius: 4;");

        Label amtLbl = new Label("Amount (₹):");
        amtLbl.setTextFill(Color.web("#cbd5e1"));
        amountField = new TextField("5000.00");
        amountField.setStyle("-fx-background-color: #0f172a; -fx-text-fill: white; -fx-border-color: #475569; -fx-border-radius: 4;");

        inputsGrid.add(srcLbl, 0, 0);
        inputsGrid.add(sourceAccField, 1, 0);
        inputsGrid.add(destLbl, 0, 1);
        inputsGrid.add(destAccField, 1, 1);
        inputsGrid.add(amtLbl, 0, 2);
        inputsGrid.add(amountField, 1, 2);

        Button btnProcess = new Button("🔒 PROCESS SECURE TRANSACTION");
        btnProcess.setMaxWidth(Double.MAX_VALUE);
        btnProcess.setStyle("-fx-background-color: #059669; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 10; -fx-background-radius: 6; -fx-cursor: hand;");
        btnProcess.setOnAction(e -> handleProcessTransaction());

        Button btnResetBalances = new Button("↺ Reset Demo Balances (₹50k / ₹25k)");
        btnResetBalances.setMaxWidth(Double.MAX_VALUE);
        btnResetBalances.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 6; -fx-background-radius: 4; -fx-cursor: hand;");
        btnResetBalances.setOnAction(e -> handleResetBalances());

        card.getChildren().addAll(balancesGrid, new Separator(), inputsGrid, btnProcess, btnResetBalances);
        return card;
    }

    private VBox createPipelineStageCard() {
        VBox card = createCardContainer("PIPELINE STAGE VERIFICATION");
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(5);

        String[] stages = {
            "ROUTING",
            "ENCRYPTION",
            "CRC GENERATION",
            "RELIABLE DELIVERY",
            "TCP COMMUNICATION",
            "CRC VERIFICATION",
            "DECRYPTION",
            "BANKING TRANSACTION",
            "DATABASE PERSISTENCE"
        };

        stageCheckmarkLabels.clear();
        for (int i = 0; i < stages.length; i++) {
            Label stageLbl = new Label(stages[i]);
            stageLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
            stageLbl.setTextFill(Color.web("#cbd5e1"));

            Label checkmark = new Label("READY");
            checkmark.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
            checkmark.setTextFill(Color.web("#94a3b8"));

            stageCheckmarkLabels.put(stages[i], checkmark);

            grid.add(stageLbl, 0, i);
            grid.add(checkmark, 1, i);
        }

        card.getChildren().add(grid);
        return card;
    }

    private VBox createSecurityStatusCard() {
        VBox card = createCardContainer("SECURITY STATUS");
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(6);

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
        grid.setHgap(14);
        grid.setVgap(6);

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
        VBox card = new VBox(10);
        card.setPadding(new Insets(14));
        card.setStyle("-fx-background-color: #1e293b; -fx-border-color: #334155; -fx-border-radius: 8; -fx-background-radius: 8;");

        Label title = new Label(titleText);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        title.setTextFill(Color.web("#38bdf8"));

        card.getChildren().add(title);
        return card;
    }

    // ==========================================
    // BOTTOM: ACTIVITY LOG PANEL
    // ==========================================
    private VBox createActivityLogPanel() {
        VBox panel = new VBox(6);
        panel.setPadding(new Insets(10, 16, 12, 16));
        panel.setStyle("-fx-background-color: #0f172a; -fx-border-color: #334155; -fx-border-width: 1 0 0 0;");

        Label title = new Label("SYSTEM ACTIVITY LOG FEED");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        title.setTextFill(Color.web("#94a3b8"));

        activityLogArea = new TextArea();
        activityLogArea.setPrefHeight(150);
        activityLogArea.setEditable(false);
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

        LoggerUtil.info(String.format("Processing Secure Banking Pipeline: Transfer ₹%.2f from %s to %s", amount, srcAcc, destAcc));

        PipelineResult result = pipeline.execute(request);

        // Update Checklist
        updateChecklist(result);

        // Update Balances in UI
        updateAccountBalances();

        if (result.isSuccessful()) {
            LoggerUtil.info("Pipeline Execution PASSED cleanly for Txn ID: " + result.getTransactionId());
        } else {
            LoggerUtil.warn("Pipeline Execution FAILED: " + result.getErrorMessage());
        }
    }

    private void updateChecklist(PipelineResult result) {
        setCheckmark("ROUTING", result.isSuccessful() || (result.getRoutingPath() != null && !result.getRoutingPath().isEmpty()));
        setCheckmark("ENCRYPTION", result.isEncryptionSuccessful());
        setCheckmark("CRC GENERATION", result.isCrcGenerated());
        setCheckmark("RELIABLE DELIVERY", result.isReliableTransmissionSuccessful());
        setCheckmark("TCP COMMUNICATION", result.isTcpSuccessful());
        setCheckmark("CRC VERIFICATION", result.isCrcVerified());
        setCheckmark("DECRYPTION", result.isDecryptionSuccessful());
        setCheckmark("BANKING TRANSACTION", result.isBankingTransactionSuccessful());
        setCheckmark("DATABASE PERSISTENCE", result.isDatabasePersistenceSuccessful());
    }

    private void setCheckmark(String stageKey, boolean success) {
        Label check = stageCheckmarkLabels.get(stageKey);
        if (check != null) {
            if (success) {
                check.setText("✓ SUCCESS");
                check.setTextFill(Color.web("#10b981"));
            } else {
                check.setText("✗ FAILED");
                check.setTextFill(Color.web("#ef4444"));
            }
        }
    }

    private void updateAccountBalances() {
        try (Connection conn = dbManager.getConnection()) {
            Optional<Account> acc1 = accountRepo.findByAccountNumber(conn, "ACC10001");
            Optional<Account> acc2 = accountRepo.findByAccountNumber(conn, "ACC10002");

            acc1.ifPresent(account -> acc1BalanceLabel.setText(String.format("₹%.2f", account.getBalance())));
            acc2.ifPresent(account -> acc2BalanceLabel.setText(String.format("₹%.2f", account.getBalance())));
        } catch (SQLException e) {
            LoggerUtil.error("Failed to refresh account balances from SQLite: " + e.getMessage());
        }
    }

    private void handleResetBalances() {
        try (Connection conn = dbManager.getConnection()) {
            accountRepo.updateBalance(conn, "ACC10001", 50000.0);
            accountRepo.updateBalance(conn, "ACC10002", 25000.0);
            updateAccountBalances();
            LoggerUtil.info("Demo account balances reset to ACC10001 = ₹50,000.00 and ACC10002 = ₹25,000.00");
        } catch (SQLException e) {
            LoggerUtil.error("Failed to reset demo balances: " + e.getMessage());
        }
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
        if (dbManager != null) {
            dbManager.close();
        }
        LoggerUtil.info("Dashboard application shut down cleanly.");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
