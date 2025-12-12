package architecture.components;

import framework.Component;
import architecture.connectors.RPCConnector;
import front.MiniNet; // Ta classe Main JavaFX
import javafx.application.Application;

// Imports nécessaires pour les WebSockets
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;

public class Client extends Component {

    // --- PORTS REQUIS ---
    private RPCConnector authConnector;
    private RPCConnector msgConnector;
    private RPCConnector postConnector;

    // État de la session
    private String currentUser = null;

    // Maintien de la connexion WebSocket
    private WebSocket eventBusSocket;

    // Singleton pour accès facile depuis le Front
    public static Client instance;

    public Client() {
        super("Client Architectural");
        instance = this;
    }

    public void start() {
        // On lance l'interface graphique JavaFX dans un thread à part
        new Thread(() -> {
            Application.launch(MiniNet.class);
        }).start();
    }

    // ==========================================
    //       API PUBLIQUE (Appelée par l'UI)
    // ==========================================

    // --- AUTHENTIFICATION ---

    public boolean login(String username, String password) {
        boolean success = authConnector.callLogin(username, password);

        if (success) {
            this.currentUser = username;
            printLog("Session ouverte pour : " + currentUser);

            //On connecte le WebSocket
            connectToEventBus(username);
        }
        return success;
    }

    public void register(String username, String password) {
        authConnector.callRegister(username, password);
    }

    public void logout() {
        this.currentUser = null;
        // On ferme proprement le WebSocket si on se déconnecte
        if (this.eventBusSocket != null) {
            this.eventBusSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Logout");
        }
    }

    // --- MESSAGERIE PRIVÉE ---

    public void sendMessage(String to, String message) {
        if (checkAuth()) {
            msgConnector.callSendMessage(currentUser, to, message);
        }
    }

    public String readMessage() {
        if (!checkAuth()) return "Veuillez vous connecter.";
        return msgConnector.callCheckMessages(currentUser);
    }

    // --- MUR / POSTS ---

    public void createPost(String content) {
        if (checkAuth()) {
            postConnector.callCreatePost(currentUser, content);
        }
    }

    public String getWall() {
        if (!checkAuth()) return "";
        return postConnector.callGetWall(currentUser);
    }

    // --- GESTION AMIS (Classique) ---

    public void addFriend(String friendName) {
        if (checkAuth()) {
            authConnector.callAddFriend(currentUser, friendName);
        }
    }

    public void removeFriend(String friendName) {
        if (checkAuth()) {
            authConnector.callRemoveFriend(currentUser, friendName);
        }
    }

    // --- FONCTIONNALITÉS AVANCÉES  ---

    // Récupère la liste des amis sous forme de texte propre pour l'UI
    public String getFriendsList() {
        if (!checkAuth()) return "";

        String rawResponse = authConnector.callGetFriends(currentUser);
        if (rawResponse == null || rawResponse.trim().isEmpty()) {
            return "Vous n'avez pas encore d'amis.";
        }

        // Formattage pour l'affichage dans un Label ou TextArea
        return rawResponse.replace(",", "\n- ");
    }

    // Récupère les recommandations (Amis d'amis)
    public String getRecommendations() {
        if (!checkAuth()) return "";

        String rawResponse = authConnector.callGetRecommendations(currentUser);
        if (rawResponse == null || rawResponse.trim().isEmpty() || rawResponse.contains("Aucune")) {
            return "Aucune recommandation pour le moment.";
        }

        return "Vous connaissez peut-être :\n? " + rawResponse.replace(",", "\n? ");
    }


    // ==========================================
    //       GESTION WEBSOCKET (Event Bus)
    // ==========================================

    private void connectToEventBus(String username) {
        try {
            // L'URL du WebSocket (Backend Javalin)
            String wsUrl = "ws://localhost:7000/events?user=" + username;

            this.eventBusSocket = HttpClient.newHttpClient()
                    .newWebSocketBuilder()
                    .buildAsync(URI.create(wsUrl), new WebSocket.Listener() {

                        @Override
                        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                            // Au lieu d'afficher dans la console, on loggue via le framework
                            // Idéalement, ici on pourrait déclencher une mise à jour de l'UI via un Observer pattern
                            printLog("[NOTIFICATION REÇUE] " + data);
                            return WebSocket.Listener.super.onText(webSocket, data, last);
                        }
                    })
                    .join(); // On attend que la connexion se fasse

            printLog("Connecté au Bus d'événements (WebSocket).");

        } catch (Exception e) {
            System.err.println("Erreur connexion WebSocket : " + e.getMessage());
        }
    }

    // ==========================================
    //           UTILITAIRES & SETTERS

    public boolean checkAuth() {
        return currentUser != null;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    // Injection des dépendances (Binding)
    public void setAuthConnector(RPCConnector c) { this.authConnector = c; }
    public void setMsgConnector(RPCConnector c) { this.msgConnector = c; }
    public void setPostConnector(RPCConnector c) { this.postConnector = c; }
}