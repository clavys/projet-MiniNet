package architecture.connectors;

import framework.Connector;
import architecture.interfaces.IUserPort;
import architecture.interfaces.IPostPort;
import architecture.interfaces.IMessagePort;

/**
 * Connecteur RPC (Remote Procedure Call)
 * Rôle : Transporter les appels de méthodes du Client vers les Managers.
 * Glue : Synchrone (Call-Return)
 */
public class RPCConnector extends Connector {

    // Le connecteur peut être relié à l'un de ces trois types de ports
    private IUserPort userPort;
    private IPostPort postPort;
    private IMessagePort msgPort;

    public RPCConnector() {
        super("RPC Call-Return");
    }

    /**
     * Méthode de Binding (Héritée du Framework)
     * Détecte automatiquement à quel type de composant on se connecte.
     */
    @Override
    public void setComponent(Object component) {
        switch (component) {
            case IUserPort iUserPort -> {
                this.userPort = iUserPort;
                printLog("Connecté au port Utilisateur");
            }
            case IPostPort iPostPort -> {
                this.postPort = iPostPort;
                printLog("Connecté au port Publication");
            }
            case IMessagePort iMessagePort -> {
                this.msgPort = iMessagePort;
                printLog("Connecté au port Message");
            }
            case null, default -> System.err.println("ERREUR : Type de composant incompatible pour RPCConnector");
        }
    }


    // SERVICES AUTHENTIFICATION (Relayés vers UserManager)


    public void callRegister(String username, String password) {
        if (userPort != null) {
            printLog(">> register(" + username + ")");
            userPort.register(username, password);
        } else {
            printError("Register", "UserManager");
        }
    }

    public boolean callLogin(String username, String password) {
        if (userPort != null) {
            printLog(">> login(" + username + ")");
            return userPort.login(username, password);
        } else {
            printError("Login", "UserManager");
            return false;
        }
    }

    // SERVICES MESSAGERIE (Relayés vers MessageService)


    public void callSendMessage(String from, String to, String content) {
        if (msgPort != null) {
            printLog(">> sendMessage(" + from + " -> " + to + ")");
            msgPort.sendMessage(from, to, content);
        } else {
            printError("SendMessage", "MessageService");
        }
    }

    public String callCheckMessages(String user) {
        if (msgPort != null) {
            printLog(">> checkMessages(" + user + ")");
            return msgPort.checkMessages(user);
        } else {
            printError("CheckMessages", "MessageService");
            return "Erreur de connexion";
        }
    }


    // SERVICES PUBLICATION (Relayés vers PostManager)

    public void callCreatePost(String author, String content) {
        if (postPort != null) {
            printLog(">> createPost(" + author + ")");
            postPort.createPost(author, content);
        } else {
            printError("CreatePost", "PostManager");
        }
    }

    public String callGetWall(String user) {
        if (postPort != null) {
            printLog(">> getWall(" + user + ")");
            return postPort.getWall(user);
        } else {
            printError("GetWall", "PostManager");
            return "Erreur de connexion";
        }
    }

    // --- Utilitaires internes ---

    private void printLog(String msg) {
        // Simule le passage réseau
        System.out.println("   [RPC-Transport] " + msg);
    }

    private void printError(String method, String target) {
        System.err.println("   [RPC-Erreur] Impossible d'appeler " + method + " : Le connecteur n'est pas relié au " + target);
    }
}