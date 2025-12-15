package architecture.components;

import framework.Component;
import architecture.connectors.RPCConnector;
import front.MiniNet;
import javafx.application.Application;
import javafx.application.Platform;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

public class Client extends Component {

    private RPCConnector authConnector;
    private RPCConnector msgConnector;
    private RPCConnector postConnector;

    private String currentUser = null;
    private WebSocket eventBusSocket;
    private Consumer<String> uiCallback;

    public static Client instance;

    public Client() {
        super("Client Architectural");
        instance = this;
    }

    public void start() {
        new Thread(() -> Application.launch(MiniNet.class)).start();
    }

    // --- AUTH ---
    public boolean login(String username, String password) {
        boolean success = authConnector.callLogin(username, password);
        if (success) {
            this.currentUser = username;
            connectToEventBus(username);
        }
        return success;
    }

    public void register(String username, String password) {
        authConnector.callRegister(username, password);
    }

    public void logout() {
        this.currentUser = null;
        if (this.eventBusSocket != null) {
            this.eventBusSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Logout");
        }
    }

    // --- MESSAGES ---
    public void sendMessage(String to, String message) {
        if (checkAuth()) msgConnector.callSendMessage(currentUser, to, message);
    }

    public String readMessage() {
        if (!checkAuth()) return "";
        return msgConnector.callCheckMessages(currentUser);
    }

    // --- POSTS ---
    public void createPost(String content) {
        if (checkAuth()) postConnector.callCreatePost(currentUser, content);
    }

    public String getWall() {
        if (!checkAuth()) return "";
        return postConnector.callGetWall(currentUser);
    }

    // --- AMIS ---
    public void addFriend(String friendName) {
        if (checkAuth()) authConnector.callAddFriend(currentUser, friendName);
    }

    public void removeFriend(String friendName) {
        if (checkAuth()) authConnector.callRemoveFriend(currentUser, friendName);
    }

    public String getFriendsRaw() {
        if (!checkAuth()) return "";
        return authConnector.callGetFriends(currentUser);
    }

    public String getRecommendationsRaw() {
        if (!checkAuth()) return "";
        return authConnector.callGetRecommendations(currentUser);
    }

    // --- WEBSOCKET ---
    public void setUiCallback(Consumer<String> callback) {
        this.uiCallback = callback;
    }

    private void connectToEventBus(String username) {
        try {
            String wsUrl = "ws://localhost:7000/events?user=" + username;
            this.eventBusSocket = HttpClient.newHttpClient()
                    .newWebSocketBuilder()
                    .buildAsync(URI.create(wsUrl), new WebSocket.Listener() {
                        @Override
                        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                            if (uiCallback != null) {
                                Platform.runLater(() -> uiCallback.accept(data.toString()));
                            }
                            return WebSocket.Listener.super.onText(webSocket, data, last);
                        }
                    }).join();
        } catch (Exception e) {
            System.err.println("Erreur WS: " + e.getMessage());
        }
    }

    // --- HELPERS ---
    public boolean checkAuth() { return currentUser != null; }
    public String getCurrentUser() { return currentUser; }

    public void setAuthConnector(RPCConnector c) { this.authConnector = c; }
    public void setMsgConnector(RPCConnector c) { this.msgConnector = c; }
    public void setPostConnector(RPCConnector c) { this.postConnector = c; }
}