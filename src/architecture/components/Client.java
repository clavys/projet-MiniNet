package architecture.components;

import framework.Component;
import architecture.connectors.RPCConnector;
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
                // Mode connecté : Pseudo en ROUGE comme demandé
                System.out.println("Connecté en tant que : [" + ANSI_RED + currentUser + ANSI_RESET + "]");
                System.out.println("---------------------------------");
                System.out.println("3. Envoyer un message privé");
                System.out.println("4. Lire mes messages");
                System.out.println("5. Publier sur le mur (Post)");
                System.out.println("6. Voir le mur (Fil d'actualité)");

                // --- OPTIONS AJOUTÉES ---
                System.out.println("7. Ajouter un ami");
                System.out.println("8. Supprimer un ami");
                // ------------------------

                System.out.println("9. Se déconnecter");
                System.out.println("0. Quitter");
            }
            System.out.print("Votre choix > ");
            String choice = scanner.nextLine();

            switch (choice) {
                // Cas accessibles hors connexion
                case "1": handleRegister(scanner); break;
                case "2": handleLogin(scanner); break;
                case "0":
                    System.out.println("Au revoir !");
                    running = false;
                    break;

                // Cas nécessitant une connexion
                case "3": if(checkAuth()) handleSendMessage(scanner); break;
                case "4": if(checkAuth()) handleReadMessages(); break;
                case "5": if(checkAuth()) handleCreatePost(scanner); break;
                case "6": if(checkAuth()) handleGetWall(); break;

                // --- CASES AJOUTÉS ---
                case "7": if(checkAuth()) handleAddFriend(scanner); break;
                case "8": if(checkAuth()) handleRemoveFriend(scanner); break;

                case "9":
                    this.currentUser = null;
                    System.out.println(ANSI_BLUE + ">> Déconnexion réussie." + ANSI_RESET);
                    break;

                default:
                    System.out.println("Choix invalide.");
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