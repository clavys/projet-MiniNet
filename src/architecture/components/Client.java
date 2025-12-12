package architecture.components;

import framework.Component;
import architecture.connectors.RPCConnector;

import java.net.URI;
import java.net.http.HttpClient;
import java.util.Scanner;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;

public class Client extends Component {

    // Codes couleurs pour la lisibilité
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_CYAN = "\u001B[36m";

    private RPCConnector authConnector;
    private RPCConnector msgConnector;
    private RPCConnector postConnector;

    private String currentUser = null;

    public Client() {
        super("Client Console");
    }

    public void setAuthConnector(RPCConnector c) { this.authConnector = c; }
    public void setMsgConnector(RPCConnector c) { this.msgConnector = c; }
    public void setPostConnector(RPCConnector c) { this.postConnector = c; }

    // On garde une référence pour empêcher la déconnexion
    private WebSocket eventBusSocket;

    //flag
    private boolean isViewingMessages = false;

    public void start() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println(ANSI_CYAN + "=================================");
        System.out.println("   DEBUGGER CLI - MININET V1.1    ");
        System.out.println("=================================" + ANSI_RESET);

        while (running) {
            printMenu();
            System.out.print(ANSI_YELLOW + "Votre choix > " + ANSI_RESET);
            String choice = scanner.nextLine();

            switch (choice) {
                // --- Hors Connexion ---
                case "1": handleRegister(scanner); break;
                case "2": handleLogin(scanner); break;

                // --- Actions Sociales ---
                case "3": if(checkAuth()) handleSendMessage(scanner); break;
                case "4": if(checkAuth()) handleReadMessages(scanner); break;
                case "5": if(checkAuth()) handleCreatePost(scanner); break;
                case "6": if(checkAuth()) handleGetWall(); break;

                // --- Gestion Amis (Ajouts) ---
                case "7": if(checkAuth()) handleAddFriend(scanner); break;
                case "8": if(checkAuth()) handleRemoveFriend(scanner); break;
                case "9": if(checkAuth()) handleListFriends(); break;       // NOUVEAU
                case "10": if(checkAuth()) handleGetRecommendations(); break; // NOUVEAU

                // --- Système ---
                case "0":
                    this.currentUser = null;
                    System.out.println(ANSI_BLUE + ">> Déconnexion..." + ANSI_RESET);
                    break;
                case "Q": // Pour quitter complètement
                case "q":
                    System.out.println("Arrêt du client.");
                    running = false;
                    break;

                default:
                    System.out.println(ANSI_RED + "Choix invalide." + ANSI_RESET);
            }
        }
        scanner.close();
    }

    private void printMenu() {
        System.out.println("\n--- MENU ---");
        if (currentUser == null) {
            System.out.println("1. S'inscrire");
            System.out.println("2. Se connecter");
            System.out.println("Q. Quitter l'application");
        } else {
            System.out.println("Utilisateur : [" + ANSI_GREEN + currentUser + ANSI_RESET + "]");
            System.out.println("--- Messagerie & Mur ---");
            System.out.println("3. Envoyer MP");
            System.out.println("4. Lire mes MP");
            System.out.println("5. Poster sur le mur");
            System.out.println("6. Voir le fil d'actu");
            System.out.println("--- Graphe Social ---");
            System.out.println("7. Ajouter un ami");
            System.out.println("8. Supprimer un ami");
            System.out.println("9. " + ANSI_CYAN + "Voir ma liste d'amis" + ANSI_RESET);
            System.out.println("10. " + ANSI_CYAN + "Voir les recommandations (Amis d'amis)" + ANSI_RESET);
            System.out.println("---------------------");
            System.out.println("0. Se déconnecter");
        }
    }

    // --- HANDLERS EXISTANTS (Légèrement nettoyés) ---

    private void handleRegister(Scanner scanner) {
        System.out.print("Nouveau Pseudo : "); String user = scanner.nextLine();
        System.out.print("Mot de passe : "); String pass = scanner.nextLine();
        authConnector.callRegister(user, pass);
    }

    private void handleLogin(Scanner scanner) {
        System.out.print("Pseudo : "); String user = scanner.nextLine();
        System.out.print("Mot de passe : "); String pass = scanner.nextLine();

        if (authConnector.callLogin(user, pass)) {
            this.currentUser = user;
            System.out.println(ANSI_GREEN + ">> Connecté !" + ANSI_RESET);

            // --- AJOUT : On branche le câble WebSocket ---
            connectToEventBus(user);
            // ---------------------------------------------

        } else {
            System.out.println(ANSI_RED + ">> Erreur login." + ANSI_RESET);
        }
    }

    private void handleSendMessage(Scanner scanner) {
        System.out.print("Destinataire : "); String to = scanner.nextLine();
        System.out.print("Message : "); String content = scanner.nextLine();
        msgConnector.callSendMessage(currentUser, to, content);
    }

    private void handleReadMessages(Scanner scanner) {
        // 1. On ACTIVE le mode Live
        this.isViewingMessages = true;

        System.out.println("--- MES MESSAGES (Mode Live) ---");
        // Affichage initial
        System.out.println(ANSI_BLUE + msgConnector.callCheckMessages(currentUser) + ANSI_RESET);
        System.out.println("\n[En attente de nouveaux messages... Appuyez sur Entrée pour sortir]");

        // 2. On BLOQUE le programme ici tant que l'utilisateur n'appuie pas sur Entrée
        scanner.nextLine();

        // 3. On DÉSACTIVE le mode Live en sortant
        this.isViewingMessages = false;
        System.out.println("Sortie du mode lecture.");
    }

    private void handleCreatePost(Scanner scanner) {
        System.out.print("Contenu : "); String content = scanner.nextLine();
        postConnector.callCreatePost(currentUser, content);
    }

    private void handleGetWall() {
        System.out.println(postConnector.callGetWall(currentUser));
    }

    private void handleAddFriend(Scanner scanner) {
        System.out.print("Ami à ajouter : "); String friend = scanner.nextLine();
        authConnector.callAddFriend(currentUser, friend);
        System.out.println("Requête envoyée.");
    }

    private void handleRemoveFriend(Scanner scanner) {
        System.out.print("Ami à supprimer : "); String friend = scanner.nextLine();
        authConnector.callRemoveFriend(currentUser, friend);
        System.out.println("Requête envoyée.");
    }

    // --- NOUVEAUX HANDLERS ---

    private void handleListFriends() {
        System.out.println("Récupération de la liste d'amis...");
        String response = authConnector.callGetFriends(currentUser);
        // Le serveur renvoie "Titi,Toto,Tata" ou une chaîne vide
        if (response == null || response.trim().isEmpty()) {
            System.out.println(ANSI_YELLOW + "Vous n'avez pas encore d'amis." + ANSI_RESET);
        } else {
            String[] friends = response.split(",");
            System.out.println(ANSI_GREEN + "Vos amis (" + friends.length + ") :" + ANSI_RESET);
            for (String f : friends) {
                System.out.println(" - " + f);
            }
        }
    }

    private void handleGetRecommendations() {
        System.out.println("Calcul des recommandations...");
        String response = authConnector.callGetRecommendations(currentUser);

        if (response == null || response.trim().isEmpty() || response.contains("Aucune")) {
            System.out.println(ANSI_YELLOW + "Aucune recommandation pour le moment." + ANSI_RESET);
        } else {
            String[] recos = response.split(",");
            System.out.println(ANSI_CYAN + "Vous connaissez peut-être :" + ANSI_RESET);
            for (String r : recos) {
                System.out.println(" ? " + r);
            }
        }
    }

    private boolean checkAuth() {
        if (currentUser == null) {
            System.out.println(ANSI_RED + ">> ERREUR : Connectez-vous d'abord !" + ANSI_RESET);
            return false;
        }
        return true;
    }

    private void connectToEventBus(String username) {
        try {
            String wsUrl = "ws://localhost:7000/events?user=" + username;

            this.eventBusSocket = HttpClient.newHttpClient()
                    .newWebSocketBuilder()
                    .buildAsync(URI.create(wsUrl), new WebSocket.Listener() {

                        @Override
                        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {

                            // CORRECTION ICI : On utilise Client.this pour accéder au flag
                            if (Client.this.isViewingMessages) {

                                // On récupère les messages à jour (Client.this est optionnel ici mais plus clair)
                                String freshMessages = Client.this.msgConnector.callCheckMessages(currentUser);

                                System.out.println("\n\n\n========================================");
                                System.out.println("   NOUVEAU MESSAGE REÇU (ACTUALISÉ)   ");
                                System.out.println("========================================");
                                System.out.println(ANSI_BLUE + freshMessages + ANSI_RESET);
                                System.out.println("\n[Mode Live] Appuyez sur Entrée pour quitter...");

                            } else {
                                System.out.println("\n" + ANSI_RED + ">>> NOTIF : " + data + ANSI_RESET);
                                System.out.print(ANSI_YELLOW + "Votre choix > " + ANSI_RESET);
                            }

                            return WebSocket.Listener.super.onText(webSocket, data, last);
                        }
                    })
                    .join();

            System.out.println(ANSI_CYAN + "[Sys] Connecté au Bus d'événements." + ANSI_RESET);

        } catch (Exception e) {
            System.err.println("Erreur connexion WebSocket : " + e.getMessage());
        }
    }
}