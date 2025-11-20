package com.businesscalculation13.monthlyprofitcalculatorfx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.embed.swing.SwingFXUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.imageio.ImageIO;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.awt.image.BufferedImage;

import net.sourceforge.tess4j.Tesseract;

public class MonthlyProfitCalculatorFX extends Application {

    // --- Design Constants ---
    private static final String COLOR_BLUE = "#1E56CD";
    private static final String COLOR_OFF_WHITE = "#FDF8F2";
    private static final String COLOR_GREEN = "#009900";
    private static final String COLOR_RED = "#FF0000";

    private static final Font FONT_TITLE = Font.font("Arial", FontWeight.BLACK, 28);
    private static final Font FONT_HEADER = Font.font("Arial", FontWeight.BLACK, 20);
    private static final Font FONT_BODY_BOLD = Font.font("Arial", FontWeight.BOLD, 14);
    private static final Font FONT_MONO = Font.font("Monospaced", FontWeight.BOLD, 16);

    private static final String STYLE_BORDER_THICK = "-fx-border-color: black; -fx-border-width: 4;";
    private static final String STYLE_BG_OFF_WHITE = "-fx-background-color: " + COLOR_OFF_WHITE + ";";
    private static final String STYLE_BG_BLUE = "-fx-background-color: " + COLOR_BLUE + ";";

    // --- Services ---
    private DataManager dataManager;
    private ScannerService scannerService;
    private Tesseract tesseract;

    // --- Layout & State ---
    private BorderPane mainLayout;
    private StackPane contentArea;
    private TextField barcodeInput;
    private Stage primaryStage;

    private DataManager.ProductType currentRestockTarget = null;
    private Label systemStatusLabel;
    private Label totalSalesLabel;
    private TextField sellSearchField;
    private TilePane sellGrid;
    private boolean isSellViewActive = false;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        dataManager = new DataManager();
        scannerService = new ScannerService();

        try {
            tesseract = new Tesseract();
            tesseract.setDatapath("tessdata");
        } catch (Exception e) {
            System.out.println("OCR Warning: " + e.getMessage());
        }

        // Initialize Main Layout Container
        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #e5e5e5;");

        // Create Content Area
        contentArea = new StackPane();
        contentArea.setPadding(new Insets(30));
        contentArea.setAlignment(Pos.TOP_CENTER);

        // START IN FULL SCREEN
        stage.setFullScreen(true);
        // THIS REMOVES THE ANNOYING MESSAGE
        stage.setFullScreenExitHint("");

        // Start with LOGIN
        showLoginScreen(stage);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (scannerService != null) scannerService.stopCamera();
    }

    // ==========================================
    //    NEW SPLIT-SCREEN LOGIN & REGISTER
    // ==========================================

    private void showLoginScreen(Stage stage) {
        HBox rootSplit = new HBox();
        rootSplit.setFillHeight(true);

        // --- LEFT SIDE (Branding) ---
        VBox leftSide = new VBox(20);
        leftSide.setAlignment(Pos.CENTER);
        leftSide.setStyle(STYLE_BG_BLUE + "-fx-border-color: black; -fx-border-width: 0 4 0 0;");
        HBox.setHgrow(leftSide, Priority.ALWAYS);
        leftSide.setPrefWidth(stage.getWidth() / 2);

        Label brandTitle = new Label("MY\nSTORE");
        brandTitle.setFont(Font.font("Arial", FontWeight.BLACK, 72));
        brandTitle.setTextFill(Color.WHITE);
        brandTitle.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        brandTitle.setStyle("-fx-effect: dropshadow(one-pass-box, black, 10, 10, 0, 0);");

        Label brandSub = new Label("MANAGER SYSTEM V1.0");
        brandSub.setFont(Font.font("Monospaced", FontWeight.BOLD, 24));
        brandSub.setTextFill(Color.WHITE);

        leftSide.getChildren().addAll(brandTitle, brandSub);

        // --- RIGHT SIDE (Login Form + Floating Exit Button) ---
        StackPane rightSideContainer = new StackPane();
        rightSideContainer.setStyle(STYLE_BG_OFF_WHITE);
        HBox.setHgrow(rightSideContainer, Priority.ALWAYS);
        rightSideContainer.setPrefWidth(stage.getWidth() / 2);

        // 1. The Centered Login Card
        VBox centerContent = new VBox();
        centerContent.setAlignment(Pos.CENTER);
        centerContent.setPadding(new Insets(50));

        VBox loginCard = createSketchCard("AUTHENTICATION");
        loginCard.setMaxWidth(400);
        VBox cardContent = (VBox) loginCard.getChildren().get(1);
        cardContent.setSpacing(20);

        TextField userField = createSketchInput("Username");
        PasswordField passField = createSketchPasswordField("Password");
        Label errorLabel = new Label("");
        errorLabel.setTextFill(Color.RED);
        errorLabel.setFont(FONT_BODY_BOLD);

        Button btnLogin = createSketchButton("LOGIN NOW", true);
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setOnAction(e -> {
            String u = userField.getText();
            String p = passField.getText();
            if (dataManager.validateLogin(u, p)) {
                initializeMainUI();
            } else {
                errorLabel.setText("ACCESS DENIED: Invalid Credentials");
            }
        });

        Button btnGoToRegister = new Button("Create New Account");
        btnGoToRegister.setStyle("-fx-background-color: transparent; -fx-text-fill: " + COLOR_BLUE + "; -fx-font-weight: bold; -fx-font-size: 14; -fx-cursor: hand; -fx-underline: true;");
        btnGoToRegister.setOnAction(e -> showRegisterScreen(stage));

        cardContent.getChildren().addAll(userField, passField, errorLabel, btnLogin, btnGoToRegister);
        centerContent.getChildren().add(loginCard);

        // 2. The Exit Button (Floating Top Right)
        Button btnExit = new Button("EXIT SYSTEM");
        btnExit.setStyle("-fx-background-color: white; -fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 12; -fx-cursor: hand; -fx-border-color: red; -fx-border-width: 2; -fx-padding: 10 20 10 20;");
        btnExit.setOnAction(e -> Platform.exit());

        StackPane.setAlignment(btnExit, Pos.TOP_RIGHT);
        StackPane.setMargin(btnExit, new Insets(30));

        rightSideContainer.getChildren().addAll(centerContent, btnExit);

        rootSplit.getChildren().addAll(leftSide, rightSideContainer);

        Scene scene = new Scene(rootSplit);
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.show();
    }

    private void showRegisterScreen(Stage stage) {
        HBox rootSplit = new HBox();
        rootSplit.setFillHeight(true);

        // --- LEFT SIDE ---
        VBox leftSide = new VBox(20);
        leftSide.setAlignment(Pos.CENTER);
        leftSide.setStyle(STYLE_BG_BLUE + "-fx-border-color: black; -fx-border-width: 0 4 0 0;");
        HBox.setHgrow(leftSide, Priority.ALWAYS);

        Label brandTitle = new Label("JOIN\nTEAM");
        brandTitle.setFont(Font.font("Arial", FontWeight.BLACK, 72));
        brandTitle.setTextFill(Color.WHITE);
        brandTitle.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        brandTitle.setStyle("-fx-effect: dropshadow(one-pass-box, black, 10, 10, 0, 0);");
        leftSide.getChildren().add(brandTitle);

        // --- RIGHT SIDE ---
        VBox rightSide = new VBox(30);
        rightSide.setAlignment(Pos.CENTER);
        rightSide.setStyle(STYLE_BG_OFF_WHITE);
        HBox.setHgrow(rightSide, Priority.ALWAYS);
        rightSide.setPadding(new Insets(50));

        VBox regCard = createSketchCard("NEW USER REGISTRATION");
        regCard.setMaxWidth(400);
        VBox cardContent = (VBox) regCard.getChildren().get(1);
        cardContent.setSpacing(15);

        TextField userField = createSketchInput("Desired Username");
        PasswordField passField = createSketchPasswordField("Set Password");
        PasswordField confirmField = createSketchPasswordField("Confirm Password");

        Label errorLabel = new Label("");
        errorLabel.setTextFill(Color.RED);
        errorLabel.setFont(FONT_BODY_BOLD);
        errorLabel.setWrapText(true);

        Button btnRegister = createSketchButton("CREATE ACCOUNT", true);
        btnRegister.setMaxWidth(Double.MAX_VALUE);
        btnRegister.setOnAction(e -> {
            String u = userField.getText();
            String p = passField.getText();
            String c = confirmField.getText();

            if(u.isEmpty() || p.isEmpty()) {
                errorLabel.setText("All fields are required.");
                return;
            }
            if(!p.equals(c)) {
                errorLabel.setText("Passwords do not match.");
                return;
            }

            if(dataManager.registerUser(u, p)) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Account created successfully! Logging you in...");
                alert.showAndWait();
                showLoginScreen(stage);
            } else {
                errorLabel.setText("Username already exists.");
            }
        });

        Button btnBack = new Button("← Back to Login");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: gray; -fx-font-weight: bold; -fx-font-size: 14; -fx-cursor: hand;");
        btnBack.setOnAction(e -> showLoginScreen(stage));

        cardContent.getChildren().addAll(userField, passField, confirmField, errorLabel, btnRegister, btnBack);
        rightSide.getChildren().add(regCard);

        rootSplit.getChildren().addAll(leftSide, rightSide);

        Scene scene = new Scene(rootSplit);
        stage.setScene(scene);
        stage.setFullScreen(true);
    }

    // ==========================================
    //    MAIN DASHBOARD LOGIC
    // ==========================================

    private void initializeMainUI() {
        scannerService.startCamera(barcode -> Platform.runLater(() -> handleBarcodeScan(barcode)));

        VBox sidebar = createSidebar();
        mainLayout.setLeft(sidebar);

        VBox rightSide = new VBox();
        rightSide.setStyle(STYLE_BG_BLUE);
        rightSide.setFillWidth(true);
        HBox.setHgrow(rightSide, Priority.ALWAYS);

        HBox headerBar = createHeaderBar();
        contentArea.getChildren().clear();
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        rightSide.getChildren().addAll(headerBar, contentArea);
        mainLayout.setCenter(rightSide);

        showDashboard();

        Scene scene = new Scene(mainLayout);
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true);
    }

    private void handleBarcodeScan(String scannedText) {
        if (currentRestockTarget != null) {
            String result = dataManager.addUniqueItem(scannedText, currentRestockTarget.getName());
            if(result.equals("Success")) {
                showAlert("Restock Success", "Added new " + currentRestockTarget.getName() + " to inventory!");
            } else {
                showAlert("Restock Failed", result);
            }
            currentRestockTarget = null;
            resetSystemStatus();
            showDashboard();
            return;
        }
        if (isSellViewActive) {
            if (dataManager.sellUniqueItem(scannedText) != null) {
                refreshSellGrid();
            }
            return;
        }
        if (barcodeInput != null && barcodeInput.getScene() != null) {
            barcodeInput.setText(scannedText);
        }
    }

    private void resetSystemStatus() {
        if (systemStatusLabel != null) {
            systemStatusLabel.setText("System Online");
            systemStatusLabel.setTextFill(Color.WHITE);
        }
    }

    private void showQRCodePopup(String productName) {
        Stage dialog = new Stage();
        dialog.setTitle("QR Code");
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle(STYLE_BG_OFF_WHITE);

        Label lbl = new Label(productName.toUpperCase());
        lbl.setFont(FONT_HEADER);

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(productName, BarcodeFormat.QR_CODE, 250, 250);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            ImageView imgView = new ImageView(SwingFXUtils.toFXImage(bufferedImage, null));
            root.getChildren().addAll(lbl, imgView, new Label("Print and scan to Sell/Restock"));
        } catch (Exception e) {
            root.getChildren().add(new Label("Error generating QR"));
        }

        Scene scene = new Scene(root, 350, 400);
        dialog.setScene(scene);
        dialog.show();
    }

    // --- DASHBOARD ---
    private void showDashboard() {
        isSellViewActive = false;
        VBox dashboardLayout = new VBox(20);
        dashboardLayout.setAlignment(Pos.TOP_CENTER);
        dashboardLayout.setFillWidth(true);

        HBox topSection = new HBox(20);
        topSection.setAlignment(Pos.CENTER_LEFT);
        topSection.setPrefHeight(160);

        // Status Card
        VBox statusCard = createSketchCard("Status");
        statusCard.setPrefWidth(400); statusCard.setPrefHeight(160);
        VBox statusContent = (VBox) statusCard.getChildren().get(1);
        Text statusDesc = new Text("Scanner Active. Database Connected.");
        statusDesc.setFont(FONT_BODY_BOLD);
        statusContent.getChildren().add(statusDesc);

        double[] financials = dataManager.getFinancialSummary();
        // Sales Card
        VBox salesCard = createSketchCard("Total Sales");
        salesCard.setPrefWidth(300); salesCard.setPrefHeight(160);
        VBox salesContent = (VBox) salesCard.getChildren().get(1);
        salesContent.setAlignment(Pos.CENTER);
        totalSalesLabel = new Label("₱" + String.format("%,.2f", financials[0]));
        totalSalesLabel.setFont(Font.font("Monospaced", FontWeight.BLACK, 36));
        totalSalesLabel.setTextFill(Color.web(COLOR_BLUE));
        salesContent.getChildren().add(totalSalesLabel);

        topSection.getChildren().addAll(statusCard, salesCard);

        VBox gridContainer = new VBox(10);
        Label gridTitle = new Label("VISUAL INVENTORY STACKS");
        gridTitle.setFont(FONT_HEADER);
        gridTitle.setStyle("-fx-text-fill: white; -fx-effect: dropshadow(one-pass-box, black, 2, 2, 0, 0);");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        TilePane grid = new TilePane();
        grid.setHgap(20); grid.setVgap(20);
        grid.setPadding(new Insets(10));
        grid.setAlignment(Pos.TOP_LEFT);

        List<DataManager.ProductType> products = dataManager.getAllProducts();
        if (products.isEmpty()) {
            Label emptyMsg = new Label("No products found. Go to 'PRODUCTS' tab to add some!");
            emptyMsg.setFont(FONT_HEADER); emptyMsg.setTextFill(Color.WHITE);
            grid.getChildren().add(emptyMsg);
        }
        for (DataManager.ProductType p : products) {
            grid.getChildren().add(createProductStackCard(p));
        }

        scrollPane.setContent(grid);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        gridContainer.getChildren().addAll(gridTitle, scrollPane);
        VBox.setVgrow(gridContainer, Priority.ALWAYS);

        dashboardLayout.getChildren().addAll(topSection, gridContainer);
        setContent(dashboardLayout);
    }

    private VBox createProductStackCard(DataManager.ProductType p) {
        VBox card = createSketchCard("");
        card.setPrefWidth(220);
        card.setMinHeight(Region.USE_PREF_SIZE);

        VBox content = (VBox) card.getChildren().get(0);
        content.setAlignment(Pos.CENTER); content.setSpacing(8);

        ImageView imgView = new ImageView();
        imgView.setFitHeight(80); imgView.setFitWidth(80); imgView.setPreserveRatio(true);
        try {
            if (p.getImagePath() != null && !p.getImagePath().isEmpty()) {
                String path = p.getImagePath();
                if (!path.startsWith("file:") && !path.startsWith("http")) path = new File(path).toURI().toString();
                imgView.setImage(new Image(path));
            }
        } catch (Exception e) { /* Ignore */ }

        StackPane imgBox = new StackPane(imgView);
        imgBox.setPrefSize(90, 90);
        imgBox.setStyle("-fx-border-color: black; -fx-border-width: 2; -fx-background-color: white;");

        Label nameLbl = new Label(p.getName());
        nameLbl.setFont(FONT_BODY_BOLD);
        nameLbl.setStyle("-fx-text-fill: black;"); // This one was working
        nameLbl.setWrapText(true); nameLbl.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Label priceLbl = new Label("₱" + String.format("%,.2f", p.getPrice()));
        priceLbl.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        priceLbl.setStyle("-fx-text-fill: black;"); // FORCE BLACK VIA CSS

        int currentStock = dataManager.getStockCount(p.getId());
        Label stockLbl = new Label(String.valueOf(currentStock));
        stockLbl.setFont(Font.font("Monospaced", FontWeight.BLACK, 32));
        stockLbl.setStyle("-fx-text-fill: black;"); // FORCE BLACK VIA CSS

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER);

        Button btnSell = createSketchButton("-", false);
        btnSell.setOnAction(e -> {
            if (dataManager.sellByOneClick(p.getId())) {
                int newCount = Integer.parseInt(stockLbl.getText()) - 1;
                stockLbl.setText(String.valueOf(newCount));
                // Ensuring no logic reverts color to red here
                double[] fins = dataManager.getFinancialSummary();
                if(totalSalesLabel != null) totalSalesLabel.setText("₱" + String.format("%,.2f", fins[0]));
            } else { showAlert("Stock Error", "No items to sell!"); }
        });

        Button btnRestock = createSketchButton("+", true);
        btnRestock.setOnAction(e -> {
            currentRestockTarget = p;
            showAlert("Ready to Scan", "Scan a new item now to add it to '" + p.getName() + "'");
        });

        actions.getChildren().addAll(btnSell, btnRestock);
        content.getChildren().addAll(imgBox, nameLbl, priceLbl, stockLbl, actions);
        return card;
    }

    private void performQuickSell(DataManager.ProductType p) {
        if (dataManager.sellByOneClick(p.getId())) {
            refreshSellGrid();
            if (sellSearchField != null) sellSearchField.requestFocus();
        } else {
            showAlert("Out of Stock", "No items available for " + p.getName());
        }
    }

    // --- PRODUCTS LIST ---
    private void showProducts() {
        isSellViewActive = false;

        VBox container = new VBox(20);
        container.setAlignment(Pos.TOP_CENTER);
        container.setFillWidth(true);

        Label title = new Label("PRODUCT INVENTORY");
        title.setFont(FONT_HEADER);
        title.setTextFill(Color.WHITE);

        TilePane grid = new TilePane();
        grid.setHgap(20); grid.setVgap(20);
        grid.setPadding(new Insets(10));
        grid.setAlignment(Pos.TOP_LEFT);

        List<DataManager.ProductType> products = dataManager.getAllProducts();
        for (DataManager.ProductType p : products) {
            grid.getChildren().add(createProductInfoCard(p));
        }

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        container.getChildren().addAll(title, scroll);
        setContent(container);
    }

    private VBox createProductInfoCard(DataManager.ProductType p) {
        VBox card = createSketchCard("");
        card.setPrefWidth(200);
        card.setMinHeight(Region.USE_PREF_SIZE);

        VBox content = (VBox) card.getChildren().get(0);
        content.setAlignment(Pos.CENTER); content.setSpacing(10);

        ImageView imgView = new ImageView();
        imgView.setFitHeight(80); imgView.setFitWidth(80); imgView.setPreserveRatio(true);
        try {
            if (p.getImagePath() != null && !p.getImagePath().isEmpty()) {
                String path = p.getImagePath();
                if (!path.startsWith("file:") && !path.startsWith("http")) path = new File(path).toURI().toString();
                imgView.setImage(new Image(path));
            }
        } catch (Exception e) { /* Ignore */ }
        StackPane imgBox = new StackPane(imgView);
        imgBox.setPrefSize(90, 90);
        imgBox.setStyle("-fx-border-color: black; -fx-border-width: 2; -fx-background-color: white;");

        Label nameLbl = new Label(p.getName());
        nameLbl.setFont(FONT_BODY_BOLD);
        nameLbl.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        nameLbl.setWrapText(true);

        Label priceLbl = new Label("₱" + String.format("%,.2f", p.getPrice()));
        priceLbl.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        priceLbl.setStyle("-fx-text-fill: black;"); // FORCE BLACK VIA CSS

        int stock = dataManager.getStockCount(p.getId());
        Label stockLbl = new Label("Stock: " + stock);
        stockLbl.setFont(Font.font("Monospaced", FontWeight.BOLD, 18));
        stockLbl.setStyle("-fx-text-fill: black;"); // FORCE BLACK VIA CSS

        Button btnQR = createSketchButton("GET QR", true);
        btnQR.setStyle(btnQR.getStyle() + "-fx-font-size: 12; -fx-padding: 8 15 8 15;");
        btnQR.setOnAction(e -> showQRCodePopup(p.getName()));

        content.getChildren().addAll(imgBox, nameLbl, priceLbl, stockLbl, btnQR);
        return card;
    }

    // --- SELL VIEW ---
    private void showSellProductView() {
        isSellViewActive = true;
        VBox container = new VBox(20); container.setAlignment(Pos.TOP_CENTER); container.setFillWidth(true);

        HBox topBar = new HBox(20); topBar.setAlignment(Pos.CENTER_LEFT);
        Button btnBack = new Button("← GO BACK");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-cursor: hand;");
        btnBack.setOnAction(e -> showAddAndSell());
        sellSearchField = createSketchInput("Type to Search...");
        sellSearchField.setPrefWidth(400);
        sellSearchField.textProperty().addListener((obs, oldVal, newVal) -> refreshSellGrid());
        topBar.getChildren().addAll(btnBack, sellSearchField);

        sellGrid = new TilePane(); sellGrid.setHgap(20); sellGrid.setVgap(20); sellGrid.setPadding(new Insets(10));
        ScrollPane scroll = new ScrollPane(sellGrid); scroll.setFitToWidth(true); scroll.setStyle("-fx-background: transparent;");
        refreshSellGrid();

        container.getChildren().addAll(topBar, scroll); VBox.setVgrow(scroll, Priority.ALWAYS);
        setContent(container);
    }

    private void refreshSellGrid() {
        if (sellGrid == null) return;
        sellGrid.getChildren().clear();
        String query = (sellSearchField != null) ? sellSearchField.getText().toLowerCase() : "";
        List<DataManager.ProductType> all = dataManager.getAllProducts();
        List<DataManager.ProductType> filtered = all.stream().filter(p -> p.getName().toLowerCase().contains(query)).collect(Collectors.toList());
        for (DataManager.ProductType p : filtered) sellGrid.getChildren().add(createSellCard(p));
    }

    private VBox createSellCard(DataManager.ProductType p) {
        VBox card = createSketchCard("");
        card.setPrefWidth(160);
        card.setMinHeight(Region.USE_PREF_SIZE);
        card.setCursor(javafx.scene.Cursor.HAND);

        card.setStyle("-fx-background-color: " + COLOR_OFF_WHITE + "; -fx-border-color: black; -fx-border-width: 3; -fx-effect: dropshadow(one-pass-box, rgba(0,0,0,0.5), 4, 4, 4, 4);");

        VBox content = (VBox) card.getChildren().get(0);
        content.setAlignment(Pos.CENTER); content.setSpacing(5);

        ImageView imgView = new ImageView(); imgView.setFitHeight(70); imgView.setFitWidth(70); imgView.setPreserveRatio(true);
        try { if(p.getImagePath() != null) imgView.setImage(new Image(new File(p.getImagePath()).toURI().toString())); } catch(Exception e){}
        StackPane imgBox = new StackPane(imgView); imgBox.setPrefSize(80, 80);

        Label nameLbl = new Label(p.getName()); nameLbl.setFont(Font.font("Arial", FontWeight.BOLD, 16)); nameLbl.setWrapText(true);

        Label priceLbl = new Label("₱" + String.format("%,.2f", p.getPrice()));
        priceLbl.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        priceLbl.setStyle("-fx-text-fill: black;"); // FORCE BLACK VIA CSS

        int stock = dataManager.getStockCount(p.getId());
        Label stockLbl = new Label("Stock: " + stock);
        stockLbl.setFont(Font.font("Monospaced", 14));
        stockLbl.setStyle("-fx-text-fill: black;"); // FORCE BLACK VIA CSS

        content.getChildren().addAll(imgBox, nameLbl, priceLbl, stockLbl);

        card.setOnMousePressed(e -> card.setStyle("-fx-background-color: #E0E0E0; -fx-border-color: black; -fx-border-width: 3; -fx-translate-y: 2;"));
        card.setOnMouseReleased(e -> {
            card.setStyle("-fx-background-color: " + COLOR_OFF_WHITE + "; -fx-border-color: black; -fx-border-width: 3; -fx-effect: dropshadow(one-pass-box, rgba(0,0,0,0.5), 4, 4, 4, 4);");
            performQuickSell(p);
        });
        return card;
    }

    // --- ADD & SELL MENU ---
    private void showAddAndSell() {
        isSellViewActive = false;
        VBox container = new VBox(30); container.setAlignment(Pos.CENTER); container.setMaxWidth(600);

        VBox titleCard = createSketchCard("TRANSACTION CENTER");
        Label sub = new Label("Choose an action below");
        sub.setFont(FONT_BODY_BOLD); sub.setTextFill(Color.GRAY);

        ((VBox)titleCard.getChildren().get(1)).getChildren().add(sub);

        Button btnAdd = createSketchButton("ADD", true); btnAdd.setPrefSize(200, 150); btnAdd.setOnAction(e -> showAddProductView());
        Button btnSell = createSketchButton("SELL", false); btnSell.setPrefSize(200, 150); btnSell.setOnAction(e -> showSellProductView());
        HBox actions = new HBox(30, btnAdd, btnSell); actions.setAlignment(Pos.CENTER);
        container.getChildren().addAll(titleCard, actions);
        setContent(container);
    }

    // --- ADD PRODUCT VIEW ---
    private void showAddProductView() {
        isSellViewActive = false;
        VBox container = new VBox(20); container.setAlignment(Pos.TOP_CENTER); container.setMaxWidth(600);
        Button btnBack = new Button("← GO BACK");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-cursor: hand;");
        btnBack.setOnAction(e -> showAddAndSell());

        VBox formCard = createSketchCard("Define New Product");
        VBox formContent = (VBox) formCard.getChildren().get(1);

        TextField nameField = createSketchInput("Product Name (e.g., Chips)");
        TextField priceField = createSketchInput("Price (e.g., 50.00)");
        TextField stockField = createSketchInput("Initial Stock (e.g., 10)");

        StackPane cameraContainer = new StackPane(); cameraContainer.setPrefSize(200, 150); cameraContainer.setStyle("-fx-background-color: black;");
        ImageView cameraView = new ImageView(); cameraView.setFitWidth(200); cameraView.setFitHeight(150); cameraView.imageProperty().bind(scannerService.imageProperty());
        cameraContainer.getChildren().add(cameraView);

        AtomicReference<String> capturedPath = new AtomicReference<>(null);
        Button captureBtn = createSketchButton("SNAP PHOTO", false);
        captureBtn.setOnAction(e -> {
            try {
                Image snapshot = cameraView.getImage();
                File out = new File("product_images/prod_" + System.currentTimeMillis() + ".png");
                out.getParentFile().mkdirs();
                ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", out);
                capturedPath.set(out.getAbsolutePath());
                cameraView.imageProperty().unbind(); cameraView.setImage(snapshot);
            } catch(Exception ex) { ex.printStackTrace(); }
        });

        Button saveBtn = createSketchButton("Save Product", true);
        saveBtn.setOnAction(e -> {
            try {
                String n = nameField.getText();
                double p = Double.parseDouble(priceField.getText());
                String i = capturedPath.get();
                int stock = 0;
                try { if(!stockField.getText().isEmpty()) stock = Integer.parseInt(stockField.getText()); } catch(Exception ex){}

                if(dataManager.createProductType(n, p, i)) {
                    if(stock > 0) {
                        for(int k=0; k<stock; k++) dataManager.addUniqueItem(n + "-" + System.currentTimeMillis() + "-" + k, n);
                    }
                    showAlert("Success", "Product Saved with " + stock + " stock!");
                    nameField.clear(); priceField.clear(); stockField.clear(); capturedPath.set(null);
                    cameraView.imageProperty().bind(scannerService.imageProperty());
                }
            } catch(Exception ex) { showAlert("Error", "Invalid Input"); }
        });

        formContent.getChildren().addAll(nameField, priceField, stockField, cameraContainer, captureBtn, saveBtn);
        container.getChildren().addAll(btnBack, formCard);
        setContent(container);
    }

    // --- EXPENSES ---
    private void showExpenses() {
        isSellViewActive = false;
        GridPane grid = new GridPane();
        grid.setHgap(30); grid.setAlignment(Pos.TOP_CENTER); grid.setMaxWidth(1000);
        ColumnConstraints col1 = new ColumnConstraints(); col1.setPercentWidth(40);
        ColumnConstraints col2 = new ColumnConstraints(); col2.setPercentWidth(60);
        grid.getColumnConstraints().addAll(col1, col2);

        VBox inputCard = createSketchCard("Add Expense");
        VBox inputContent = (VBox) inputCard.getChildren().get(1);
        TextField amountField = createSketchInput("Amount (" + "₱" + ")");
        TextField noteField = createSketchInput("Notes (e.g., Week 1)");
        Button addBtn = createSketchButton("Add Entry", true);
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setOnAction(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText());
                String note = noteField.getText();
                ExpenseRecord newRecord = new ExpenseRecord(note, amount, note);
                dataManager.addExpense(newRecord);
                showExpenses();
            } catch (NumberFormatException ex) { showAlert("Invalid Input", "Please enter a valid number."); }
        });
        inputContent.setSpacing(15);
        inputContent.getChildren().addAll(amountField, noteField, new Region(), addBtn);
        VBox.setVgrow(inputContent.getChildren().get(2), Priority.ALWAYS);

        VBox listCard = createSketchCard("Expense History");
        VBox listContent = (VBox) listCard.getChildren().get(1);
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true); scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox listItems = new VBox(10);
        List<ExpenseRecord> records = dataManager.loadExpenses();
        for (ExpenseRecord rec : records) {
            HBox item = new HBox();
            item.setStyle(STYLE_BG_OFF_WHITE + STYLE_BORDER_THICK);
            item.setPadding(new Insets(15)); item.setAlignment(Pos.CENTER_LEFT);
            VBox text = new VBox(2);
            Label w = new Label(rec.getWeek()); w.setFont(FONT_BODY_BOLD);
            Label n = new Label(rec.getNote()); n.setTextFill(Color.GRAY);
            text.getChildren().addAll(w, n);
            Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
            Label amt = new Label("-₱" + String.format("%.2f", rec.getAmount()));
            amt.setFont(FONT_HEADER); amt.setTextFill(Color.RED);
            item.getChildren().addAll(text, spacer, amt);
            listItems.getChildren().add(item);
        }
        scroll.setContent(listItems);
        listContent.getChildren().add(scroll);
        grid.add(inputCard, 0, 0); grid.add(listCard, 1, 0);
        setContent(grid);
    }

    // --- SALES ---
    private void showSales() {
        isSellViewActive = false;
        GridPane grid = new GridPane();
        grid.setHgap(30); grid.setAlignment(Pos.TOP_CENTER); grid.setMaxWidth(1000);
        ColumnConstraints col1 = new ColumnConstraints(); col1.setPercentWidth(35);
        ColumnConstraints col2 = new ColumnConstraints(); col2.setPercentWidth(65);
        grid.getColumnConstraints().addAll(col1, col2);

        VBox formCard = createSketchCard("New Sale");
        VBox formContent = (VBox) formCard.getChildren().get(1);
        barcodeInput = createSketchInput("Barcode / Product ID");
        TextField nameField = createSketchInput("Product Name");
        TextField priceField = createSketchInput("Price (Unit)");
        TextField qtyField = createSketchInput("Quantity");
        Button recordBtn = createSketchButton("Record Sale", true);
        recordBtn.setMaxWidth(Double.MAX_VALUE);
        recordBtn.setOnAction(e -> {
            try {
                String name = nameField.getText();
                double price = Double.parseDouble(priceField.getText());
                int qty = Integer.parseInt(qtyField.getText());
                ProductSale sale = new ProductSale(name, price, qty);
                dataManager.addSale(sale);
                nameField.clear(); priceField.clear(); qtyField.clear(); barcodeInput.clear();
                showSales();
            } catch (NumberFormatException ex) { showAlert("Input Error", "Check number fields."); }
        });
        formContent.setSpacing(15);
        formContent.getChildren().addAll(barcodeInput, nameField, priceField, qtyField, new Region(), recordBtn);
        VBox.setVgrow(formContent.getChildren().get(4), Priority.ALWAYS);

        VBox tableCard = createSketchCard("Sales Log");
        VBox tableContent = (VBox) tableCard.getChildren().get(1);
        TableView<ProductSale> table = new TableView<>();
        List<ProductSale> salesData = dataManager.loadSales();
        ObservableList<ProductSale> observableSales = FXCollections.observableArrayList(salesData);
        table.setItems(observableSales);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<ProductSale, String> nameCol = new TableColumn<>("PRODUCT");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<ProductSale, Double> priceCol = new TableColumn<>("PRICE");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        TableColumn<ProductSale, Integer> qtyCol = new TableColumn<>("QTY");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        TableColumn<ProductSale, Double> totalCol = new TableColumn<>("TOTAL");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("income"));
        table.getColumns().addAll(nameCol, priceCol, qtyCol, totalCol);
        table.setStyle("-fx-base: " + COLOR_OFF_WHITE + "; -fx-control-inner-background: " + COLOR_OFF_WHITE + "; -fx-table-cell-border-color: black; -fx-border-color: black; -fx-border-width: 2;");
        tableContent.getChildren().add(table);
        grid.add(formCard, 0, 0); grid.add(tableCard, 1, 0);
        setContent(grid);
    }

    // --- PROFIT ---
    private void showProfit() {
        isSellViewActive = false;
        VBox centerContainer = new VBox();
        centerContainer.setAlignment(Pos.CENTER); centerContainer.setMaxWidth(600);

        VBox card = createSketchCard("");
        card.setRotate(1);

        VBox content = (VBox) card.getChildren().get(0);
        content.setAlignment(Pos.CENTER); content.setPadding(new Insets(40));

        Label title = new Label("PROFIT SUMMARY");
        title.setFont(Font.font("Arial", FontWeight.BLACK, 36));
        double[] fins = dataManager.getFinancialSummary();
        VBox expenseBox = createSummaryRow("Total Expenses", fins[1], Color.RED, -1);
        VBox incomeBox = createSummaryRow("Total Income", fins[0], Color.web(COLOR_BLUE), 1);
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: black; -fx-border-color: black; -fx-border-width: 2;");
        sep.setPadding(new Insets(20, 0, 20, 0));
        VBox netBox = new VBox(5);
        netBox.setAlignment(Pos.CENTER);
        netBox.setStyle("-fx-background-color: black; -fx-padding: 20; -fx-effect: dropshadow(three-pass-box, " + COLOR_BLUE + ", 0, 0, 8, 8); -fx-rotate: -2;");
        Label netTitle = new Label("NET PROFIT");
        netTitle.setTextFill(Color.WHITE);
        netTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        Label netValue = new Label("₱" + String.format("%,.2f", fins[2]));
        netValue.setTextFill(Color.WHITE);
        netValue.setFont(Font.font("Monospaced", FontWeight.BOLD, 48));
        netBox.getChildren().addAll(netTitle, netValue);
        content.setSpacing(20);
        content.getChildren().addAll(title, new Region(), expenseBox, incomeBox, sep, netBox);
        centerContainer.getChildren().add(card);
        setContent(centerContainer);
    }

    // --- SIDEBAR & NAV ---
    private VBox createSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(280);
        sidebar.setStyle(STYLE_BG_OFF_WHITE + "-fx-border-color: black; -fx-border-width: 0 4 0 0;");

        VBox logoBox = new VBox();
        logoBox.setPadding(new Insets(30));
        logoBox.setStyle("-fx-background-color: " + COLOR_BLUE + "; -fx-border-color: black; -fx-border-width: 0 0 4 0;");
        Label logoText = new Label("STORE\nMANAGER");
        logoText.setTextFill(Color.WHITE);
        logoText.setFont(Font.font("Arial", FontWeight.BLACK, 32));
        logoText.setStyle("-fx-effect: dropshadow(one-pass-box, black, 0, 0, 2, 2);");
        logoBox.getChildren().add(logoText);

        VBox nav = new VBox();
        nav.getChildren().add(createNavButton("Dashboard", this::showDashboard));
        nav.getChildren().add(createNavButton("Add & Sell", this::showAddAndSell));
        nav.getChildren().add(createNavButton("Products", this::showProducts));
        nav.getChildren().add(createNavButton("Sales", this::showSales));
        nav.getChildren().add(createNavButton("Expenses", this::showExpenses));
        nav.getChildren().add(createNavButton("Profit", this::showProfit));

        Button btnLogout = createNavButton("Logout", () -> showLoginScreen(primaryStage));
        btnLogout.setStyle(btnLogout.getStyle() + "-fx-text-fill: red;");
        nav.getChildren().add(btnLogout);

        VBox.setVgrow(nav, Priority.ALWAYS);

        HBox status = new HBox(10);
        status.setAlignment(Pos.CENTER_LEFT);
        status.setPadding(new Insets(20));
        status.setStyle("-fx-background-color: black; -fx-border-color: black; -fx-border-width: 4 0 0 0;");
        Circle indicator = new Circle(6, Color.LIME);
        systemStatusLabel = new Label("System Online");
        systemStatusLabel.setTextFill(Color.WHITE);
        systemStatusLabel.setFont(FONT_BODY_BOLD);
        status.getChildren().addAll(indicator, systemStatusLabel);

        sidebar.getChildren().addAll(logoBox, nav, status);
        return sidebar;
    }

    private Button createNavButton(String text, Runnable action) {
        Button btn = new Button(text.toUpperCase());
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(20, 0, 20, 30));
        btn.setFont(Font.font("Arial", FontWeight.BLACK, 16));
        btn.setStyle("-fx-background-color: transparent; -fx-border-color: black; -fx-border-width: 0 0 4 0; -fx-cursor: hand;");

        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: white; -fx-padding: 20 0 20 40; -fx-border-color: black; -fx-border-width: 0 0 4 0;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-padding: 20 0 20 30; -fx-border-color: black; -fx-border-width: 0 0 4 0;"));
        btn.setOnAction(e -> action.run());

        return btn;
    }

    private HBox createHeaderBar() {
        HBox header = new HBox();
        header.setPadding(new Insets(20));
        header.setStyle(STYLE_BG_OFF_WHITE + "-fx-border-color: black; -fx-border-width: 0 0 4 0; -fx-effect: dropshadow(one-pass-box, rgba(0,0,0,1), 0, 0, 8, 8);");
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titles = new VBox();
        Label pageTitle = new Label("MANAGER DASHBOARD");
        pageTitle.setFont(FONT_TITLE);
        Label subTitle = new Label("Welcome back, User!");
        subTitle.setTextFill(Color.web(COLOR_BLUE));
        subTitle.setFont(FONT_BODY_BOLD);
        titles.getChildren().addAll(pageTitle, subTitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label dateBadge = new Label(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        dateBadge.setPadding(new Insets(10, 20, 10, 20));
        dateBadge.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-family: Monospaced; -fx-rotate: -2;");

        header.getChildren().addAll(titles, spacer, dateBadge);
        return header;
    }

    public VBox createSketchCard(String title) {
        VBox card = new VBox();
        card.setEffect(new DropShadow(0, 8, 8, Color.BLACK));
        card.setStyle(STYLE_BG_OFF_WHITE + STYLE_BORDER_THICK);

        if (!title.isEmpty()) {
            HBox header = new HBox();
            header.setPadding(new Insets(15));
            header.setStyle(STYLE_BG_BLUE + "-fx-border-color: black; -fx-border-width: 0 0 4 0;");
            Label titleLbl = new Label(title.toUpperCase());
            titleLbl.setTextFill(Color.WHITE);
            titleLbl.setFont(FONT_HEADER);
            header.getChildren().add(titleLbl);
            card.getChildren().add(header);
        }

        VBox body = new VBox();
        body.setPadding(new Insets(20));
        card.getChildren().add(body);
        return card;
    }

    private TextField createSketchInput(String label) {
        TextField tf = new TextField();
        tf.setPromptText(label);
        tf.setFont(FONT_BODY_BOLD);
        tf.setPadding(new Insets(15));
        tf.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-border-width: 4; -fx-prompt-text-fill: gray;");
        return tf;
    }

    private PasswordField createSketchPasswordField(String prompt) {
        PasswordField pf = new PasswordField();
        pf.setPromptText(prompt);
        pf.setFont(FONT_BODY_BOLD);
        pf.setPadding(new Insets(15));
        pf.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-border-width: 4; -fx-prompt-text-fill: gray;");
        return pf;
    }

    private Button createSketchButton(String text, boolean primary) {
        Button btn = new Button(text.toUpperCase());
        btn.setPadding(new Insets(15, 30, 15, 30));
        btn.setFont(FONT_HEADER);
        btn.setCursor(javafx.scene.Cursor.HAND);

        String baseStyle = STYLE_BORDER_THICK + "-fx-background-radius: 0; ";
        String colorStyle = primary
                ? "-fx-background-color: " + COLOR_BLUE + "; -fx-text-fill: white;"
                : "-fx-background-color: white; -fx-text-fill: black;";
        String shadowStyle = "-fx-effect: dropshadow(one-pass-box, rgba(0,0,0,1), 0, 0, 4, 4);";

        btn.setStyle(baseStyle + colorStyle + shadowStyle);

        btn.setOnMousePressed(e -> btn.setStyle(baseStyle + colorStyle + "-fx-translate-y: 4; -fx-translate-x: 4; -fx-effect: none;"));
        btn.setOnMouseReleased(e -> btn.setStyle(baseStyle + colorStyle + shadowStyle));

        return btn;
    }

    private VBox createSummaryRow(String label, double val, Color color, int rotateDir) {
        VBox box = new VBox(5);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-border-width: 4; -fx-rotate: " + rotateDir + "; " +
                "-fx-effect: dropshadow(one-pass-box, black, 0, 0, 4, 4);");

        Label l = new Label(label.toUpperCase());
        l.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        l.setTextFill(Color.GRAY);

        Label v = new Label("₱" + String.format("%,.2f", val));
        v.setFont(FONT_MONO);
        v.setTextFill(color);

        box.getChildren().addAll(l, v);
        return box;
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void setContent(Node node) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(node);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
