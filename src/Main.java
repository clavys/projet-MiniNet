// ... imports ...

import architecture.components.Client;
import architecture.components.MessageService;
import architecture.components.Storage;
import architecture.components.UserManager;
import architecture.connectors.RPCConnector;
import architecture.connectors.SQLConnector;

public class Main {
    public static void main(String[] args) {
// 1. Composants
        Storage storage = new Storage();
        UserManager userMgr = new UserManager();
        MessageService msgSvc = new MessageService(); // <-- Nouveau
        Client client = new Client();

        // 2. Connecteurs
        SQLConnector sqlConn = new SQLConnector();
        RPCConnector authConn = new RPCConnector(); // Pour l'auth
        RPCConnector msgConn = new RPCConnector();  // Pour les messages (ACME: RPC_Msg_Conn)

        // 3. Câblage

        // Base de données (Multiplexage)
        sqlConn.setComponent(storage);
        userMgr.setDbConnector(sqlConn);
        msgSvc.setDbConnector(sqlConn); // <-- Le MessageService a accès à la DB

        // Réseau (RPC)
        // Circuit Auth
        authConn.setComponent(userMgr);
        client.setAuthConnector(authConn);

        // Circuit Message
        msgConn.setComponent(msgSvc);   // On branche le connecteur au service
        client.setMsgConnector(msgConn); // On donne le bout du câble au client

        // 4. Start
        client.start();

        // 4. Test du scénario
        client.start();
    }
}