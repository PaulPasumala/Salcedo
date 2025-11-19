package com.businesscalculation13.monthlyprofitcalculatorfx;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MonthlyProfitCalculatorFX extends Application {

    // --- Design Constants ---
    private static final String COLOR_BLUE = "#1E56CD";
    private static final String COLOR_OFF_WHITE = "#FDF8F2";
    private static final String COLOR_BLACK = "#000000";

    private static final Font FONT_TITLE = Font.font("Arial", FontWeight.BLACK, 28);
    private static final Font FONT_HEADER = Font.font("Arial", FontWeight.BLACK, 20);
    private static final Font FONT_BODY_BOLD = Font.font("Arial", FontWeight.BOLD, 14);
    private static final Font FONT_MONO = Font.font("Monospaced", FontWeight.BOLD, 16);

    // --- CSS Styles ---
    private static final String STYLE_BORDER_THICK = "-fx-border-color: black; -fx-border-width: 4;";
    private static final String STYLE_BG_OFF_WHITE = "-fx-background-color: " + COLOR_OFF_WHITE + ";";
    private static final String STYLE_BG_BLUE = "-fx-background-color: " + COLOR_BLUE + ";";

    // --- Application State ---
    private final ObservableList<ExpenseRecord> expenseList = FXCollections.observableArrayList();
    private final ObservableList<ProductSale> salesList = FXCollections.observableArrayList();

    private Label totalSalesLabel;
    private Label netProfitLabel;
    private BorderPane mainLayout;
    private StackPane contentArea;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Retro Store Manager");

        // Main Layout Structure
        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #e5e5e5;"); // Outer background

        // 1. Create Sidebar
        VBox sidebar = createSidebar();
        mainLayout.setLeft(sidebar);

        // 2. Create Content Area (Right Side)
        VBox rightSide = new VBox();
        rightSide.setStyle(STYLE_BG_BLUE); // Blue background for the main area like the React app
        rightSide.setFillWidth(true);
        HBox.setHgrow(rightSide, Priority.ALWAYS);

        // Header Bar
        HBox headerBar = createHeaderBar();

        // Dynamic Content Container
        contentArea = new StackPane();
        contentArea.setPadding(new Insets(30));
        contentArea.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        rightSide.getChildren().addAll(headerBar, contentArea);
        mainLayout.setCenter(rightSide);

        // Initialize with Dashboard View
        showDashboard();

        Scene scene = new Scene(mainLayout, 1100, 700);
        stage.setScene(scene);
        stage.show();
    }

    // --- View Navigation Methods ---

    private void showDashboard() {
        GridPane dashboardGrid = new GridPane();
        dashboardGrid.setHgap(30);
        dashboardGrid.setVgap(30);
        dashboardGrid.setAlignment(Pos.TOP_CENTER);
        dashboardGrid.setMaxWidth(1000);

        // 1. Launch Card (Left Side)
        VBox launchCard = createSketchCard("Program Name");
        launchCard.setPrefHeight(300);

        Text desc = new Text("Manage your inventory, track weekly expenses, and calculate monthly profits with retro style.");
        desc.setFont(FONT_BODY_BOLD);
        desc.setWrappingWidth(400);

        // Progress Bar Simulation
        VBox progressContainer = new VBox(5);
        StackPane progressBar = new StackPane();
        Rectangle track = new Rectangle(400, 20, Color.rgb(0,0,0,0.1));
        track.setStroke(Color.BLACK);
        track.setStrokeWidth(2);
        track.setArcWidth(20); track.setArcHeight(20);

        Rectangle fill = new Rectangle(300, 20, Color.web(COLOR_BLUE)); // 75% fill
        fill.setArcWidth(20); fill.setArcHeight(20);
        StackPane.setAlignment(fill, Pos.CENTER_LEFT);

        progressBar.getChildren().addAll(track, fill);
        Label progressLabel = new Label("System Status: 75% Online");
        progressLabel.setFont(Font.font("Monospaced", 12));

        progressContainer.getChildren().addAll(desc, new Region(), progressBar, progressLabel);
        VBox.setVgrow(progressContainer.getChildren().get(1), Priority.ALWAYS); // Spacer

        Button launchBtn = createSketchButton("Launch Program", true);
        launchBtn.setMaxWidth(Double.MAX_VALUE);

        // Add content to card
        ((VBox)launchCard.getChildren().get(1)).getChildren().addAll(progressContainer, new Region(), launchBtn);
        ((VBox)launchCard.getChildren().get(1)).setSpacing(20);

        // 2. Stats Cards (Right Side)
        double totalSales = calculateTotalSales();
        double netProfit = calculateNetProfit();

        VBox salesStat = createStatCard("Weekly Sales", totalSales, false);
        VBox profitStat = createStatCard("Net Profit", netProfit, true);

        // Layout placement
        dashboardGrid.add(launchCard, 0, 0, 1, 2); // Spans 2 rows
        dashboardGrid.add(salesStat, 1, 0);
        dashboardGrid.add(profitStat, 1, 1);

        // Column Constraints
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(60);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(40);
        dashboardGrid.getColumnConstraints().addAll(col1, col2);

        setContent(dashboardGrid);
    }

    private void showExpenses() {
        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setAlignment(Pos.TOP_CENTER);
        grid.setMaxWidth(1000);

        ColumnConstraints col1 = new ColumnConstraints(); col1.setPercentWidth(40);
        ColumnConstraints col2 = new ColumnConstraints(); col2.setPercentWidth(60);
        grid.getColumnConstraints().addAll(col1, col2);

        // 1. Input Form
        VBox inputCard = createSketchCard("Add Weekly Expense");
        VBox inputContent = (VBox) inputCard.getChildren().get(1);

        // Week Indicator
        Label weekLabel = new Label("WEEK " + (expenseList.size() + 1));
        weekLabel.setStyle("-fx-background-color: #DBEAFE; -fx-text-fill: " + COLOR_BLUE + "; -fx-border-color: " + COLOR_BLUE + "; -fx-border-style: dashed; -fx-border-width: 2; -fx-padding: 10;");
        weekLabel.setFont(FONT_HEADER);
        weekLabel.setMaxWidth(Double.MAX_VALUE);
        weekLabel.setAlignment(Pos.CENTER);

        TextField amountField = createSketchInput("Amount (" + "₱" + ")");
        TextField noteField = createSketchInput("Notes");

        Button addBtn = createSketchButton("Add Entry", true);
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setOnAction(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText());
                expenseList.add(new ExpenseRecord("Week " + (expenseList.size() + 1), amount, noteField.getText()));
                amountField.clear();
                noteField.clear();
                showExpenses(); // Refresh to update week count
            } catch (NumberFormatException ex) {
                showAlert("Invalid Input", "Please enter a valid number.");
            }
        });

        inputContent.setSpacing(15);
        inputContent.getChildren().addAll(weekLabel, amountField, noteField, new Region(), addBtn);
        VBox.setVgrow(inputContent.getChildren().get(3), Priority.ALWAYS);

        // 2. History List
        VBox listCard = createSketchCard("Expense History");
        VBox listContent = (VBox) listCard.getChildren().get(1);
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox listItems = new VBox(10);
        for (ExpenseRecord rec : expenseList) {
            HBox item = new HBox();
            item.setStyle(STYLE_BG_OFF_WHITE + STYLE_BORDER_THICK);
            item.setPadding(new Insets(15));
            item.setAlignment(Pos.CENTER_LEFT);

            VBox text = new VBox(2);
            Label w = new Label(rec.week); w.setFont(FONT_BODY_BOLD);
            Label n = new Label(rec.note.isEmpty() ? "Logged manually" : rec.note); n.setTextFill(Color.GRAY);
            text.getChildren().addAll(w, n);

            Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

            Label amt = new Label("-₱" + String.format("%.2f", rec.amount));
            amt.setFont(FONT_HEADER);
            amt.setTextFill(Color.RED);

            item.getChildren().addAll(text, spacer, amt);

            // Hover effect for list items
            item.setOnMouseEntered(e -> item.setStyle(STYLE_BG_OFF_WHITE + STYLE_BORDER_THICK + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,1), 0, 0, 4, 4);"));
            item.setOnMouseExited(e -> item.setStyle(STYLE_BG_OFF_WHITE + STYLE_BORDER_THICK));

            listItems.getChildren().add(item);
        }
        scroll.setContent(listItems);
        listContent.getChildren().add(scroll);

        grid.add(inputCard, 0, 0);
        grid.add(listCard, 1, 0);

        setContent(grid);
    }

    private void showSales() {
        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setAlignment(Pos.TOP_CENTER);
        grid.setMaxWidth(1000);

        ColumnConstraints col1 = new ColumnConstraints(); col1.setPercentWidth(35);
        ColumnConstraints col2 = new ColumnConstraints(); col2.setPercentWidth(65);
        grid.getColumnConstraints().addAll(col1, col2);

        // 1. New Sale Form
        VBox formCard = createSketchCard("New Sale");
        VBox formContent = (VBox) formCard.getChildren().get(1);

        TextField nameField = createSketchInput("Product Name");
        TextField priceField = createSketchInput("Value (Unit Price)");
        TextField qtyField = createSketchInput("Sold Qty");

        Button recordBtn = createSketchButton("Record Sale", true);
        recordBtn.setMaxWidth(Double.MAX_VALUE);
        recordBtn.setOnAction(e -> {
            try {
                String name = nameField.getText();
                double price = Double.parseDouble(priceField.getText());
                int qty = Integer.parseInt(qtyField.getText());
                salesList.add(new ProductSale(name, price, qty));
                nameField.clear(); priceField.clear(); qtyField.clear();
                showSales(); // Refresh table
            } catch (NumberFormatException ex) {
                showAlert("Input Error", "Check your number fields.");
            }
        });

        formContent.setSpacing(15);
        formContent.getChildren().addAll(nameField, priceField, qtyField, new Region(), recordBtn);
        VBox.setVgrow(formContent.getChildren().get(3), Priority.ALWAYS);

        // 2. Sales Table
        VBox tableCard = createSketchCard("Sales Log");
        VBox tableContent = (VBox) tableCard.getChildren().get(1);

        TableView<ProductSale> table = new TableView<>();
        table.setItems(salesList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-border-width: 2;");

        TableColumn<ProductSale, String> nameCol = new TableColumn<>("PRODUCT");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<ProductSale, Double> priceCol = new TableColumn<>("PRICE");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));

        TableColumn<ProductSale, Integer> qtyCol = new TableColumn<>("QTY");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<ProductSale, Double> totalCol = new TableColumn<>("TOTAL");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("income"));

        table.getColumns().addAll(nameCol, priceCol, qtyCol, totalCol);

        // Simple Table Styling via code roughly
        table.setStyle("-fx-base: " + COLOR_OFF_WHITE + "; -fx-control-inner-background: " + COLOR_OFF_WHITE + "; -fx-table-cell-border-color: black;");

        tableContent.getChildren().add(table);

        grid.add(formCard, 0, 0);
        grid.add(tableCard, 1, 0);

        setContent(grid);
    }

    private void showProfit() {
        VBox centerContainer = new VBox();
        centerContainer.setAlignment(Pos.CENTER);
        centerContainer.setMaxWidth(600);

        // Decorative Card
        VBox card = createSketchCard("");
        // Rotate the card slightly for style
        card.setRotate(1);

        VBox content = (VBox) card.getChildren().get(1);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(40));

        Label title = new Label("PROFIT SUMMARY");
        title.setFont(Font.font("Arial", FontWeight.BLACK, 36));

        VBox expenseBox = createSummaryRow("Total Expenses", calculateTotalExpenses(), Color.RED, -1);
        VBox incomeBox = createSummaryRow("Total Income", calculateTotalSales(), Color.web(COLOR_BLUE), 1);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: black; -fx-border-color: black; -fx-border-width: 2;");
        sep.setPadding(new Insets(20, 0, 20, 0));

        double net = calculateNetProfit();
        VBox netBox = new VBox(5);
        netBox.setAlignment(Pos.CENTER);
        netBox.setStyle("-fx-background-color: black; -fx-padding: 20; -fx-effect: dropshadow(three-pass-box, " + COLOR_BLUE + ", 0, 0, 8, 8); -fx-rotate: -2;");

        Label netTitle = new Label("NET PROFIT");
        netTitle.setTextFill(Color.WHITE);
        netTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        Label netValue = new Label("₱" + String.format("%,.2f", net));
        netValue.setTextFill(Color.WHITE);
        netValue.setFont(Font.font("Monospaced", FontWeight.BOLD, 48));

        netBox.getChildren().addAll(netTitle, netValue);

        content.setSpacing(20);
        content.getChildren().addAll(title, new Region(), expenseBox, incomeBox, sep, netBox);

        centerContainer.getChildren().add(card);
        setContent(centerContainer);
    }

    // --- Component Builders (The Retro Design System) ---

    private VBox createSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(280);
        sidebar.setStyle(STYLE_BG_OFF_WHITE + "-fx-border-color: black; -fx-border-width: 0 4 0 0;");

        // Logo Area
        VBox logoBox = new VBox();
        logoBox.setPadding(new Insets(30));
        logoBox.setStyle("-fx-background-color: " + COLOR_BLUE + "; -fx-border-color: black; -fx-border-width: 0 0 4 0;");
        Label logoText = new Label("STORE\nMANAGER");
        logoText.setTextFill(Color.WHITE);
        logoText.setFont(Font.font("Arial", FontWeight.BLACK, 32));
        logoText.setStyle("-fx-effect: dropshadow(one-pass-box, black, 0, 0, 2, 2);"); // Text shadow
        logoBox.getChildren().add(logoText);

        // Nav Items
        VBox nav = new VBox();
        nav.getChildren().add(createNavButton("Dashboard", this::showDashboard));
        nav.getChildren().add(createNavButton("Expenses", this::showExpenses));
        nav.getChildren().add(createNavButton("Sales", this::showSales));
        nav.getChildren().add(createNavButton("Profit", this::showProfit));
        VBox.setVgrow(nav, Priority.ALWAYS);

        // Bottom Status
        HBox status = new HBox(10);
        status.setAlignment(Pos.CENTER_LEFT);
        status.setPadding(new Insets(20));
        status.setStyle("-fx-background-color: black; -fx-border-color: black; -fx-border-width: 4 0 0 0;");
        Circle indicator = new Circle(6, Color.LIME);
        Label statusText = new Label("System Online");
        statusText.setTextFill(Color.WHITE);
        statusText.setFont(FONT_BODY_BOLD);
        status.getChildren().addAll(indicator, statusText);

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

        // Hover and Active states logic would go here, simplified for JavaFX standard
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
        Label pageTitle = new Label("DASHBOARD"); // Dynamic in full app
        pageTitle.setFont(FONT_TITLE);
        Label subTitle = new Label("Welcome back, Admin!");
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

    /**
     * Creates a "Sketch" Card with header and wavy decoration.
     */
    private VBox createSketchCard(String title) {
        VBox card = new VBox();
        // Hard shadow effect
        DropShadow hardShadow = new DropShadow(0, 8, 8, Color.BLACK);
        card.setEffect(hardShadow);
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
        // Wavy pattern at bottom
        Pane wavy = new Pane();
        wavy.setPrefHeight(20);
        // (Wavy SVG path drawing omitted for brevity, simplistic line used)

        card.getChildren().add(body);
        return card;
    }

    private TextField createSketchInput(String label) {
        TextField tf = new TextField();
        tf.setPromptText(label);
        tf.setFont(FONT_BODY_BOLD);
        tf.setPadding(new Insets(15));
        tf.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-border-width: 4; -fx-prompt-text-fill: gray;");

        // Corner accent logic requires a StackPane wrapper, simplifying to just simple border for file conciseness
        return tf;
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

    private VBox createStatCard(String title, double value, boolean isProfit) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setStyle(STYLE_BG_OFF_WHITE + STYLE_BORDER_THICK + "-fx-effect: dropshadow(one-pass-box, rgba(0,0,0,1), 0, 0, 8, 8);");

        Label t = new Label(title.toUpperCase());
        t.setFont(FONT_HEADER);

        Label v = new Label("₱" + String.format("%,.2f", value));
        v.setFont(Font.font("Monospaced", FontWeight.BLACK, 36));
        v.setTextFill(isProfit ? (value >= 0 ? Color.GREEN : Color.RED) : Color.web(COLOR_BLUE));

        card.getChildren().addAll(t, v);

        // Hover effect
        card.setOnMouseEntered(e -> card.setTranslateY(-5));
        card.setOnMouseExited(e -> card.setTranslateY(0));

        return card;
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
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void setContent(Node node) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(node);
    }

    // --- Calculation Helpers ---

    private double calculateTotalExpenses() {
        return expenseList.stream().mapToDouble(r -> r.amount).sum();
    }

    private double calculateTotalSales() {
        return salesList.stream().mapToDouble(s -> s.income).sum();
    }

    private double calculateNetProfit() {
        return calculateTotalSales() - calculateTotalExpenses();
    }

    public static void main(String[] args) {
        launch(args);
    }

    // --- Data Classes ---

    public static class ExpenseRecord {
        String week;
        double amount;
        String note;

        public ExpenseRecord(String week, double amount, String note) {
            this.week = week;
            this.amount = amount;
            this.note = note;
        }
    }

    public static class ProductSale {
        String name;
        double unitPrice;
        int quantity;
        double income;

        public ProductSale(String name, double unitPrice, int quantity) {
            this.name = name;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
            this.income = unitPrice * quantity;
        }

        // Getters needed for TableView PropertyValueFactory
        public String getName() { return name; }
        public double getUnitPrice() { return unitPrice; }
        public int getQuantity() { return quantity; }
        public double getIncome() { return income; }
    }
}