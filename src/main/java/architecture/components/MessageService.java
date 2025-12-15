package architecture.components;

import framework.Component;
import architecture.interfaces.IMessagePort;
import architecture.connectors.SQLConnector;
import java.util.*;

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
        // ID unique : "EXPEDITEUR_TIMESTAMP"
        String msgId = from + "_" + System.currentTimeMillis();
        // Contenu : "DESTINATAIRE:MESSAGE"
        String data = to + ":" + content;

        printLog("Envoi message de " + from + " vers " + to);
        dbConnector.saveRecord("MESSAGES", msgId, data);
    }

    @Override
    public String checkMessages(String user) {
        // 1. On récupère TOUS les messages de la base
        Map<String, String> allMsgs = dbConnector.findAllRecords("MESSAGES");

        // Liste pour stocker l'historique brut
        List<String> history = new ArrayList<>();

        for (Map.Entry<String, String> entry : allMsgs.entrySet()) {
            String key = entry.getKey();   // Ex: "Toto_1709887654321"
            String val = entry.getValue(); // Ex: "Titi:Salut ça va ?"

            try {
                // Analyse des clés/valeurs
                String[] keyParts = key.split("_");
                String sender = keyParts[0];
                long timestamp = Long.parseLong(keyParts[1]);

                String[] valParts = val.split(":", 2);
                String recipient = valParts[0];
                String content = valParts[1];

                // FILTRE : Est-ce que ce message me concerne ? (Envoyé par moi OU Reçu par moi)
                if (sender.equals(user) || recipient.equals(user)) {
                    // Format => TIMESTAMP::SENDER::RECIPIENT::CONTENT
                    history.add(timestamp + "::" + sender + "::" + recipient + "::" + content);
                }
            } catch (Exception e) {
                // Ignorer les messages mal formés
            }
        }

        // 2. TRI CHRONOLOGIQUE, comparaison des timestamps
        Collections.sort(history);

        // On renvoie tout sous forme d'un gros bloc de texte
        return String.join("\n", history);
    }
}