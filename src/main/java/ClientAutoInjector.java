import architecture.connectors.RPCConnector;

public class ClientAutoInjector {

    public static void main(String[] args) {
        // On utilise le connecteur existant (la "télécommande" vers le serveur)
        RPCConnector connector = new RPCConnector();

        System.out.println("--- DÉBUT DE L'INJECTION CLIENT (AUTOMATIQUE) ---");

        // ==========================================
        // 1. INSCRIPTION DES UTILISATEURS (REGISTER)
        // ==========================================
        System.out.println("\n>> Création des comptes...");
        registerUser(connector, "Alice", "123");
        registerUser(connector, "Bob", "123");
        registerUser(connector, "Charly", "123");
        registerUser(connector, "David", "123"); // Un 4ème pour les recos

        // ==========================================
        // 2. CRÉATION DES LIENS D'AMITIÉ (FRIENDS)
        // ==========================================
        System.out.println("\n>> Création du graphe social...");

        // Alice est amie avec Bob
        connector.callAddFriend("Alice", "Bob");
        System.out.println(" - Alice <-> Bob");

        // Bob est ami avec Charly
        connector.callAddFriend("Bob", "Charly");
        System.out.println(" - Bob <-> Charly");

        // Charly est ami avec David
        connector.callAddFriend("Charly", "David");
        System.out.println(" - Charly <-> David");

        // (Cela devrait permettre de recommander Charly à Alice, et David à Bob)

        // ==========================================
        // 3. PUBLICATION DE POSTS (WALL)
        // ==========================================
        System.out.println("\n>> Publication sur les murs...");

        connector.callCreatePost("Alice", "Bonjour tout le monde ! #PremierPost");
        System.out.println(" - Post de Alice envoyé.");

        sleep(500); // Petit délai pour varier les timestamps

        connector.callCreatePost("Bob", "Quelqu'un est chaud pour un café ?");
        System.out.println(" - Post de Bob envoyé.");

        sleep(500);

        connector.callCreatePost("Alice", "Il fait super beau aujourd'hui.");
        System.out.println(" - 2ème Post de Alice envoyé.");

        // ==========================================
        // 4. ENVOI DE MESSAGES PRIVÉS (MESSAGES)
        // ==========================================
        System.out.println("\n>> Envoi de messages privés...");

        connector.callSendMessage("Bob", "Alice", "Salut Alice, tu as vu mon post ?");
        System.out.println(" - Message Bob -> Alice envoyé.");

        connector.callSendMessage("Alice", "Bob", "Oui je viens de le voir !");
        System.out.println(" - Message Alice -> Bob envoyé.");

        connector.callSendMessage("Charly", "Bob", "Yo mec, ça va ?");
        System.out.println(" - Message Charly -> Bob envoyé.");

        System.out.println("\n-------------------------------------------");
        System.out.println("SUCCÈS : Base de données remplie via l'API !");
        System.out.println("Vous pouvez maintenant lancer le vrai Client console.");
    }

    // Petite méthode utilitaire pour éviter de répéter le code
    private static void registerUser(RPCConnector conn, String u, String p) {
        conn.callRegister(u, p);
        System.out.println(" + Compte créé : " + u);
    }

    // Petite pause pour que les timestamps ne soient pas identiques
    private static void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) {}
    }
}