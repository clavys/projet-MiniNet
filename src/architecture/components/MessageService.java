package architecture.components;

import framework.Component;
import architecture.interfaces.IMessagePort;
import architecture.connectors.SQLConnector;

public class MessageService extends Component implements IMessagePort {

    private SQLConnector dbConnector;

    public MessageService() {
        super("MessageService");
    }

    public void setDbConnector(SQLConnector conn) {
        this.dbConnector = conn;
    }

    @Override
    public void sendMessage(String from, String to, String content) {
        // On crée une clé unique (ex: timestamp)
        String msgId = from + "_" + System.currentTimeMillis();
        // Format de stockage : "DESTINATAIRE:CONTENU"
        String data = to + ":" + content;

        printLog("Envoi message de " + from + " vers " + to);
        // On sauvegarde dans la table MESSAGES
        dbConnector.saveRecord("MESSAGES", msgId, data);
    }

    @Override
    public String checkMessages(String user) {
        // NOTE : Dans une vraie DB, on ferait "SELECT * FROM MESSAGES WHERE TO=user"
        // Ici, notre Storage simulé est basique (clé/valeur).
        // Pour simplifier l'exercice, on va juste simuler la lecture.
        printLog("Lecture de la boîte de réception de " + user);
        return "Vous avez 0 nouveaux messages (Simulation)";
    }
}