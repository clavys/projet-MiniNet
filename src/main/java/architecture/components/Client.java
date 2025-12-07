package architecture.components;

import framework.Component;
import architecture.connectors.RPCConnector;
import front.MiniNet;
import javafx.application.Application;

import java.util.Scanner;

public class Client extends Component {

    // --- COULEURS ANSI (Pour embellir la console) ---
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_BLUE = "\u001B[34m";

    // --- PORTS REQUIS (Via Connecteurs) ---
    private RPCConnector authConnector;
    private RPCConnector msgConnector;
    private RPCConnector postConnector;

    // Pour simuler une session (qui est connecté ?)
    private String currentUser = null;

    public static Client instance;

    public Client() {

        super("Client Console");
        instance = this;
    }

    // --- BINDING (Setters pour l'injection des connecteurs) ---
    public void setAuthConnector(RPCConnector c) { this.authConnector = c; }
    public void setMsgConnector(RPCConnector c) { this.msgConnector = c; }
    public void setPostConnector(RPCConnector c) { this.postConnector = c; }

    // --- BOUCLE PRINCIPALE (UI) ---
    public void start() {
        new Thread(() -> {
            Application.launch(MiniNet.class);
        }).start();
    }

    public boolean login(String name, String pass ) {
        return authConnector.callLogin(name, pass);
    }

    public void register(String username, String pass) {
        authConnector.callRegister(username, pass);
    }

    // --- HANDLERS (Gestion des actions) ---

    private void handleRegister(Scanner scanner) {
        System.out.print("Pseudo : ");
        String user = scanner.nextLine();
        System.out.print("Mot de passe : ");
        String pass = scanner.nextLine();
        authConnector.callRegister(user, pass);
        System.out.println(ANSI_GREEN + ">> Demande d'inscription envoyée." + ANSI_RESET);
    }

    private void handleLogin(Scanner scanner) {
        System.out.print("Pseudo : ");
        String user = scanner.nextLine();
        System.out.print("Mot de passe : ");
        String pass = scanner.nextLine();

        boolean success = authConnector.callLogin(user, pass);
        if (success) {
            this.currentUser = user;
            System.out.println(ANSI_GREEN + ">> SUCCÈS : Bienvenue " + user + " !" + ANSI_RESET);
        } else {
            System.out.println(ANSI_RED + ">> ÉCHEC : Identifiants incorrects." + ANSI_RESET);
        }
    }

    private void handleSendMessage(Scanner scanner) {
        System.out.print("Destinataire : ");
        String to = scanner.nextLine();
        System.out.print("Message : ");
        String content = scanner.nextLine();

        msgConnector.callSendMessage(currentUser, to, content);
        System.out.println(ANSI_GREEN + ">> Message envoyé !" + ANSI_RESET);
    }

    private void handleReadMessages() {
        String inbox = msgConnector.callCheckMessages(currentUser);
        System.out.println("\n--- Vos Messages ---");
        System.out.println(inbox);
    }

    private void handleCreatePost(Scanner scanner) {
        System.out.print("Contenu du post : ");
        String content = scanner.nextLine();

        postConnector.callCreatePost(currentUser, content);
        System.out.println(ANSI_GREEN + ">> Post publié sur votre mur !" + ANSI_RESET);
    }

    private void handleGetWall() {
        String wall = postConnector.callGetWall(currentUser);
        System.out.println(wall);
    }

    // --- MÉTHODES POUR LES AMIS ---

    private void handleAddFriend(Scanner scanner) {
        System.out.print("Nom de l'ami à ajouter : ");
        String friend = scanner.nextLine();

        authConnector.callAddFriend(currentUser, friend);
        System.out.println(ANSI_GREEN + ">> Ami ajouté (simulé) !" + ANSI_RESET);
    }

    private void handleRemoveFriend(Scanner scanner) {
        System.out.print("Nom de l'ami à supprimer : ");
        String friend = scanner.nextLine();

        authConnector.callRemoveFriend(currentUser, friend);
        System.out.println(ANSI_GREEN + ">> Ami supprimé !" + ANSI_RESET);
    }

    // --- UTILITAIRE DE SÉCURITÉ ---
    private boolean checkAuth() {
        if (currentUser == null) {
            System.out.println(ANSI_RED + ">> ERREUR : Vous devez être connecté !" + ANSI_RESET);
            return false;
        }
        return true;
    }
}