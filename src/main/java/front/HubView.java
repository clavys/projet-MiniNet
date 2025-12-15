package front;

import architecture.components.Client;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class HubView {

    private static final String FONT_FAMILY = "Monospaced";
    private static BorderPane mainLayout;

    // UI Elements globaux
    private static Button btnMessages;
    private static boolean isViewingMessages = false;

    // Données Messagerie
    private static ListView<String> conversationList;
    private static ListView<String> chatView;
    private static String currentChatPartner = null;

    // Cache : Map<Utilisateur, ContenuDeLaConversation>
    private static Map<String, StringBuilder> conversationsCache = new HashMap<>();

    public static Scene createScene(Stage stage) {
        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #000000;");

        // --- SIDEBAR ---
        mainLayout.setLeft(createSidebar(stage));

        // --- HEADER ---
        mainLayout.setTop(createHeader());

        // --- WEBSOCKET CALLBACK ---
        Client.instance.setUiCallback(event -> {
            System.out.println("UI >> Event reçu : " + event);

            // Si c'est un message privé (MSG:Alice)
            if (event.startsWith("MSG:")) {
                String sender = event.split(":")[1];

                Platform.runLater(() -> {
                    // Si on n'est pas sur l'onglet message : NOTIFICATION ROUGE
                    if (!isViewingMessages) {
                        btnMessages.setText("2. MESSAGES (!)");
                        btnMessages.setStyle("-fx-background-color: #000000; -fx-text-fill: red; -fx-border-color: red;");
                    } else {
                        // Si on est déjà dessus, on recharge tout
                        refreshMessagesData();
                        // Si on parlait déjà à cette personne, on met à jour la vue
                        if (currentChatPartner != null) {
                            openConversation(currentChatPartner);
                        }
                    }
                });
            }
        });

        // Vue par défaut
        showWallContent();

        return new Scene(mainLayout, 1000, 700);
    }

    // --- LOGIQUE MESSAGERIE ---

    private static void refreshMessagesData() {
        conversationsCache.clear();
        String rawData = Client.instance.readMessage(); // Format: TIMESTAMP::SENDER::RECIPIENT::CONTENT

        if (rawData == null || rawData.isEmpty()) return;

        String currentUser = Client.instance.getCurrentUser();
        String[] lines = rawData.split("\n");

        //Sur chaque ligne on check quel type data
        for (String line : lines) {
            try {
                // Parsing du format robuste
                String[] parts = line.split("::", 4);
                if (parts.length < 4) continue;

                // String timestamp = parts[0];
                String sender = parts[1];
                String recipient = parts[2];
                String content = parts[3];

                String otherPerson;
                String displayLine;

                if (sender.equals(currentUser)) {
                    // C'est un message que J'AI envoyé
                    otherPerson = recipient;
                    displayLine = "MOI > " + content;
                } else {
                    // C'est un message que J'AI reçu
                    otherPerson = sender;
                    displayLine = otherPerson + " > " + content;
                }

                // On ajoute au cache de la bonne conversation
                conversationsCache.computeIfAbsent(otherPerson, k -> new StringBuilder())
                        .append(displayLine).append("\n");

            } catch (Exception e) {
                System.err.println("Erreur parsing ligne : " + line);
            }
        }

        // Mise à jour de la liste des contacts (à gauche)
        if (conversationList != null) {
            conversationList.getItems().clear();
            conversationList.getItems().addAll(conversationsCache.keySet());
        }
    }

    private static void openConversation(String partner) {
        currentChatPartner = partner;
        chatView.getItems().clear();

        if (conversationsCache.containsKey(partner)) {
            String fullText = conversationsCache.get(partner).toString();
            chatView.getItems().addAll(fullText.split("\n"));
        } else {
            chatView.getItems().add(">> Nouvelle conversation avec " + partner);
        }
        // Scroll automatique en bas
        chatView.scrollTo(chatView.getItems().size() - 1);
    }

    // =========================================================================
    // VUE : MESSAGERIE
    // =========================================================================
    private static void showMessagesContent() {
        SplitPane split = new SplitPane();
        split.setStyle("-fx-background-color: black; -fx-box-border: transparent;");

        // GAUCHE : LISTE CONTACTS
        VBox leftPane = new VBox(10);
        leftPane.setPadding(new Insets(10));
        leftPane.setStyle("-fx-background-color: #111;");

        conversationList = new ListView<>();
        conversationList.setStyle("-fx-control-inner-background: #111; -fx-background-color: #111;");
        conversationList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) openConversation(newVal);
        });

        leftPane.getChildren().addAll(new Label(":: CONTACTS ::"), conversationList);
        VBox.setVgrow(conversationList, Priority.ALWAYS);

        // DROITE : CHAT
        VBox rightPane = new VBox(10);
        rightPane.setPadding(new Insets(10));
        rightPane.setStyle("-fx-background-color: #000;");

        chatView = new ListView<>();
        chatView.setStyle("-fx-control-inner-background: #000; -fx-background-color: #000;");
        VBox.setVgrow(chatView, Priority.ALWAYS);

        HBox inputBox = new HBox(10);
        TextField msgField = new TextField();
        msgField.setPromptText("Message...");
        styleTerminalField(msgField);
        HBox.setHgrow(msgField, Priority.ALWAYS);

        Button btnSend = createMenuButton("[ SEND ]");
        btnSend.setOnAction(e -> {
            String txt = msgField.getText();
            if (currentChatPartner != null && !txt.isEmpty()) {
                Client.instance.sendMessage(currentChatPartner, txt);
                msgField.clear();
                // Ajout immédiat pour fluidité
                chatView.getItems().add("MOI > " + txt);
                chatView.scrollTo(chatView.getItems().size() - 1);
                // On force un petit refresh des données pour être sûr que le timestamp serveur soit pris en compte plus tard
                // (Optionnel, le WebSocket le fera aussi)
            }
        });

        inputBox.getChildren().addAll(msgField, btnSend);
        rightPane.getChildren().addAll(new Label(":: DISCUSSION ::"), chatView, inputBox);

        split.getItems().addAll(leftPane, rightPane);
        split.setDividerPositions(0.3);

        refreshMessagesData();

        // Réouverture de la conv active si besoin
        if (currentChatPartner != null) {
            conversationList.getSelectionModel().select(currentChatPartner);
            openConversation(currentChatPartner);
        }

        mainLayout.setCenter(split);
    }

    // =========================================================================
    //  AUTRES VUES (Wall, Sidebar...)
    // =========================================================================

    private static void showWallContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        Label title = new Label(">> GLOBAL_WALL_FEED");
        title.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 16));
        title.setTextFill(Color.WHITE);

        TextArea wallDisplay = new TextArea();
        wallDisplay.setEditable(false);
        styleTerminalTextArea(wallDisplay);
        VBox.setVgrow(wallDisplay, Priority.ALWAYS);

        // Chargement du mur
        String w = Client.instance.getWall();
        wallDisplay.setText((w == null || w.isEmpty()) ? ">> Connect to server..." : w);

        HBox postBox = new HBox(10);
        TextField newPostField = new TextField();
        styleTerminalField(newPostField);
        HBox.setHgrow(newPostField, Priority.ALWAYS);
        Button btnPost = createMenuButton("[ PUBLISH ]");

        btnPost.setOnAction(e -> {
            if(!newPostField.getText().isEmpty()) {
                Client.instance.createPost(newPostField.getText());
                newPostField.clear();
                wallDisplay.setText(Client.instance.getWall()); // Refresh local immédiat
            }
        });

        // Petit bouton refresh manuel
        Button btnRef = createMenuButton("R");
        btnRef.setOnAction(e -> wallDisplay.setText(Client.instance.getWall()));

        postBox.getChildren().addAll(newPostField, btnPost, btnRef);
        content.getChildren().addAll(title, wallDisplay, postBox);
        mainLayout.setCenter(content);
    }

// =========================================================================
// 3. ONGLET AMIS
// =========================================================================

    private static void showFriendsContent() {

        GridPane grid = new GridPane();

        grid.setPadding(new Insets(20));

        grid.setHgap(20);

        grid.setVgap(10);


        // --- MES AMIS ---

        Label titleFriends = new Label(">> MY_FRIENDS_LIST");

        titleFriends.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 16));

        titleFriends.setTextFill(Color.WHITE);


        ListView<HBox> friendsList = new ListView<>();

        friendsList.setStyle("-fx-background-color: #111; -fx-control-inner-background: #111;");

        String rawFriends = Client.instance.getFriendsRaw();

        if(rawFriends != null && !rawFriends.isEmpty()) {
            String[] amis = rawFriends.split(",");
            for(String f : amis) {
                if(!f.trim().isEmpty()) {
                    friendsList.getItems().add(createFriendItem(f.trim(), true));
                }
            }
        } else {
            friendsList.getItems().add(new HBox(new Label("No friends yet.")));
        }

        // --- RECOMMANDATIONS ---

        Label titleRecos = new Label(">> SUGGESTED");

        titleRecos.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 16));

        titleRecos.setTextFill(Color.CYAN);

        ListView<HBox> recosList = new ListView<>();

        recosList.setStyle("-fx-background-color: #111; -fx-control-inner-background: #111;");

        String rawRecos = Client.instance.getRecommendationsRaw();

        if(rawRecos != null && !rawRecos.isEmpty() && !rawRecos.contains("Aucune")) {
            for(String r : rawRecos.split(",")) {
                if(!r.trim().isEmpty()) {
                    recosList.getItems().add(createFriendItem(r.trim(), false));
                }
            }
        }


        // Ajout MANUEL

        HBox manualAddBox = new HBox(10);

        TextField manualField = new TextField();

        manualField.setPromptText("Add manually...");

        styleTerminalField(manualField);

        Button btnManualAdd = createMenuButton("[+]");

        btnManualAdd.setOnAction(e -> {
            Client.instance.addFriend(manualField.getText());
            showFriendsContent();
        });

        manualAddBox.getChildren().addAll(manualField, btnManualAdd);

        grid.add(titleFriends, 0, 0);
        grid.add(friendsList, 0, 1);
        grid.add(titleRecos, 1, 0);
        grid.add(recosList, 1, 1);
        grid.add(manualAddBox, 1, 2);

        ColumnConstraints col1 = new ColumnConstraints(); col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints(); col2.setPercentWidth(50);

        grid.getColumnConstraints().addAll(col1, col2);

        mainLayout.setCenter(grid);
    }


    private static HBox createFriendItem(String name, boolean isFriend) {

        HBox row = new HBox(10);

        row.setAlignment(Pos.CENTER_LEFT);

        Label nameLbl = new Label(name);

        nameLbl.setTextFill(Color.WHITE);

        nameLbl.setFont(Font.font(FONT_FAMILY, 14));

        nameLbl.setPrefWidth(100);

        Region spacer = new Region();

        HBox.setHgrow(spacer, Priority.ALWAYS);

        if (isFriend) {
            // Bouton Message Rapide
            Button btnMsg = new Button("[ MSG ]");
            btnMsg.setStyle("-fx-background-color: #000; -fx-text-fill: cyan; -fx-border-color: cyan; -fx-font-size: 10px;");
            btnMsg.setOnAction(e -> {
                // Switch vers l'onglet message et ouvre la conv
                currentChatPartner = name;
                btnMessages.fire(); // Simule le clic
            });


            Button btnDel = new Button("[ X ]");
            btnDel.setStyle("-fx-background-color: #500; -fx-text-fill: white; -fx-font-size: 10px;");

            btnDel.setOnAction(e -> {
                Client.instance.removeFriend(name);
                showFriendsContent();
            });
            row.getChildren().addAll(nameLbl, spacer, btnMsg, btnDel);
        } else {
            Button btnAdd = new Button("[ ADD ]");
            btnAdd.setStyle("-fx-background-color: #050; -fx-text-fill: white; -fx-font-size: 10px;");
            btnAdd.setOnAction(e -> {

                Client.instance.addFriend(name);

                showFriendsContent();

            });

            row.getChildren().addAll(nameLbl, spacer, btnAdd);

        }


        return row;

    }

    // --- SIDEBAR ---
    private static VBox createSidebar(Stage stage) {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-border-color: #FFFFFF; -fx-border-width: 0 1 0 0;");

        Button btnWall = createMenuButton("1. WALL / FEED");
        btnMessages = createMenuButton("2. MESSAGES");
        Button btnFriends = createMenuButton("3. FRIENDS");
        Button btnLogout = createMenuButton("0. LOGOUT");
        btnLogout.setTextFill(Color.RED);
        Region spacer = new Region(); VBox.setVgrow(spacer, Priority.ALWAYS);

        btnWall.setOnAction(e -> { isViewingMessages = false; showWallContent(); });
        btnMessages.setOnAction(e -> {
            isViewingMessages = true;
            btnMessages.setText("2. MESSAGES"); // Reset notif
            btnMessages.setStyle("-fx-background-color: #000000; -fx-text-fill: white; -fx-border-color: #333;");
            showMessagesContent();
        });
        btnFriends.setOnAction(e -> { isViewingMessages = false; showFriendsContent(); }); // Idéalement remettre le vrai code Friends ici
        btnLogout.setOnAction(e -> { Client.instance.logout(); stage.setScene(LoginView.createScene(stage)); });

        sidebar.getChildren().addAll(btnWall, btnMessages, btnFriends, spacer, btnLogout);
        return sidebar;
    }

    private static HBox createHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(10));
        header.setStyle("-fx-border-color: white; -fx-border-width: 0 0 1 0;");
        Label l = new Label("MiniNet_Hub"); l.setTextFill(Color.WHITE); l.setFont(Font.font(FONT_FAMILY, 18));
        header.getChildren().add(l);
        return header;
    }

    // Utils
    private static Button createMenuButton(String t) {
        Button b = new Button(t); b.setMaxWidth(Double.MAX_VALUE); b.setAlignment(Pos.CENTER_LEFT);
        b.setStyle("-fx-background-color: #000; -fx-text-fill: white; -fx-border-color: #333;");
        b.setFont(Font.font(FONT_FAMILY, 14));
        return b;
    }
    private static void styleTerminalField(TextField f) {
        f.setStyle("-fx-background-color: #000; -fx-text-fill: #0f0; -fx-border-color: white; -fx-font-family: "+FONT_FAMILY+";");
    }
    private static void styleTerminalTextArea(TextArea a) {
        a.setStyle("-fx-control-inner-background: #000; -fx-text-fill: #0f0; -fx-font-family: "+FONT_FAMILY+";");
    }
}