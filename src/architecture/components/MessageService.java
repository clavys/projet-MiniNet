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
        // ASTUCE : On met le nom de l'expéditeur DANS l'identifiant du message
        // Exemple d'ID généré : "Toto_1709887654321"
        String msgId = from + "_" + System.currentTimeMillis();

        // Le contenu contient le destinataire pour le filtrage
        // Exemple de Data : "Titi:Salut ça va ?"
        String data = to + ":" + content;

        printLog("Envoi message de " + from + " vers " + to);
        dbConnector.saveRecord("MESSAGES", msgId, data);
    }

    @Override
    public String checkMessages(String user) {
        printLog("Récupération des messages pour " + user);

        // 1. On récupère TOUT (via le connecteur, c'est propre architecturalement)
        Map<String, String> allMsgs = dbConnector.findAllRecords("MESSAGES");

        StringBuilder sb = new StringBuilder();
        boolean found = false;

        for (Map.Entry<String, String> entry : allMsgs.entrySet()) {
            String key = entry.getKey();   // Ex: "Toto_1709887654321"
            String val = entry.getValue(); // Ex: "Titi:Salut ça va ?"

            // 2. On vérifie si c'est pour nous (ça commence par "Moi:")
            if (val.startsWith(user + ":")) {

                // 3. On extrait le message pur (après le "Moi:")
                String content = val.split(":", 2)[1];

                // 4. ON RECUPERE L'EXPEDITEUR DEPUIS LA CLÉ !
                // On prend ce qu'il y a avant le "_"
                String senderInfo = key.split("_")[0];

                sb.append(" - De [").append(senderInfo).append("] : ").append(content).append("\n");
                found = true;
            }
        }

        if (!found) {
            return "Vous n'avez aucun nouveau message.";
        }
        return sb.toString();
    }
}