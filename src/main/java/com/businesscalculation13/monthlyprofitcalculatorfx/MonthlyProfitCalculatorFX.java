package com.businesscalculation13.monthlyprofitcalculatorfx;

import javafx.application.Application;
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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MonthlyProfitCalculatorFX extends Application {

    // --- Data State ---
    private final ObservableList<ExpenseRecord> expenseList = FXCollections.observableArrayList();
    private final ObservableList<ProductSale> salesList = FXCollections.observableArrayList();
    private final DataManager dataManager = new DataManager();

    // --- Colors & Fonts ---
    private static final String COL_BLUE = "#1E56CD";
    private static final String COL_OFF_WHITE = "#FDF8F2";
    private static final Font FONT_TITLE = Font.font("Arial", FontWeight.BLACK, 24);

    // --- UI Components ---
    private Stage primaryStage;
    private Scene dashboardScene;
    private StackPane contentArea;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("Retro Store Manager - Login");

        // 1. Show Login Screen First
        showLoginScreen();
        stage.show();
    }

    // --- LOGIN SYSTEM ---

    private void showLoginScreen() {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: " + COL_BLUE + ";");

        VBox card = createCard("SECURE LOGIN");
        card.setMaxWidth(400);
        VBox content = (VBox) card.getChildren().get(1);

        TextField userField = createInput("Username");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");
        passField.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-border-width: 4; -fx-padding: 15; -fx-font-weight: bold;");

        Button loginBtn = createButton("Login", true);
        Button registerBtn = createButton("Create Account", false);

        Label statusLabel = new Label("");
        statusLabel.setTextFill(Color.RED);

        loginBtn.setOnAction(e -> {
            if (dataManager.validateLogin(userField.getText(), passField.getText())) {
                loadDashboardData(); // Load data only after login
                initializeDashboard(); // Switch to main app
            } else {
                statusLabel.setText("Invalid Credentials!");
            }
        });

        registerBtn.setOnAction(e -> {
            if (userField.getText().isEmpty() || passField.getText().isEmpty()) {
                statusLabel.setText("Fields cannot be empty!");
                return;
            }
            boolean success = dataManager.registerUser(userField.getText(), passField.getText());
            if (success) {
                statusLabel.setText("Account Created! Please Login.");
                statusLabel.setTextFill(Color.GREEN);
            } else {
                statusLabel.setText("Username already taken!");
                statusLabel.setTextFill(Color.RED);
            }
        });

        content.setSpacing(15);
        content.getChildren().addAll(new Label("Enter Credentials:"), userField, passField, loginBtn, registerBtn, statusLabel);

        layout.getChildren().add(card);
        Scene loginScene = new Scene(layout, 1100, 700);
        primaryStage.setScene(loginScene);
    }

    // --- DASHBOARD SYSTEM ---

    private void loadDashboardData() {
        expenseList.clear();
        salesList.clear();
        expenseList.addAll(dataManager.loadExpenses());
        salesList.addAll(dataManager.loadSales());
    }

    private void initializeDashboard() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #e5e5e5;");

        VBox sidebar = createSidebar();
        mainLayout.setLeft(sidebar);

        VBox rightSide = new VBox();
        rightSide.setStyle("-fx-background-color: " + COL_BLUE + ";");
        rightSide.setFillWidth(true);
        HBox.setHgrow(rightSide, Priority.ALWAYS);

        HBox headerBar = createHeaderBar();

        contentArea = new StackPane();
        contentArea.setPadding(new Insets(30));
        contentArea.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        rightSide.getChildren().addAll(headerBar, contentArea);
        mainLayout.setCenter(rightSide);

        showDashboardView();

        dashboardScene = new Scene(mainLayout, 1100, 700);
        primaryStage.setTitle("Retro Store Manager - Dashboard");
        primaryStage.setScene(dashboardScene);
    }

    // --- Views ---

    private void showDashboardView() {
        GridPane grid = new GridPane();
        grid.setHgap(30); grid.setVgap(30);
        grid.setAlignment(Pos.TOP_CENTER);
        grid.setMaxWidth(1000);

        double totalSales = salesList.stream().mapToDouble(ProductSale::getIncome).sum();
        double totalExpenses = expenseList.stream().mapToDouble(ExpenseRecord::getAmount).sum();
        double netProfit = totalSales - totalExpenses;

        VBox welcomeCard = createCard("System Status");
        Text desc = new Text("Connected to MySQL Database.\nUser Authenticated.");
        desc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        ((VBox)welcomeCard.getChildren().get(1)).getChildren().add(desc);

        VBox salesStat = createStatCard("Total Sales", totalSales, false);
        VBox profitStat = createStatCard("Net Profit", netProfit, true);

        grid.add(welcomeCard, 0, 0);
        grid.add(salesStat, 1, 0);
        grid.add(profitStat, 1, 1);

        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(60);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(40);
        grid.getColumnConstraints().addAll(c1, c2);

        setContent(grid);
    }

    private void showExpensesView() {
        GridPane grid = new GridPane();
        grid.setHgap(30); grid.setAlignment(Pos.TOP_CENTER); grid.setMaxWidth(1000);
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(40);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(60);
        grid.getColumnConstraints().addAll(c1, c2);

        VBox inputCard = createCard("Add Expense");
        TextField amountField = createInput("Amount");
        TextField noteField = createInput("Note");
        Button addBtn = createButton("Add Entry", true);

        addBtn.setOnAction(e -> {
            try {
                double val = Double.parseDouble(amountField.getText());
                ExpenseRecord r = new ExpenseRecord("Week " + (expenseList.size()+1), val, noteField.getText());
                dataManager.addExpense(r); // Save to DB
                expenseList.add(r); // Update UI
                amountField.clear(); noteField.clear();
            } catch (NumberFormatException ex) {
                showAlert("Invalid Number");
            }
        });

        VBox formBox = (VBox)inputCard.getChildren().get(1);
        formBox.setSpacing(15);
        formBox.getChildren().addAll(amountField, noteField, addBtn);

        VBox listCard = createCard("History");
        ListView<ExpenseRecord> list = new ListView<>(expenseList);
        list.setStyle("-fx-font-family: 'Monospaced'; -fx-control-inner-background: #FDF8F2;");
        ((VBox)listCard.getChildren().get(1)).getChildren().add(list);

        grid.add(inputCard, 0, 0);
        grid.add(listCard, 1, 0);
        setContent(grid);
    }

    private void showSalesView() {
        GridPane grid = new GridPane();
        grid.setHgap(30); grid.setAlignment(Pos.TOP_CENTER); grid.setMaxWidth(1000);
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(30);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(70);
        grid.getColumnConstraints().addAll(c1, c2);

        VBox formCard = createCard("New Sale");
        TextField nameF = createInput("Product");
        TextField priceF = createInput("Price");
        TextField qtyF = createInput("Qty");
        Button btn = createButton("Record", true);

        btn.setOnAction(e -> {
            try {
                String n = nameF.getText();
                double p = Double.parseDouble(priceF.getText());
                int q = Integer.parseInt(qtyF.getText());
                ProductSale s = new ProductSale(n, p, q);
                dataManager.addSale(s); // Save to DB
                salesList.add(s); // Update UI
                nameF.clear(); priceF.clear(); qtyF.clear();
            } catch (Exception ex) { showAlert("Input Error"); }
        });

        VBox formBox = (VBox)formCard.getChildren().get(1);
        formBox.setSpacing(15);
        formBox.getChildren().addAll(nameF, priceF, qtyF, btn);

        VBox tableCard = createCard("Sales Log");
        TableView<ProductSale> table = new TableView<>(salesList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ProductSale, String> tc1 = new TableColumn<>("Item"); tc1.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<ProductSale, Double> tc2 = new TableColumn<>("Price"); tc2.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        TableColumn<ProductSale, Integer> tc3 = new TableColumn<>("Qty"); tc3.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        TableColumn<ProductSale, Double> tc4 = new TableColumn<>("Total"); tc4.setCellValueFactory(new PropertyValueFactory<>("income"));

        table.getColumns().addAll(tc1, tc2, tc3, tc4);
        ((VBox)tableCard.getChildren().get(1)).getChildren().add(table);

        grid.add(formCard, 0, 0);
        grid.add(tableCard, 1, 0);
        setContent(grid);
    }

    // --- Standard Helpers (Same as before) ---

    private VBox createSidebar() {
        VBox box = new VBox();
        box.setPrefWidth(280);
        box.setStyle("-fx-background-color: " + COL_OFF_WHITE + "; -fx-border-color: black; -fx-border-width: 0 4 0 0;");

        VBox logo = new VBox();
        logo.setPadding(new Insets(30));
        logo.setStyle("-fx-background-color: " + COL_BLUE + "; -fx-border-color: black; -fx-border-width: 0 0 4 0;");
        Label l = new Label("STORE\nMANAGER");
        l.setTextFill(Color.WHITE);
        l.setFont(Font.font("Arial", FontWeight.BLACK, 32));
        logo.getChildren().add(l);

        VBox nav = new VBox();
        nav.getChildren().addAll(createNavBtn("Dashboard", this::showDashboardView), createNavBtn("Expenses", this::showExpensesView), createNavBtn("Sales", this::showSalesView), createNavBtn("Logout", this::showLoginScreen));
        box.getChildren().addAll(logo, nav);
        return box;
    }

    private Button createNavBtn(String text, Runnable action) {
        Button b = new Button(text.toUpperCase());
        b.setMaxWidth(Double.MAX_VALUE);
        b.setAlignment(Pos.CENTER_LEFT);
        b.setPadding(new Insets(20, 0, 20, 30));
        b.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        String base = "-fx-background-color: transparent; -fx-border-color: black; -fx-border-width: 0 0 4 0; -fx-cursor: hand;";
        b.setStyle(base);
        b.setOnMouseEntered(e -> b.setStyle("-fx-background-color: white; -fx-padding: 20 0 20 40; -fx-border-color: black; -fx-border-width: 0 0 4 0;"));
        b.setOnMouseExited(e -> b.setStyle(base));
        b.setOnAction(e -> action.run());
        return b;
    }

    private HBox createHeaderBar() {
        HBox h = new HBox();
        h.setPadding(new Insets(20));
        h.setStyle("-fx-background-color: " + COL_OFF_WHITE + "; -fx-border-color: black; -fx-border-width: 0 0 4 0;");
        h.setEffect(new DropShadow(8, Color.BLACK));
        VBox t = new VBox();
        Label title = new Label("DASHBOARD"); title.setFont(FONT_TITLE);
        Label sub = new Label("Authorized Access"); sub.setTextFill(Color.web(COL_BLUE)); sub.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        t.getChildren().addAll(title, sub);
        Region r = new Region(); HBox.setHgrow(r, Priority.ALWAYS);
        Label d = new Label(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        d.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-padding: 10 20; -fx-rotate: -2; -fx-font-weight: bold;");
        h.getChildren().addAll(t, r, d);
        return h;
    }

    private VBox createCard(String title) {
        VBox c = new VBox();
        c.setEffect(new DropShadow(0, 8, 8, Color.BLACK));
        c.setStyle("-fx-background-color: " + COL_OFF_WHITE + "; -fx-border-color: black; -fx-border-width: 4;");
        if (!title.isEmpty()) {
            HBox h = new HBox();
            h.setPadding(new Insets(15));
            h.setStyle("-fx-background-color: " + COL_BLUE + "; -fx-border-color: black; -fx-border-width: 0 0 4 0;");
            Label l = new Label(title.toUpperCase()); l.setTextFill(Color.WHITE); l.setFont(Font.font("Arial", FontWeight.BLACK, 18));
            h.getChildren().add(l);
            c.getChildren().add(h);
        }
        VBox b = new VBox();
        b.setPadding(new Insets(20));
        c.getChildren().add(b);
        return c;
    }

    private TextField createInput(String prompt) {
        TextField t = new TextField();
        t.setPromptText(prompt);
        t.setPadding(new Insets(15));
        t.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        t.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-border-width: 4; -fx-background-radius: 0;");
        return t;
    }

    private Button createButton(String text, boolean primary) {
        Button b = new Button(text.toUpperCase());
        b.setPadding(new Insets(15, 30, 15, 30));
        b.setFont(Font.font("Arial", FontWeight.BLACK, 14));
        b.setCursor(javafx.scene.Cursor.HAND);
        String common = "-fx-border-color: black; -fx-border-width: 4; -fx-background-radius: 0; ";
        String col = primary ? "-fx-background-color: " + COL_BLUE + "; -fx-text-fill: white;" : "-fx-background-color: white; -fx-text-fill: black;";
        b.setStyle(common + col + "-fx-effect: dropshadow(one-pass-box, rgba(0,0,0,1), 4, 4, 0, 0);");
        b.setOnMousePressed(e -> b.setStyle(common + col + "-fx-translate-y: 4; -fx-translate-x: 4; -fx-effect: none;"));
        b.setOnMouseReleased(e -> b.setStyle(common + col + "-fx-effect: dropshadow(one-pass-box, rgba(0,0,0,1), 4, 4, 0, 0);"));
        return b;
    }

    private VBox createStatCard(String title, double val, boolean isProfit) {
        VBox c = createCard("");
        Label t = new Label(title.toUpperCase()); t.setFont(Font.font("Arial", FontWeight.BLACK, 16));
        Label v = new Label("₱" + String.format("%,.2f", val));
        v.setFont(Font.font("Monospaced", FontWeight.BOLD, 32));
        v.setTextFill(isProfit ? (val >= 0 ? Color.GREEN : Color.RED) : Color.web(COL_BLUE));
        ((VBox)c.getChildren().get(0)).getChildren().addAll(t, v);
        return c;
    }

    private void setContent(Node n) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(n);
    }

    private void showAlert(String m) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setContentText(m);
        a.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}