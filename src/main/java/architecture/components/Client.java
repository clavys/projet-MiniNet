package architecture.components;

import framework.Component;
import architecture.connectors.RPCConnector;
import front.MiniNet; // Assure-toi que c'est bien le nom de ta classe Main JavaFX
import javafx.application.Application;

public class Client extends Component {

    // --- PORTS REQUIS (Connecteurs vers le serveur) ---
    private RPCConnector authConnector;
    private RPCConnector msgConnector;
    private RPCConnector postConnector;

    // État de la session
    private String currentUser;

    // Singleton pour Front
    public static Client instance;

    public Client() {
        super("Client Architectural");
        instance = this;
        currentUser = null;
    }

    public String getUserName(){
        return currentUser;
    }
    // --- DÉMARRAGE ---
    public void start() {
        // On lance l'interface graphique JavaFX
        new Thread(() -> {
            Application.launch(MiniNet.class);
        }).start();
    }

    // ==========================================
    //       API PUBLIQUE (Appelée par l'UI)
    // ==========================================

    // --- AUTHENTIFICATION ---

    public boolean login(String name, String pass) {
        boolean success = authConnector.callLogin(name, pass);
        if (success) {
            this.currentUser = name;
            System.out.println("LOG >> Session ouverte pour : " + currentUser);
        }
        return success;
    }

    public void register(String username, String pass) {
        authConnector.callRegister(username, pass);
    }

    public void logout() {
        this.currentUser = null;
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

    // --- MUR / POSTS  ---

    public void createPost(String content) {
        if (checkAuth()) {
            postConnector.callCreatePost(currentUser, content);
        }
    }

    public String getWall() {
        if (!checkAuth()) return "";
        // Retourne le String directement pour l'afficher dans le TextArea du Hub
        return postConnector.callGetWall(currentUser);
    }

    // --- AMIS ---

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

    // Check
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