import architecture.components.Client;
import architecture.connectors.RPCConnector;

public class Main {
    public static void main(String[] args) {

        System.out.println("--- Démarrage du Client Console (Distribué) ---");

        // 1. On ne crée PLUS le backend (Storage, UserManager...) ici !
        // Ils sont gérés par le ServerMain de ton binôme.

        // 2. On prépare le Client et son Connecteur
        Client client = new Client();
        RPCConnector httpConn = new RPCConnector(); // C'est ton nouveau connecteur HTTP

        // 3. On branche le câble réseau
        // Note: setComponent ne sert plus à rien car l'URL est codée en dur dans le connecteur
        // Mais on injecte le connecteur dans le client
        client.setAuthConnector(httpConn);
        client.setPostConnector(httpConn);
        client.setMsgConnector(httpConn);

        // 4. On lance l'interface
        // IMPORTANT : Il faut que ServerMain tourne DÉJÀ dans une autre fenêtre !
        client.start();
    }
}