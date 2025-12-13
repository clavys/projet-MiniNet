package front;

import architecture.components.Client;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class HubView {

    private static final String FONT_FAMILY = "Monospaced";
    private static BorderPane mainLayout;

    public static Scene createScene(Stage stage) {
        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #000000;");

        // HEADER
        mainLayout.setTop(createHeader());

        // SIDEBAR
        mainLayout.setLeft(createSidebar(stage));

        // Wall en tant que page d'acceuil
        showWallContent();

        return new Scene(mainLayout, 950, 650);
    }

    // --- HEADER ---
    private static HBox createHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(10, 20, 10, 20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(20);
        header.setStyle("-fx-background-color: #000000; -fx-border-color: #FFFFFF; -fx-border-width: 0 0 1 0;");

        Label logo = new Label(">_ MiniNet_Hub");
        logo.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 18));
        logo.setTextFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String username = Client.instance.getCurrentUser();

        Label userLabel = new Label("[ USER: " + username + " ]");
        userLabel.setFont(Font.font(FONT_FAMILY, 14));
        userLabel.setTextFill(Color.web("#00FF00"));

        header.getChildren().addAll(logo, spacer, userLabel);
        return header;
    }

    // --- SIDEBAR ---
    private static VBox createSidebar(Stage stage) {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-border-color: #FFFFFF; -fx-border-width: 0 1 0 0;");

        Button btnWall = createMenuButton("1. WALL / FEED");
        Button btnMessages = createMenuButton("2. MESSAGES (MP)");
        Button btnFriends = createMenuButton("3. FRIENDS & RECOS");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button btnLogout = createMenuButton("0. LOGOUT");
        btnLogout.setTextFill(Color.RED);

        // Actions
        btnWall.setOnAction(e -> showWallContent());
        btnMessages.setOnAction(e -> showMessagesContent());
        btnFriends.setOnAction(e -> showFriendsContent());

        btnLogout.setOnAction(e -> {
            if(Client.instance != null) Client.instance.logout();
            stage.setScene(LoginView.createScene(stage));
        });

        sidebar.getChildren().addAll(btnWall, btnMessages, btnFriends, spacer, btnLogout);
        return sidebar;
    }

    // =========================================================================
    // 1. ONGLET WALL (MUR)
    // =========================================================================
    private static void showWallContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label title = new Label(">> GLOBAL_WALL_FEED");
        title.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 16));
        title.setTextFill(Color.WHITE);

        // Zone d'affichage
        TextArea wallDisplay = new TextArea();
        wallDisplay.setEditable(false);
        styleTerminalTextArea(wallDisplay);

        // Chargement des données
        String wallData = Client.instance.getWall();
        if(wallData == null || wallData.isEmpty()) wallDisplay.setText(">> Connect to server to see posts...");
        else wallDisplay.setText(wallData);

        // Zone de publication
        HBox postBox = new HBox(10);
        TextField newPostField = new TextField();
        newPostField.setPromptText("Write status update...");
        styleTerminalField(newPostField);
        HBox.setHgrow(newPostField, Priority.ALWAYS);

        Button btnPost = createMenuButton("[ PUBLISH ]");
        btnPost.setPrefWidth(120);
        btnPost.setOnAction(e -> {
            String txt = newPostField.getText();
            if(!txt.isEmpty()) {
                Client.instance.createPost(txt);
                // Petit hack pour rafraîchir "instantanément" (optimiste)
                wallDisplay.setText(Client.instance.getWall());
                newPostField.clear();
            }
        });

        postBox.getChildren().addAll(newPostField, btnPost);
        content.getChildren().addAll(title, wallDisplay, postBox);

        mainLayout.setCenter(content);
    }

    // =========================================================================
    // 2. ONGLET MESSAGERIE (MP)
    // =========================================================================
    private static void showMessagesContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // --- Titre ---
        Label title = new Label(">> SECURE_MESSAGING_SYSTEM");
        title.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 16));
        title.setTextFill(Color.WHITE);

        // --- Inbox (Lecture) ---
        Label lblInbox = new Label(":: INBOX ::");
        lblInbox.setTextFill(Color.GRAY);
        lblInbox.setFont(Font.font(FONT_FAMILY, 12));

        TextArea msgDisplay = new TextArea();
        msgDisplay.setEditable(false);
        styleTerminalTextArea(msgDisplay);

        // 1. On définit ce qui doit se passer quand une notif arrive
        Client.instance.setUiCallback(notification -> {
            // notification contient le texte envoyé par le serveur
            System.out.println("UI >> Mise à jour déclenchée par WebSocket");

            // On recharge simplement tous les messages pour être à jour
            String refreshContent = Client.instance.readMessage();
            msgDisplay.setText(refreshContent);

            // Petit bonus visuel
            msgDisplay.appendText("\n[SYNC] Live update received.");
        });

        // 2. On charge les messages une première fois (comme avant)
        msgDisplay.setText(Client.instance.readMessage());

        // --- Compose (Écriture) ---
        Label lblCompose = new Label(":: COMPOSE NEW MESSAGE ::");
        lblCompose.setTextFill(Color.GRAY);
        lblCompose.setFont(Font.font(FONT_FAMILY, 12));

        HBox composeBox = new HBox(10);

        TextField toField = new TextField();
        toField.setPromptText("Recipient (User)");
        styleTerminalField(toField);
        toField.setPrefWidth(200);

        TextField bodyField = new TextField();
        bodyField.setPromptText("Encrypted message content...");
        styleTerminalField(bodyField);
        HBox.setHgrow(bodyField, Priority.ALWAYS);

        Button btnSend = createMenuButton("[ SEND ]");
        btnSend.setOnAction(e -> {
            String to = toField.getText();
            String body = bodyField.getText();
            if(!to.isEmpty() && !body.isEmpty()) {
                Client.instance.sendMessage(to, body);
                bodyField.clear();
                msgDisplay.appendText("\n[SYS] Message sent to " + to);
            }
        });

        composeBox.getChildren().addAll(toField, bodyField, btnSend);

        content.getChildren().addAll(title, lblInbox, msgDisplay, new Separator(), lblCompose, composeBox);
        mainLayout.setCenter(content);
    }

    // =========================================================================
    // 3. ONGLET AMIS & RECOMMANDATIONS
    // =========================================================================
    private static void showFriendsContent() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(20);
        grid.setVgap(10);

        // --- COLONNE GAUCHE : MES AMIS ---
        Label titleFriends = new Label(">> MY_FRIENDS_LIST");
        titleFriends.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 16));
        titleFriends.setTextFill(Color.WHITE);

        ListView<HBox> friendsList = new ListView<>();
        friendsList.setStyle("-fx-background-color: #111; -fx-control-inner-background: #111;");

        // Chargement des amis
        String rawFriends = Client.instance.getFriendsList(); // "Alice,Bob"
        if(rawFriends != null && !rawFriends.isEmpty()) {
            for(String f : rawFriends.split(",")) {
                if(!f.trim().isEmpty()) {
                    friendsList.getItems().add(createFriendItem(f, true));
                }
            }
        } else {
            friendsList.getItems().add(new HBox(new Label("No friends yet.")));
        }

        // --- COLONNE DROITE : RECOMMANDATIONS ---
        Label titleRecos = new Label(">> SUGGESTED_CONNECTIONS");
        titleRecos.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 16));
        titleRecos.setTextFill(Color.CYAN);

        ListView<HBox> recosList = new ListView<>();
        recosList.setStyle("-fx-background-color: #111; -fx-control-inner-background: #111;");

        // Chargement des recommandations
        String rawRecos = Client.instance.getRecommendations();
        if(rawRecos != null && !rawRecos.isEmpty() && !rawRecos.contains("Aucune")) {
            for(String r : rawRecos.split(",")) {
                if(!r.trim().isEmpty()) {
                    recosList.getItems().add(createFriendItem(r, false));
                }
            }
        } else {
            recosList.getItems().add(new HBox(new Label("No suggestions.")));
        }

        // Ajout MANUEL d'un ami (fallback)
        HBox manualAddBox = new HBox(10);
        TextField manualField = new TextField();
        manualField.setPromptText("Add manually...");
        styleTerminalField(manualField);
        Button btnManualAdd = createMenuButton("[+]");
        btnManualAdd.setOnAction(e -> {
            Client.instance.addFriend(manualField.getText());
            showFriendsContent(); // Refresh bourrin mais efficace
        });
        manualAddBox.getChildren().addAll(manualField, btnManualAdd);


        // Placement dans la grille
        grid.add(titleFriends, 0, 0);
        grid.add(friendsList, 0, 1);

        grid.add(titleRecos, 1, 0);
        grid.add(recosList, 1, 1);
        grid.add(manualAddBox, 1, 2);

        // Contraintes de taille
        ColumnConstraints col1 = new ColumnConstraints(); col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints(); col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);

        mainLayout.setCenter(grid);
    }

    // Helper pour créer une ligne dans la liste (Ami + Bouton Action)
    private static HBox createFriendItem(String name, boolean isFriend) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(5));

        Label nameLbl = new Label(name);
        nameLbl.setTextFill(Color.WHITE);
        nameLbl.setFont(Font.font(FONT_FAMILY, 14));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button actionBtn = new Button(isFriend ? "[ DEL ]" : "[ ADD ]");
        actionBtn.setStyle(isFriend
                ? "-fx-background-color: #500; -fx-text-fill: white; -fx-font-size: 10px;"
                : "-fx-background-color: #050; -fx-text-fill: white; -fx-font-size: 10px;");

        actionBtn.setOnAction(e -> {
            if(isFriend) {
                Client.instance.removeFriend(name);
            } else {
                Client.instance.addFriend(name);
            }
            // On rafraichit la vue pour voir le changement
            showFriendsContent();
        });

        row.getChildren().addAll(nameLbl, spacer, actionBtn);
        return row;
    }


    // --- STYLES UTILITAIRES ---

    private static Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(10));
        btn.setCursor(javafx.scene.Cursor.HAND);
        btn.setFont(Font.font(FONT_FAMILY, 14));
        String styleNormal = "-fx-background-color: #000000; -fx-text-fill: #FFFFFF; -fx-border-color: #333333;";
        String styleHover = "-fx-background-color: #FFFFFF; -fx-text-fill: #000000; -fx-border-color: #FFFFFF;";
        btn.setStyle(styleNormal);
        btn.setOnMouseEntered(e -> btn.setStyle(styleHover));
        btn.setOnMouseExited(e -> btn.setStyle(styleNormal));
        return btn;
    }

    private static void styleTerminalField(TextField field) {
        field.setPrefHeight(35);
        field.setStyle("-fx-background-color: #000000; -fx-text-fill: #00FF00; -fx-prompt-text-fill: #555555; -fx-border-color: #FFFFFF; -fx-font-family: '" + FONT_FAMILY + "';");
    }

    private static void styleTerminalTextArea(TextArea area) {
        area.setStyle("-fx-control-inner-background: #000000; -fx-text-fill: #00FF00; -fx-font-family: '" + FONT_FAMILY + "';");
    }
}