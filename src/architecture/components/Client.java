package architecture.components;

import framework.Component;
import architecture.connectors.RPCConnector;
import java.util.Scanner;

public class Client extends Component {

    // Le câble vers le monde extérieur
    private RPCConnector authConnector;
    private RPCConnector msgConnector;
    public Client() {
        super("Client Console");
    }

    // Binding : On branche le câble
    public void setAuthConnector(RPCConnector connector) {
        this.authConnector = connector;
    }
    public void setMsgConnector(RPCConnector c) {
        this.msgConnector = c;
    }

    // La boucle principale (L'interface graphique du pauvre)
    public void start() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("=================================");
        System.out.println("   BIENVENUE SUR MININET V1.0    ");
        System.out.println("=================================");

        while (running) {
            System.out.println("\n--- MENU ---");
            System.out.println("1. S'inscrire (Register)");
            System.out.println("2. Se connecter (Login)");
            System.out.println("3. Quitter");
            System.out.print("Votre choix > ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    handleRegister(scanner);
                    break;
                case "2":
                    handleLogin(scanner);
                    break;
                case "3":
                    System.out.println("Au revoir !");
                    running = false;
                    break;
                default:
                    System.out.println("Choix invalide.");
            }
        }
        scanner.close();
    }

    // --- Gestion des écrans ---

    private void handleRegister(Scanner scanner) {
        System.out.println("\n[INSCRIPTION]");
        System.out.print("Choisissez un pseudo : ");
        String user = scanner.nextLine();
        System.out.print("Choisissez un mot de passe : ");
        String pass = scanner.nextLine();

        // APPEL ARCHITECTURAL
        // Le client délègue tout au connecteur
        authConnector.callRegister(user, pass);

        System.out.println(">> Demande d'inscription envoyée !");
    }

    private void handleLogin(Scanner scanner) {
        System.out.println("\n[CONNEXION]");
        System.out.print("Pseudo : ");
        String user = scanner.nextLine();
        System.out.print("Mot de passe : ");
        String pass = scanner.nextLine();

        // APPEL ARCHITECTURAL
        boolean success = authConnector.callLogin(user, pass);

        if (success) {
            System.out.println(">> SUCCÈS : Vous êtes connecté en tant que " + user);
            // Ici, on pourrait lancer un sous-menu pour voir les posts...
        } else {
            System.out.println(">> ÉCHEC : Pseudo ou mot de passe incorrect.");
        }
    }
}