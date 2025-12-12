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

public class LoginView {

    private static final String FONT_FAMILY = "Monospaced";

    public static Scene createScene(Stage stage) {

        // 1. LE TITRE (Style prompt shell)
        Label titleLabel = new Label(">_ MiniNet_v1.0");
        titleLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.WHITE);

        Label subtitleLabel = new Label("AUTH_REQUIRED...");
        subtitleLabel.setFont(Font.font(FONT_FAMILY, 12));
        subtitleLabel.setTextFill(Color.web("#CCCCCC")); // Gris clair

        // 2. LES CHAMPS
        TextField userField = new TextField();
        userField.setPromptText("$username");
        styleTerminalField(userField);

        PasswordField passField = new PasswordField();
        passField.setPromptText("$password");
        styleTerminalField(passField);

        // 3. LES BOUTONS
        Button btnLogin = new Button("[ LOGIN ]");
        styleTerminalButton(btnLogin);

        Button btnRegister = new Button("[ REGISTER ]");
        styleTerminalButton(btnRegister);

        // --- ACTION DES BOUTONS ---

        // Event login
        btnLogin.setOnAction(e -> {
            System.out.println("CMD >> LOGIN_ATTEMPT : " + userField.getText());
            if (!Client.instance.login(userField.getText(), passField.getText())){
                subtitleLabel.setText(">> ERROR: ACCESS_DENIED");
                subtitleLabel.setTextFill(Color.web("#FF0000")); // Rouge terminal
            } else {
                subtitleLabel.setText(">> SUCCESS: ACCESS_GRANTED");
                subtitleLabel.setTextFill(Color.web("#00FF00"));
                stage.setScene(HubView.createScene(stage));
            }
        });

        // Event register
        btnRegister.setOnAction(e -> {
            System.out.println("CMD >> REGISTER_ATTEMPT");
            Client.instance.register(userField.getText(), passField.getText());
            subtitleLabel.setText(">> NEW_USER_HELLO. PLS LOGIN.");
            subtitleLabel.setTextFill(Color.web("#00FF00")); // Vert terminal
        });

        // 4. LE LAYOUT
        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));
        // FOND NOIR
        layout.setStyle("-fx-background-color: #000000; -fx-border-color: #FFFFFF; -fx-border-width: 2px;");

        layout.getChildren().addAll(
                titleLabel,
                subtitleLabel,
                new Region() {{ setMinHeight(20); }},
                userField,
                passField,
                new Region() {{ setMinHeight(20); }},
                btnLogin,
                btnRegister
        );

        // 5. SCÈNE
        return new Scene(layout, 400, 500);
    }

    // --- STYLE RETRO / TERMINAL ---

    private static void styleTerminalField(TextField field) {
        field.setPrefHeight(35);
        // Fond noir, texte blanc, bordure blanche, pas d'arrondi
        String styleBase =
                "-fx-background-color: #000000;" +
                        "-fx-text-fill: #FFFFFF;" +
                        "-fx-prompt-text-fill: #555555;" +
                        "-fx-border-color: #FFFFFF;" +
                        "-fx-border-width: 1px;" +
                        "-fx-background-radius: 0;" +
                        "-fx-border-radius: 0;" +
                        "-fx-font-family: '" + FONT_FAMILY + "';";

        field.setStyle(styleBase);

        // Focus : la bordure devient plus épaisse ou change de style
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                // Bordure plus épaisse au focus
                field.setStyle(styleBase + "-fx-border-width: 2px;");
            } else {
                field.setStyle(styleBase);
            }
        });
    }

    private static void styleTerminalButton(Button btn) {
        btn.setPrefHeight(35);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setCursor(javafx.scene.Cursor.HAND);
        btn.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 14));

        // État normal : Noir avec bordure blanche
        String styleNormal =
                "-fx-background-color: #000000;" +
                        "-fx-text-fill: #FFFFFF;" +
                        "-fx-border-color: #FFFFFF;" +
                        "-fx-border-width: 1px;" +
                        "-fx-background-radius: 0;";

        // État survolé (Hover) : Inversion des couleurs (Blanc avec texte noir)
        String styleHover =
                "-fx-background-color: #FFFFFF;" +
                        "-fx-text-fill: #000000;" +
                        "-fx-border-color: #FFFFFF;" +
                        "-fx-border-width: 1px;" +
                        "-fx-background-radius: 0;";

        btn.setStyle(styleNormal);

        btn.setOnMouseEntered(e -> btn.setStyle(styleHover));
        btn.setOnMouseExited(e -> btn.setStyle(styleNormal));
    }
}