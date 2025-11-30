package architecture.components;

import framework.Component;
import architecture.interfaces.IMessagePort;
import architecture.connectors.SQLConnector;

import java.util.Map;

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
        Map<String, String> allMsgs = dbConnector.findAllRecords("MESSAGES"); // Supposez que vous exposez selectAll dans le connecteur
        StringBuilder sb = new StringBuilder();
        for (String val : allMsgs.values()) {
            // Format stocké : "DESTINATAIRE:CONTENU"
            if (val.startsWith(user + ":")) {
                sb.append("- ").append(val.split(":")[1]).append("\n");
            }
        }
        return !sb.isEmpty() ? sb.toString() : "Aucun message.";
    }
}