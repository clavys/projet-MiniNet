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
    private static BorderPane mainLayout; // Le conteneur principal

    public static Scene createScene(Stage stage) {
        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #000000;");

        // 1. HEADER (Haut)
        HBox header = createHeader();
        mainLayout.setTop(header);

        // 2. SIDEBAR (Gauche - Menu)
        VBox sidebar = createSidebar(stage);
        mainLayout.setLeft(sidebar);

        // 3. CONTENT (Centre - Par défaut le Mur)
        // On affiche le mur au démarrage
        showWallContent();

        return new Scene(mainLayout, 900, 600);
    }

    // ---HEADER ---
    private static HBox createHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(10, 20, 10, 20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(20);

        // Bordure blanche en bas uniquement
        header.setStyle("-fx-background-color: #000000; -fx-border-color: #FFFFFF; -fx-border-width: 0 0 1 0;");

        Label logo = new Label(">_ MiniNet_Hub");
        logo.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 18));
        logo.setTextFill(Color.WHITE);

        // Espace flexible pour pousser le nom de l'user à droite
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Récupération dynamique du user connecté (si géré dans Client)
        // Pour l'instant on met un placeholder si null
        String username = (Client.instance != null) ? "LOGGED_USER" : "GUEST";

        Label userLabel = new Label("[ USER: " + username + " ]");
        userLabel.setFont(Font.font(FONT_FAMILY, 14));
        userLabel.setTextFill(Color.web("#00FF00")); // Vert terminal

        header.getChildren().addAll(logo, spacer, userLabel);
        return header;
    }

    // --- SIDEBAR ---
    private static VBox createSidebar(Stage stage) {
        VBox sidebar = new VBox(10); // Espacement de 10px entre les boutons
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(200);
        // Bordure à droite
        sidebar.setStyle("-fx-border-color: #FFFFFF; -fx-border-width: 0 1 0 0;");

        // Boutons de navigation
        Button btnWall = createMenuButton("1. WALL / FEED");
        Button btnMessages = createMenuButton("2. MESSAGES");
        Button btnFriends = createMenuButton("3. FRIENDS");

        // Espaceur pour pousser le bouton Logout en bas
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button btnLogout = createMenuButton("0. LOGOUT");
        btnLogout.setTextFill(Color.RED); // Texte rouge pour la sortie

        // --- ACTIONS ---
        btnWall.setOnAction(e -> showWallContent());
        btnMessages.setOnAction(e -> showMessagesContent());
        btnFriends.setOnAction(e -> showFriendsContent());

        btnLogout.setOnAction(e -> {
            System.out.println("CMD >> LOGOUT_INITIATED");
            // Retour au login
            stage.setScene(LoginView.createScene(stage));
        });

        sidebar.getChildren().addAll(btnWall, btnMessages, btnFriends, spacer, btnLogout);
        return sidebar;
    }

    // --- CONTENU CENTRAL ---

    // AFFICHER LE WALL
    private static void showWallContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label title = new Label(">> SYSTEM_WALL_FEED");
        title.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 16));
        title.setTextFill(Color.WHITE);

        // Zone de texte pour voir les posts
        TextArea wallDisplay = new TextArea();
        wallDisplay.setEditable(false);
        styleTerminalTextArea(wallDisplay);

        // Récupérer la TL
        wallDisplay.setText("Loading data from RPCConnector...\n[SUCCESS] Connected to Server.");
        wallDisplay.setText(Client.instance.getWall());

        // Zone pour publier
        HBox postBox = new HBox(10);
        TextField newPostField = new TextField();
        newPostField.setPromptText("Write new status...");
        styleTerminalField(newPostField);
        HBox.setHgrow(newPostField, Priority.ALWAYS);

        Button btnPost = createMenuButton("[ POST ]");
        btnPost.setPrefWidth(100);
        btnPost.setOnAction(e -> {
            String txt = newPostField.getText();
            if(!txt.isEmpty()) {
                Client.instance.createPost(txt); // Appel architectural
                wallDisplay.appendText("\n> You: " + txt);
                newPostField.clear();
            }
        });

        postBox.getChildren().addAll(newPostField, btnPost);
        content.getChildren().addAll(title, wallDisplay, postBox);

        mainLayout.setCenter(content);
    }

    // AFFICHER LES MESSAGES
    private static void showMessagesContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label title = new Label(">> PRIVATE_INBOX");
        title.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 16));
        title.setTextFill(Color.WHITE);

        TextArea msgDisplay = new TextArea(Client.instance.readMessage());
        msgDisplay.setEditable(false);
        msgDisplay.setText("No new secure messages.");
        styleTerminalTextArea(msgDisplay);

        content.getChildren().addAll(title, msgDisplay);
        mainLayout.setCenter(content);
    }

    // AFFICHER LES AMIS
    private static void showFriendsContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label title = new Label(">> FRIEND_LIST_MANAGER");
        title.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 16));
        title.setTextFill(Color.WHITE);

        TextField friendField = new TextField();
        friendField.setPromptText("Enter friend username...");
        styleTerminalField(friendField);

        Button btnAdd = createMenuButton("[ ADD FRIEND ]");
        btnAdd.setOnAction(e -> {
            Client.instance.addFriend(friendField.getText());
            friendField.clear();
        });

        content.getChildren().addAll(title, friendField, btnAdd);
        mainLayout.setCenter(content);
    }


    // --- STYLE ---

    private static Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(10));
        btn.setCursor(javafx.scene.Cursor.HAND);
        btn.setFont(Font.font(FONT_FAMILY, 14));

        // Style normal (Fond noir, texte blanc)
        String styleNormal =
                "-fx-background-color: #000000; -fx-text-fill: #FFFFFF; " +
                        "-fx-border-color: #333333; -fx-border-width: 1px;";

        // Style Hover (Inversion vidéo)
        String styleHover =
                "-fx-background-color: #FFFFFF; -fx-text-fill: #000000; " +
                        "-fx-border-color: #FFFFFF;";

        btn.setStyle(styleNormal);
        btn.setOnMouseEntered(e -> btn.setStyle(styleHover));
        btn.setOnMouseExited(e -> btn.setStyle(styleNormal));

        return btn;
    }

    private static void styleTerminalField(TextField field) {
        field.setPrefHeight(35);
        field.setStyle(
                "-fx-background-color: #000000; -fx-text-fill: #00FF00; " + // Texte vert quand on écrit
                        "-fx-prompt-text-fill: #555555; " +
                        "-fx-border-color: #FFFFFF; -fx-border-width: 1px; " +
                        "-fx-font-family: '" + FONT_FAMILY + "';"
        );
    }

    private static void styleTerminalTextArea(TextArea area) {
        // Enlève le style par défaut de la scrollpane interne
        area.setStyle(
                "-fx-control-inner-background: #000000; " +
                        "-fx-text-fill: #00FF00; " + // Texte des logs en vert hacker
                        "-fx-font-family: '" + FONT_FAMILY + "'; " +
                        "-fx-highlight-fill: #FFFFFF; -fx-highlight-text-fill: #000000;"
        );

    }
}