package architecture.components;

import framework.Component;
import architecture.interfaces.IPostPort;
import architecture.connectors.SQLConnector;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class PostManager extends Component implements IPostPort {

    private SQLConnector dbConnector;

    public PostManager() {
        super("PostManager");
    }

    public void setDbConnector(SQLConnector conn) {
        this.dbConnector = conn;
    }

    @Override
    public void createPost(String author, String content) {
        // On utilise le timestamp pour trier chronologiquement plus tard
        long timestamp = System.currentTimeMillis();
        String postId = "POST_" + timestamp + "_" + author;

        // Format de stockage : "AUTEUR:CONTENU"
        String data = author + ":" + content;

        printLog("Création d'un post par " + author);
        dbConnector.saveRecord("POSTS", postId, data);
    }

    @Override
    public String getWall(String user) {
        printLog("Génération du mur pour " + user);

        // 1. Récupération de TOUS les posts (Architecturalement correct via le connecteur)
        Map<String, String> allPosts = dbConnector.findAllRecords("POSTS");

        if (allPosts.isEmpty()) {
            return ">> Le mur est vide. Soyez le premier à poster !";
        }

        // 2. Tri des posts (du plus récent au plus vieux)
        // Astuce : On trie les clés (qui contiennent le timestamp) en ordre inverse
        List<String> sortedKeys = new ArrayList<>(allPosts.keySet());
        sortedKeys.sort(Collections.reverseOrder());

        StringBuilder sb = new StringBuilder();
        sb.append("--- FIL D'ACTUALITÉ (Derniers messages) ---\n");

        for (String key : sortedKeys) {
            String val = allPosts.get(key);
            // Format "AUTEUR:CONTENU"
            if (val.contains(":")) {
                String[] parts = val.split(":", 2);
                String author = parts[0];
                String content = parts[1];



                sb.append(" [").append(author).append("] : ").append(content).append("\n");
            }
        }
        return sb.toString();
    }
}