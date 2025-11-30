package architecture.components;

import framework.Component;
import architecture.interfaces.IPostPort;
import architecture.connectors.SQLConnector;

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
        // Génération d'un ID unique pour le post
        String postId = "POST_" + System.currentTimeMillis();

        // Format de stockage simulé : "AUTEUR:CONTENU"
        String data = author + ":" + content;

        printLog("Création d'un post par " + author);

        // Appel architectural vers le Storage
        dbConnector.saveRecord("POSTS", postId, data);
    }

    @Override
    public String getWall(String user) {
        // Simulation : Dans un vrai système, on ferait une jointure SQL complexe
        // (Amis + Posts triés par date).
        // Ici, on retourne une réponse simulée pour valider le flux architectural.

        printLog("Génération du mur pour " + user);
        return "--- Mur de " + user + " ---\n" +
                "1. [Admin] Bienvenue sur MiniNet !\n" +
                "2. [Système] Ceci est une simulation de posts.";
    }
}