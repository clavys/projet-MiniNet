package architecture.components;

import framework.Component;
import architecture.connectors.RPCConnector;
import java.util.Scanner;

public class Client extends Component {

    // --- PORTS REQUIS (Via Connecteurs) ---
    private RPCConnector authConnector;
    private RPCConnector msgConnector;
    private RPCConnector postConnector;

    // Pour simuler une session (qui est connecté ?)
    private String currentUser = null;

    public Client() {
        super("Client Console");
    }

    // --- BINDING (Setters pour l'injection des connecteurs) ---
    public void setAuthConnector(RPCConnector c) { this.authConnector = c; }
    public void setMsgConnector(RPCConnector c) { this.msgConnector = c; }
    public void setPostConnector(RPCConnector c) { this.postConnector = c; }

    // --- BOUCLE PRINCIPALE (UI) ---
    public void start() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("=================================");
        System.out.println("   BIENVENUE SUR MININET V1.0    ");
        System.out.println("=================================");

        while (running) {
            System.out.println("\n--- MENU ---");
            if (currentUser == null) {
                // Mode déconnecté
                System.out.println("1. S'inscrire (Register)");
                System.out.println("2. Se connecter (Login)");
                System.out.println("0. Quitter");
            } else {
                // Mode connecté
                System.out.println("Connecté en tant que : [" + currentUser + "]");
                System.out.println("---------------------------------");
                System.out.println("3. Envoyer un message privé");
                System.out.println("4. Lire mes messages");
                System.out.println("5. Publier sur le mur (Post)");
                System.out.println("6. Voir le mur (Fil d'actualité)");
                System.out.println("9. Se déconnecter");
                System.out.println("0. Quitter");
            }
            System.out.print("Votre choix > ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1": handleRegister(scanner); break;
                case "2": handleLogin(scanner); break;

                case "3":
                    if(checkAuth()) handleSendMessage(scanner);
                    break;
                case "4":
                    if(checkAuth()) handleReadMessages();
                    break;
                case "5":
                    if(checkAuth()) handleCreatePost(scanner);
                    break;
                case "6":
                    if(checkAuth()) handleGetWall();
                    break;

                case "9":
                    this.currentUser = null;
                    System.out.println(">> Déconnexion réussie.");
                    break;
                case "0":
                    System.out.println("Au revoir !");
                    running = false;
                    break;
                default: System.out.println("Choix invalide.");
            }
        }
        scanner.close();
    }

    // --- HANDLERS (Gestion des actions) ---

    private void handleRegister(Scanner scanner) {
        System.out.print("Pseudo : ");
        String user = scanner.nextLine();
        System.out.print("Mot de passe : ");
        String pass = scanner.nextLine();
        authConnector.callRegister(user, pass);
        System.out.println(">> Demande d'inscription envoyée.");
    }

    private void handleLogin(Scanner scanner) {
        System.out.print("Pseudo : ");
        String user = scanner.nextLine();
        System.out.print("Mot de passe : ");
        String pass = scanner.nextLine();

        boolean success = authConnector.callLogin(user, pass);
        if (success) {
            this.currentUser = user;
            System.out.println(">> SUCCÈS : Bienvenue " + user + " !");
        } else {
            System.out.println(">> ÉCHEC : Identifiants incorrects.");
        }
    }

    private void handleSendMessage(Scanner scanner) {
        System.out.print("Destinataire : ");
        String to = scanner.nextLine();
        System.out.print("Message : ");
        String content = scanner.nextLine();

        // Appel via le connecteur Message
        msgConnector.callSendMessage(currentUser, to, content);
        System.out.println(">> Message envoyé !");
    }

    private void handleReadMessages() {
        // Appel via le connecteur Message
        String inbox = msgConnector.callCheckMessages(currentUser);
        System.out.println("\n--- Vos Messages ---");
        System.out.println(inbox);
    }

    private void handleCreatePost(Scanner scanner) {
        System.out.print("Contenu du post : ");
        String content = scanner.nextLine();

        // Appel via le connecteur Post
        postConnector.callCreatePost(currentUser, content);
        System.out.println(">> Post publié sur votre mur !");
    }

    private void handleGetWall() {
        // Appel via le connecteur Post
        String wall = postConnector.callGetWall(currentUser);
        System.out.println(wall);
    }

    // Utilitaire pour sécuriser le menu
    private boolean checkAuth() {
        if (currentUser == null) {
            System.out.println(">> ERREUR : Vous devez être connecté !");
            return false;
        }
        return true;
    }
}