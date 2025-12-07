package front;

import architecture.components.Client;
import javafx.application.Application;
import javafx.stage.Stage;

public class MiniNet extends Application {

    @Override
    public void start(Stage primaryStage) {
        // On récupère la belle scène créée dans l'autre fichier
        if(Client.instance.check)
        primaryStage.setScene(LoginView.createScene(primaryStage));

        primaryStage.setTitle("MiniNet - Connexion");
        primaryStage.setResizable(false); // Empêche de déformer la fenêtre
        primaryStage.show();
    }
}