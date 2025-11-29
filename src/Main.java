// ... imports ...

import architecture.components.Client;
import architecture.components.Storage;
import architecture.components.UserManager;
import architecture.connectors.RPCConnector;
import architecture.connectors.SQLConnector;

public class Main {
    public static void main(String[] args) {
        // 1. Création des Composants
        Storage storage = new Storage(); // La base de données
        UserManager userMgr = new UserManager(); // Le gestionnaire
        Client client = new Client(); // L'interface

        // 2. Création des Connecteurs
        SQLConnector sqlConn = new SQLConnector();
        RPCConnector rpcConn = new RPCConnector();

        // 3. Câblage (Binding & Attachments)

        // A. Branchement de la DB
        // Manager -> SQLConnector -> Storage
        sqlConn.setComponent(storage);      // Le connecteur pointe vers le Storage
        userMgr.setDbConnector(sqlConn);    // Le Manager utilise le connecteur

        // B. Branchement du Client
        // Client -> RPCConnector -> UserManager
        rpcConn.setComponent(userMgr);
        client.setAuthConnector(rpcConn);

        // 4. Test du scénario
        client.start();
    }
}