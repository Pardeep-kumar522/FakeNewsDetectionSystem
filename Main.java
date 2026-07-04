package application;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import javafx.animation.*;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {

    private Article[] articles;
    private int articleCount;
    private static final int MAX_ARTICLES = 1000;
    private HashMap<String, FakeSource> fakeSources;
    private ArrayList<User> users;
    private ArrayList<Admin> admins;

    private User currentUser;
    private Admin currentAdmin;
    private String currentRole;

    private TableView<Article> articleTable;
    private ObservableList<Article> articleData;
    private Label statusLabel;
    private Label internetStatusLabel;
    private ProgressIndicator verificationProgress;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        articles     = new Article[MAX_ARTICLES];
        articleCount = 0;
        fakeSources  = new HashMap<>();
        users        = new ArrayList<>();
        admins       = new ArrayList<>();

        loadSampleData();
        showLoginScreen();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  LOGIN SCREEN
    // ─────────────────────────────────────────────────────────────────────────
    private void showLoginScreen() {
        Stage loginStage = new Stage();
        loginStage.setTitle("Internet Fake News Detection - Login");
        loginStage.setResizable(true);
        loginStage.setMinWidth(420);
        loginStage.setMinHeight(560);

        VBox loginBox = new VBox(20);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setPadding(new Insets(40));
        loginBox.setStyle("-fx-background-color: linear-gradient(to bottom, #1a1a2e, #16213e);");

        Label titleLabel = new Label("🌐 INTERNET-BASED FAKE NEWS DETECTION");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setWrapText(true);

        Label subtitleLabel = new Label("Real-time verification via DuckDuckGo · Wikipedia · Domain Database");
        subtitleLabel.setFont(Font.font("Segoe UI", 12));
        subtitleLabel.setTextFill(Color.web("#cccccc"));
        subtitleLabel.setWrapText(true);

        ToggleGroup roleGroup = new ToggleGroup();
        RadioButton userRadio  = new RadioButton("User");
        RadioButton adminRadio = new RadioButton("Admin");
        userRadio.setToggleGroup(roleGroup);
        adminRadio.setToggleGroup(roleGroup);
        userRadio.setSelected(true);
        userRadio.setTextFill(Color.WHITE);
        adminRadio.setTextFill(Color.WHITE);

        HBox roleBox = new HBox(20);
        roleBox.setAlignment(Pos.CENTER);
        roleBox.getChildren().addAll(userRadio, adminRadio);

        TextField idField = new TextField();
        idField.setPromptText("Enter ID");
        idField.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");
        idField.setPrefWidth(280);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");
        passwordField.setPrefWidth(280);

        TextField nameField = new TextField();
        nameField.setPromptText("Enter Name (for new users)");
        nameField.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");
        nameField.setPrefWidth(280);
        nameField.setVisible(false);
        nameField.setManaged(false);

        CheckBox newUserCheck = new CheckBox("New User? Register");
        newUserCheck.setTextFill(Color.WHITE);
        newUserCheck.setOnAction(e -> {
            boolean reg = newUserCheck.isSelected();
            nameField.setVisible(reg);
            nameField.setManaged(reg);
            passwordField.setPromptText(reg ? "Create Password" : "Enter Password");
        });

        Button loginBtn = new Button("Login / Register");
        loginBtn.setStyle(
            "-fx-background-color: #28a745; -fx-text-fill: white; " +
            "-fx-font-size: 14px; -fx-font-weight: bold; " +
            "-fx-padding: 10 20; -fx-cursor: hand;");
        loginBtn.setPrefWidth(220);

        loginBtn.setOnAction(e -> {
            String role     = userRadio.isSelected() ? "user" : "admin";
            String idText   = idField.getText().trim();
            String password = passwordField.getText();

            if (idText.isEmpty()) {
                showAlert("Error", "Please enter ID!", Alert.AlertType.ERROR);
                return;
            }
            try {
                int id = Integer.parseInt(idText);

                if (role.equals("user")) {
                    if (newUserCheck.isSelected()) {
                        String name = nameField.getText().trim();
                        if (name.isEmpty() || password.isEmpty()) {
                            showAlert("Error", "Please enter name and password!", Alert.AlertType.ERROR);
                            return;
                        }
                        User newUser = new User(id, name, password);
                        users.add(newUser);
                        currentUser = newUser;
                        currentRole = "user";
                        showAlert("Success", "Registration successful!", Alert.AlertType.INFORMATION);
                        loginStage.close();
                        startMainApp();
                    } else {
                        User user = findUserById(id);
                        if (user != null && user.getPassword().equals(password)) {
                            currentUser = user;
                            currentRole = "user";
                            loginStage.close();
                            startMainApp();
                        } else {
                            showAlert("Error", "Invalid credentials!", Alert.AlertType.ERROR);
                        }
                    }
                } else {
                    Admin admin = findAdminById(id);
                    if (admin != null && admin.getPassword().equals(password)) {
                        currentAdmin = admin;
                        currentRole  = "admin";
                        loginStage.close();
                        startMainApp();
                    } else {
                        showAlert("Error",
                            "Invalid admin credentials!\nDefault: ID=1, Password=admin123",
                            Alert.AlertType.ERROR);
                    }
                }
            } catch (NumberFormatException ex) {
                showAlert("Error", "ID must be a number!", Alert.AlertType.ERROR);
            }
        });

        Label hintLabel = new Label("Default User → ID: 1  Password: user123\nDefault Admin → ID: 1  Password: admin123");
        hintLabel.setFont(Font.font("Segoe UI", 11));
        hintLabel.setTextFill(Color.web("#aaaaaa"));
        hintLabel.setAlignment(Pos.CENTER);

        loginBox.getChildren().addAll(
            titleLabel, subtitleLabel, roleBox,
            idField, passwordField,
            newUserCheck, nameField,
            loginBtn, hintLabel
        );

        ScrollPane sp = new ScrollPane(loginBox);
        sp.setFitToWidth(true);
        sp.setFitToHeight(true);
        sp.setStyle("-fx-background-color: #1a1a2e;");

        Scene scene = new Scene(sp, 480, 620);
        loginStage.setScene(scene);
        loginStage.show();
        loginStage.centerOnScreen();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  MAIN APP
    // ─────────────────────────────────────────────────────────────────────────
    private void startMainApp() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");
        root.setTop(createHeader());
        root.setCenter(createTabPane());
        root.setBottom(createFooter());

        Scene scene = new Scene(root, 1400, 850);
        primaryStage.setTitle("INTERNET-BASED FAKE NEWS DETECTION - " +
            (currentRole.equals("admin") ? "ADMIN" : "USER"));
        primaryStage.setResizable(true);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.centerOnScreen();

        updateStatus();
        testInternetConnection();
    }

    private void testInternetConnection() {
        new Thread(() -> {
            try {
                URL url = new URL("https://www.google.com");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(3000);
                conn.connect();
                if (conn.getResponseCode() == 200) {
                    javafx.application.Platform.runLater(() -> {
                        internetStatusLabel.setText("🌐 Internet Connected - Real-time APIs Active");
                        internetStatusLabel.setTextFill(Color.GREEN);
                    });
                }
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    internetStatusLabel.setText("⚠️ No Internet Connection - Using Local Database Only");
                    internetStatusLabel.setTextFill(Color.RED);
                });
            }
        }).start();
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setStyle("-fx-background-color: linear-gradient(to right, #1a1a2e, #16213e);");
        header.setPadding(new Insets(20));
        header.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("🌐 INTERNET-BASED REAL-TIME FAKE NEWS DETECTION");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        titleLabel.setTextFill(Color.WHITE);

        Label subtitleLabel = new Label(
            "Live verification via DuckDuckGo API | Wikipedia API | Domain Reputation Database");
        subtitleLabel.setFont(Font.font("Segoe UI", 12));
        subtitleLabel.setTextFill(Color.web("#f0f0f0"));

        internetStatusLabel = new Label("🌐 Checking Internet Connection...");
        internetStatusLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        internetStatusLabel.setTextFill(Color.YELLOW);

        HBox infoBox = new HBox(20);
        infoBox.setAlignment(Pos.CENTER);

        String roleIcon = currentRole.equals("admin") ? "👑" : "👤";
        String roleText = currentRole.equals("admin") ? "ADMIN" : "USER";
        String name     = currentRole.equals("admin") ? currentAdmin.getName() : currentUser.getName();

        Label userInfo = new Label(roleIcon + " " + roleText + ": " + name);
        userInfo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        userInfo.setTextFill(Color.web("#ffd700"));

        Button logoutBtn = new Button("🚪 Logout");
        logoutBtn.setStyle(
            "-fx-background-color: #dc3545; -fx-text-fill: white; " +
            "-fx-font-size: 12px; -fx-cursor: hand;");
        logoutBtn.setOnAction(e -> {
            primaryStage.hide();
            currentUser  = null;
            currentAdmin = null;
            showLoginScreen();
        });

        infoBox.getChildren().addAll(userInfo, logoutBtn);
        header.getChildren().addAll(titleLabel, subtitleLabel, internetStatusLabel, infoBox);
        return header;
    }

    private TabPane createTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: white;");
        tabPane.getTabs().addAll(
            createInternetVerificationTab(),
            createSearchTab(),
            createFakeSourcesTab(),
            createArticlesTab(),
            createStatisticsTab()
        );
        return tabPane;
    }

    // ── Sorting Controls ──────────────────────────────────────────────────────
    private HBox createSortingControls() {
        HBox sortBox = new HBox(15);
        sortBox.setAlignment(Pos.CENTER_LEFT);
        sortBox.setPadding(new Insets(10, 0, 10, 0));
        
        Label sortLabel = new Label("Sort by:");
        sortLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        
        ComboBox<String> sortTypeCombo = new ComboBox<>();
        sortTypeCombo.getItems().addAll(
            "Source (A-Z)",
            "Source (Z-A)",
            "Date (Newest First)",
            "Date (Oldest First)"
        );
        sortTypeCombo.setValue("Source (A-Z)");
        sortTypeCombo.setPrefWidth(180);
        
        Button applySortBtn = new Button("Apply Sorting");
        applySortBtn.setStyle(
            "-fx-background-color: #17a2b8; -fx-text-fill: white; " +
            "-fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 5 15;");
        
        applySortBtn.setOnAction(e -> {
            String sortType = sortTypeCombo.getValue();
            sortArticles(sortType);
        });
        
        // Date filter controls
        Label filterLabel = new Label("Filter by Date:");
        filterLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        
        DatePicker startDatePicker = new DatePicker();
        startDatePicker.setPromptText("Start Date");
        
        DatePicker endDatePicker = new DatePicker();
        endDatePicker.setPromptText("End Date");
        
        Button applyFilterBtn = new Button("Apply Filter");
        applyFilterBtn.setStyle(
            "-fx-background-color: #28a745; -fx-text-fill: white; " +
            "-fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 5 15;");
        
        applyFilterBtn.setOnAction(e -> {
            if (startDatePicker.getValue() != null || endDatePicker.getValue() != null) {
                filterArticlesByDate(startDatePicker.getValue(), endDatePicker.getValue());
            } else {
                refreshArticleTable();
            }
        });
        
        Button resetFilterBtn = new Button("Reset Filter");
        resetFilterBtn.setStyle(
            "-fx-background-color: #dc3545; -fx-text-fill: white; " +
            "-fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 5 15;");
        
        resetFilterBtn.setOnAction(e -> {
            startDatePicker.setValue(null);
            endDatePicker.setValue(null);
            refreshArticleTable();
        });
        
        sortBox.getChildren().addAll(
            sortLabel, sortTypeCombo, applySortBtn,
            filterLabel, startDatePicker, endDatePicker, applyFilterBtn, resetFilterBtn
        );
        
        return sortBox;
    }

    private void sortArticles(String sortType) {
        if (articleCount == 0) return;
        
        List<Article> articleList = new ArrayList<>();
        for (int i = 0; i < articleCount; i++) {
            articleList.add(articles[i]);
        }
        
        switch (sortType) {
            case "Source (A-Z)":
                articleList.sort(Article.sortBySourceAsc());
                break;
            case "Source (Z-A)":
                articleList.sort(Article.sortBySourceDesc());
                break;
            case "Date (Newest First)":
                articleList.sort(Article.sortByTimestampDesc());
                break;
            case "Date (Oldest First)":
                articleList.sort(Article.sortByTimestampAsc());
                break;
        }
        
        articleData.clear();
        articleData.addAll(articleList);
    }

    private void filterArticlesByDate(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        if (articleCount == 0) return;
        
        java.util.Date start = null;
        java.util.Date end = null;
        
        try {
            if (startDate != null) {
                start = java.sql.Date.valueOf(startDate);
            }
            if (endDate != null) {
                end = java.sql.Date.valueOf(endDate.plusDays(1));
            }
        } catch (Exception e) {
            showAlert("Error", "Invalid date format", Alert.AlertType.ERROR);
            return;
        }
        
        final java.util.Date finalStart = start;
        final java.util.Date finalEnd = end;
        
        List<Article> filtered = new ArrayList<>();
        for (int i = 0; i < articleCount; i++) {
            Article a = articles[i];
            java.util.Date articleDate = a.getTimestamp();
            
            boolean include = true;
            if (finalStart != null && articleDate.before(finalStart)) {
                include = false;
            }
            if (finalEnd != null && articleDate.after(finalEnd)) {
                include = false;
            }
            
            if (include) {
                filtered.add(a);
            }
        }
        
        articleData.clear();
        articleData.addAll(filtered);
        
        statusLabel.setText(String.format(
            "📅 Filtered: %d of %d articles | Start: %s | End: %s",
            filtered.size(), articleCount,
            startDate != null ? startDate.toString() : "Any",
            endDate != null ? endDate.toString() : "Any"
        ));
    }

    // ── Internet Verification Tab ─────────────────────────────────────────────
    private Tab createInternetVerificationTab() {
        Tab tab = new Tab("🌐 Internet Verification");
        tab.setClosable(false);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white;");

        VBox mainBox = new VBox(20);
        mainBox.setPadding(new Insets(30));
        mainBox.setStyle("-fx-background-color: white;");

        Label titleLabel = new Label("🔍 REAL-TIME INTERNET FACT CHECKING");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#1a1a2e"));

        Label descLabel = new Label(
            "This system connects to live internet sources (DuckDuckGo, Wikipedia) to verify article authenticity");
        descLabel.setFont(Font.font("Segoe UI", 14));
        descLabel.setTextFill(Color.web("#666666"));
        descLabel.setWrapText(true);

        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(15);
        formGrid.setAlignment(Pos.CENTER);

        TextField articleIdField = new TextField();
        articleIdField.setPromptText("Enter unique Article ID (number)");
        articleIdField.setPrefWidth(500);

        TextField titleField = new TextField();
        titleField.setPromptText("Enter article title");
        titleField.setPrefWidth(500);

        TextField authorField = new TextField();
        authorField.setPromptText("Enter author name");
        authorField.setPrefWidth(500);

        TextField sourceField = new TextField();
        sourceField.setPromptText("Enter source domain (e.g., bbc.com)");
        sourceField.setPrefWidth(500);

        TextArea contentArea = new TextArea();
        contentArea.setPromptText("Paste article content here for analysis...");
        contentArea.setPrefRowCount(5);
        contentArea.setPrefWidth(600);

        String[]  labels = {"📝 Article ID:", "📌 Title:", "✍️ Author:", "🌐 Source Domain:", "📄 Article Content:"};
        Control[] fields = {articleIdField, titleField, authorField, sourceField, contentArea};

        for (int i = 0; i < labels.length; i++) {
            Label label = new Label(labels[i]);
            label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            formGrid.add(label, 0, i);
            formGrid.add(fields[i], 1, i);
        }

        VBox progressBox = new VBox(10);
        progressBox.setAlignment(Pos.CENTER);
        progressBox.setPadding(new Insets(20));
        progressBox.setStyle(
            "-fx-background-color: #f8f9fa; -fx-border-radius: 10; -fx-background-radius: 10;");
        progressBox.setVisible(false);

        verificationProgress = new ProgressIndicator();
        verificationProgress.setProgress(-1);
        verificationProgress.setPrefSize(60, 60);

        Label progressLabel = new Label("🌐 Connecting to internet APIs...");
        progressLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

        progressBox.getChildren().addAll(verificationProgress, progressLabel);

        TextArea resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 12px;");
        resultArea.setPrefHeight(350);
        resultArea.setVisible(false);

        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        Button verifyBtn = new Button("🌐 VERIFY FROM INTERNET");
        verifyBtn.setStyle(
            "-fx-background-color: #17a2b8; -fx-text-fill: white; " +
            "-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 25; -fx-cursor: hand;");

        Button submitBtn = new Button("✓ SUBMIT ARTICLE");
        submitBtn.setStyle(
            "-fx-background-color: #28a745; -fx-text-fill: white; " +
            "-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 25; -fx-cursor: hand;");
        submitBtn.setDisable(true);

        buttonBox.getChildren().addAll(verifyBtn, submitBtn);

        verifyBtn.setOnAction(e -> {
            String source  = sourceField.getText().trim().toLowerCase();
            String title   = titleField.getText().trim();
            String content = contentArea.getText().trim();

            if (source.isEmpty() || title.isEmpty()) {
                showAlert("Error", "Please enter source and title!", Alert.AlertType.ERROR);
                return;
            }

            progressBox.setVisible(true);
            resultArea.setVisible(false);
            verifyBtn.setDisable(true);
            submitBtn.setDisable(true);

            Thread progressThread = new Thread(() -> {
                try {
                    Thread.sleep(700);
                    javafx.application.Platform.runLater(() ->
                        progressLabel.setText("🌐 Connecting to DuckDuckGo API..."));
                    Thread.sleep(700);
                    javafx.application.Platform.runLater(() ->
                        progressLabel.setText("📖 Querying Wikipedia for source credibility..."));
                    Thread.sleep(700);
                    javafx.application.Platform.runLater(() ->
                        progressLabel.setText("🔍 Checking domain reputation database..."));
                    Thread.sleep(700);
                    javafx.application.Platform.runLater(() ->
                        progressLabel.setText("📝 Analysing content for red flags..."));
                    Thread.sleep(700);
                    javafx.application.Platform.runLater(() ->
                        progressLabel.setText("⚖️ Calculating final verdict..."));
                } catch (InterruptedException ex) { /* ignored */ }
            });
            progressThread.setDaemon(true);
            progressThread.start();

            CompletableFuture<VerificationResult> future =
                RealInternetVerifier.verifyFromInternet(source, title, content);

            future.thenAccept(result -> javafx.application.Platform.runLater(() -> {
                verificationProgress.setProgress(1);
                resultArea.setText(result.message);
                resultArea.setVisible(true);
                progressBox.setVisible(false);

                if (result.status.equals("FAKE")) {
                    resultArea.setStyle(
                        "-fx-font-family: 'Consolas'; -fx-font-size: 12px; " +
                        "-fx-text-fill: red; -fx-control-inner-background: #ffebee;");
                } else if (result.status.equals("REAL")) {
                    resultArea.setStyle(
                        "-fx-font-family: 'Consolas'; -fx-font-size: 12px; " +
                        "-fx-text-fill: green; -fx-control-inner-background: #e8f5e9;");
                } else {
                    resultArea.setStyle(
                        "-fx-font-family: 'Consolas'; -fx-font-size: 12px; " +
                        "-fx-text-fill: orange; -fx-control-inner-background: #fff3e0;");
                }

                submitBtn.setDisable(false);
                verifyBtn.setDisable(false);
                sourceField.setUserData(result);
            }));
        });

        submitBtn.setOnAction(e -> {
            try {
                int    articleId   = Integer.parseInt(articleIdField.getText().trim());
                String title       = titleField.getText().trim();
                String author      = authorField.getText().trim();
                String source      = sourceField.getText().trim().toLowerCase();
                String summary     = contentArea.getText().trim();

                VerificationResult verification = (VerificationResult) sourceField.getUserData();
                if (verification == null) {
                    showAlert("Error", "Please verify the article from internet first!",
                        Alert.AlertType.ERROR);
                    return;
                }

                if (currentRole.equals("user") && currentUser.getSubmissionCount() >= 20) {
                    showAlert("Limit Reached", "Maximum 20 submissions!", Alert.AlertType.WARNING);
                    return;
                }

                if (findArticleById(articleId) != null) {
                    showAlert("Duplicate ID", "Article ID already exists!", Alert.AlertType.ERROR);
                    return;
                }

                if (articleCount >= MAX_ARTICLES) {
                    showAlert("System Full", "Maximum articles limit reached!", Alert.AlertType.ERROR);
                    return;
                }

                Article article = new Article(
                    articleId, title, summary, author, source, new Date(), verification.status);
                articles[articleCount++] = article;

                if (currentRole.equals("user")) currentUser.addToHistory(article);

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Success");
                successAlert.setHeaderText("Article Submitted with Internet Verification!");
                successAlert.setContentText(
                    "Verification Result: " + verification.status +
                    "\n\n" + verification.message);
                successAlert.showAndWait();

                articleIdField.clear();
                titleField.clear();
                authorField.clear();
                sourceField.clear();
                contentArea.clear();
                resultArea.clear();
                resultArea.setVisible(false);
                submitBtn.setDisable(true);
                verifyBtn.setDisable(false);
                sourceField.setUserData(null);

                refreshArticleTable();
                updateStatus();

            } catch (NumberFormatException ex) {
                showAlert("Invalid Input", "Please enter a valid numeric Article ID!",
                    Alert.AlertType.ERROR);
            }
        });

        mainBox.getChildren().addAll(
            titleLabel, descLabel, formGrid, buttonBox, progressBox, resultArea);
        scrollPane.setContent(mainBox);
        tab.setContent(scrollPane);
        return tab;
    }

    // ── Search Tab ────────────────────────────────────────────────────────────
    private Tab createSearchTab() {
        Tab tab = new Tab("🔍 Search Articles");
        tab.setClosable(false);

        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        vbox.setStyle("-fx-background-color: white;");

        HBox searchBox = new HBox(15);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> searchType = new ComboBox<>();
        searchType.getItems().addAll("Search by Source", "Search by Title", "Search by Author");
        searchType.setValue("Search by Source");

        TextField searchField = new TextField();
        searchField.setPromptText("Enter search term...");
        searchField.setPrefWidth(300);

        Button searchBtn = new Button("🔎 Search");
        searchBtn.setStyle(
            "-fx-background-color: #007bff; -fx-text-fill: white; " +
            "-fx-font-weight: bold; -fx-cursor: hand;");

        searchBox.getChildren().addAll(searchType, searchField, searchBtn);

        TextArea searchResults = new TextArea();
        searchResults.setEditable(false);
        searchResults.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 13px;");
        searchResults.setPrefHeight(500);

        searchBtn.setOnAction(e -> {
            String term = searchField.getText().trim().toLowerCase();
            if (term.isEmpty()) {
                showAlert("Empty Field", "Please enter a search term!", Alert.AlertType.WARNING);
                return;
            }
            searchResults.clear();
            searchResults.appendText("🔍 SEARCH RESULTS for: " + term + "\n");
            searchResults.appendText("=".repeat(60) + "\n\n");

            int found = 0;
            for (int i = 0; i < articleCount; i++) {
                Article a = articles[i];
                boolean match = false;
                switch (searchType.getValue()) {
                    case "Search by Source": match = a.getSource().toLowerCase().contains(term); break;
                    case "Search by Title":  match = a.getTitle().toLowerCase().contains(term);  break;
                    default:                 match = a.getAuthor().toLowerCase().contains(term); break;
                }
                if (match) {
                    searchResults.appendText(String.format("[%s] ID:%03d | %s\n",
                        getStatusIcon(a.getStatus()), a.getId(), a.getTitle()));
                    searchResults.appendText(String.format(
                        "   Source: %s | Author: %s | Date: %s\n", 
                        a.getSource(), a.getAuthor(), a.getFormattedDateTime()));
                    searchResults.appendText("   " + "─".repeat(45) + "\n");
                    found++;
                }
            }
            searchResults.appendText("\n📊 Total articles found: " + found);
        });

        vbox.getChildren().addAll(searchBox, searchResults);
        tab.setContent(vbox);
        return tab;
    }

    // ── Fake Sources Tab ──────────────────────────────────────────────────────
    private Tab createFakeSourcesTab() {
        Tab tab = new Tab("🚫 Fake Sources DB");
        tab.setClosable(false);

        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        vbox.setStyle("-fx-background-color: white;");

        TableView<FakeSource> fakeTable = new TableView<>();

        TableColumn<FakeSource, String> sourceCol = new TableColumn<>("Source Domain");
        sourceCol.setCellValueFactory(new PropertyValueFactory<>("sourceName"));
        sourceCol.setPrefWidth(300);

        TableColumn<FakeSource, Integer> reportCol = new TableColumn<>("Report Count");
        reportCol.setCellValueFactory(new PropertyValueFactory<>("reportCount"));
        reportCol.setPrefWidth(150);

        TableColumn<FakeSource, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData ->
            javafx.beans.binding.Bindings.createStringBinding(() ->
                cellData.getValue().isFlagged() ? "🚫 CONFIRMED FAKE" : "⚠️ Under Review"
            )
        );
        statusCol.setPrefWidth(150);

        fakeTable.getColumns().addAll(sourceCol, reportCol, statusCol);
        ObservableList<FakeSource> fakeData = FXCollections.observableArrayList();
        fakeData.addAll(fakeSources.values());
        fakeTable.setItems(fakeData);

        vbox.getChildren().addAll(new Label("📋 Known Fake News Sources Database"), fakeTable);
        tab.setContent(vbox);
        return tab;
    }

    // ── Article Database Tab (UPDATED with Date/Time columns) ─────────────────
    private Tab createArticlesTab() {
        Tab tab = new Tab("📊 Article Database");
        tab.setClosable(false);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));
        vbox.setStyle("-fx-background-color: white;");

        articleTable = new TableView<>();
        articleData  = FXCollections.observableArrayList();

        TableColumn<Article, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);

        TableColumn<Article, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(300);

        TableColumn<Article, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        authorCol.setPrefWidth(120);

        TableColumn<Article, String> sourceCol2 = new TableColumn<>("Source");
        sourceCol2.setCellValueFactory(new PropertyValueFactory<>("source"));
        sourceCol2.setPrefWidth(150);

        TableColumn<Article, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFormattedDate()));
        dateCol.setPrefWidth(100);

        TableColumn<Article, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFormattedTime()));
        timeCol.setPrefWidth(80);

        TableColumn<Article, String> statusCol2 = new TableColumn<>("Verification");
        statusCol2.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol2.setPrefWidth(120);
        statusCol2.setCellFactory(col -> new TableCell<Article, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); 
                    setStyle("");
                } else {
                    setText(item);
                    if      (item.equals("FAKE"))       setStyle("-fx-text-fill: red;    -fx-font-weight: bold;");
                    else if (item.equals("SUSPICIOUS")) setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    else                                setStyle("-fx-text-fill: green;  -fx-font-weight: bold;");
                }
            }
        });

        articleTable.getColumns().addAll(idCol, titleCol, authorCol, sourceCol2, dateCol, timeCol, statusCol2);
        articleTable.setItems(articleData);

        HBox sortControls = createSortingControls();
        
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle(
            "-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 5 15;");
        refreshBtn.setOnAction(e -> refreshArticleTable());

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().add(refreshBtn);

        vbox.getChildren().addAll(sortControls, buttonBox, articleTable);
        
        refreshArticleTable();
        tab.setContent(vbox);
        return tab;
    }

    // ── Statistics Tab (UPDATED with Date/Time stats) ─────────────────────────
    private Tab createStatisticsTab() {
        Tab tab = new Tab("📊 Statistics");
        tab.setClosable(false);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(30));
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setAlignment(Pos.CENTER);
        grid.setStyle("-fx-background-color: white;");

        updateStatCards(grid);

        Button refreshStatsBtn = new Button("🔄 Refresh Statistics");
        refreshStatsBtn.setStyle(
            "-fx-background-color: #007bff; -fx-text-fill: white; " +
            "-fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;");
        refreshStatsBtn.setOnAction(e -> updateStatCards(grid));

        VBox vbox = new VBox(15);
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().addAll(grid, refreshStatsBtn);

        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white;");
        tab.setContent(scrollPane);
        return tab;
    }

    private void updateStatCards(GridPane grid) {
        grid.getChildren().clear();
        int real = 0, fake = 0, suspicious = 0;
        for (int i = 0; i < articleCount; i++) {
            switch (articles[i].getStatus()) {
                case "REAL":       real++;       break;
                case "FAKE":       fake++;       break;
                case "SUSPICIOUS": suspicious++; break;
            }
        }
        addStatCard(grid, "📰 Total Articles", String.valueOf(articleCount),    "Verified via Internet", 0, 0);
        addStatCard(grid, "👥 Total Users",    String.valueOf(users.size()),     "Registered Users",      1, 0);
        addStatCard(grid, "🚫 Fake Sources",   String.valueOf(fakeSources.size()),"Blacklisted",          2, 0);
        addStatCard(grid, "✅ Real Articles",  String.valueOf(real),             "Internet Verified",     0, 1);
        addStatCard(grid, "🚫 Fake Articles",  String.valueOf(fake),             "Internet Detected",     1, 1);
        addStatCard(grid, "⚠️ Suspicious",    String.valueOf(suspicious),       "Needs Review",          2, 1);
        addStatCard(grid, "🌐 APIs Active",    "3/3",                            "DDG + Wiki + Domain",   0, 2);
        addStatCard(grid, "💾 Storage",        String.format("%d/%d", articleCount, MAX_ARTICLES), "Array Storage", 1, 2);
        addDateTimeStats(grid, 2, 2);
    }

    private void addStatCard(GridPane grid, String title, String value,
                             String subtitle, int col, int row) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setStyle(
            "-fx-background-color: white; -fx-border-color: #4CAF50; " +
            "-fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        card.setPrefWidth(220);
        card.setPrefHeight(130);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.web("#1a1a2e"));

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        valueLabel.setTextFill(Color.web("#4CAF50"));

        Label subLabel = new Label(subtitle);
        subLabel.setFont(Font.font("Segoe UI", 11));
        subLabel.setTextFill(Color.web("#888888"));

        card.getChildren().addAll(titleLabel, valueLabel, subLabel);
        grid.add(card, col, row);
    }

    private void addDateTimeStats(GridPane grid, int col, int row) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setStyle(
            "-fx-background-color: white; -fx-border-color: #ff9800; " +
            "-fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        card.setPrefWidth(220);
        card.setPrefHeight(130);

        Label titleLabel = new Label("📅 Latest Article");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.web("#1a1a2e"));

        String latestInfo = "No articles";
        if (articleCount > 0) {
            Article latest = articles[0];
            for (int i = 1; i < articleCount; i++) {
                if (articles[i].getTimestamp().after(latest.getTimestamp())) {
                    latest = articles[i];
                }
            }
            latestInfo = latest.getFormattedDateTime();
        }
        
        Label valueLabel = new Label(latestInfo);
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        valueLabel.setTextFill(Color.web("#ff9800"));
        valueLabel.setWrapText(true);

        Label subLabel = new Label("Most recent submission");
        subLabel.setFont(Font.font("Segoe UI", 11));
        subLabel.setTextFill(Color.web("#888888"));

        card.getChildren().addAll(titleLabel, valueLabel, subLabel);
        grid.add(card, col, row);
    }

    // ── Footer ────────────────────────────────────────────────────────────────
    private HBox createFooter() {
        HBox footer = new HBox();
        footer.setStyle("-fx-background-color: #1a1a2e;");
        footer.setPadding(new Insets(10));
        footer.setAlignment(Pos.CENTER);

        statusLabel = new Label();
        statusLabel.setTextFill(Color.WHITE);
        statusLabel.setFont(Font.font("Segoe UI", 12));
        footer.getChildren().add(statusLabel);
        return footer;
    }

    // ── Data helpers ──────────────────────────────────────────────────────────
    private void loadSampleData() {
        admins.add(new Admin(1, "Administrator", "admin123", "Super Admin"));
        users.add(new User(1, "John Doe",   "user123"));
        users.add(new User(2, "Jane Smith", "user456"));

        String[] fakeSourcesList = {"news-buzz.com", "clickbait-news.net", "fakenewsdaily.org"};
        for (String src : fakeSourcesList) fakeSources.put(src, new FakeSource(src));
    }

    private User  findUserById(int id) {
        for (User  u : users)  if (u.getId() == id) return u;
        return null;
    }
    private Admin findAdminById(int id) {
        for (Admin a : admins) if (a.getId() == id) return a;
        return null;
    }
    private Article findArticleById(int id) {
        for (int i = 0; i < articleCount; i++)
            if (articles[i].getId() == id) return articles[i];
        return null;
    }

    private void refreshArticleTable() {
        articleData.clear();
        for (int i = 0; i < articleCount; i++) articleData.add(articles[i]);
    }

    private void updateStatus() {
        int real = 0, fake = 0, suspicious = 0;
        for (int i = 0; i < articleCount; i++) {
            switch (articles[i].getStatus()) {
                case "REAL":       real++;       break;
                case "FAKE":       fake++;       break;
                case "SUSPICIOUS": suspicious++; break;
            }
        }
        statusLabel.setText(String.format(
            "🌐 Internet Detection Active | 📰 %d Articles | 👥 %d Users | " +
            "✅ %d Real | 🚫 %d Fake | ⚠️ %d Suspicious",
            articleCount, users.size(), real, fake, suspicious));
    }

    private String getStatusIcon(String status) {
        switch (status) {
            case "FAKE":       return "🚫";
            case "SUSPICIOUS": return "⚠️";
            default:           return "✅";
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) { launch(args); }
}