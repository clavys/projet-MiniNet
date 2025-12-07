
import architecture.components.*;
import architecture.connectors.RPCConnector;
import architecture.connectors.SQLConnector;

public class Main {
    public static void main(String[] args) {
// 1. Composants
        Storage storage = new Storage();
        UserManager userMgr = new UserManager();
        MessageService msgSvc = new MessageService(); // <-- Nouveau
        PostManager postMgr = new PostManager();
        Client client = new Client();

        // 2. Connecteurs
        SQLConnector sqlConn = new SQLConnector();
        RPCConnector authConn = new RPCConnector(); // Pour l'auth
        RPCConnector msgConn = new RPCConnector();  // Pour les messages (ACME: RPC_Msg_Conn)
        RPCConnector postConn = new RPCConnector();    // Connecteur Post (ACME: RPC_Post_Conn)

        // 3. Câblage

        // Base de données (Multiplexage)
        sqlConn.setComponent(storage);
        userMgr.setDbConnector(sqlConn);
        msgSvc.setDbConnector(sqlConn);
        postMgr.setDbConnector(sqlConn);

        //  Côté Client (Front-end) -> Services
        // Circuit Auth
        authConn.setComponent(userMgr);
        client.setAuthConnector(authConn);

        // Circuit Message
        msgConn.setComponent(msgSvc);   // On branche le connecteur au service
        client.setMsgConnector(msgConn); // On donne le bout du câble au client

        // Circuit Post
        postConn.setComponent(postMgr);     // On branche le connecteur sur le PostManager
        client.setPostConnector(postConn);  // On donne l'autre bout au Client

        // 4. Start
        client.start();

        // 4. Test du scénario
        //client.start();
    }
}