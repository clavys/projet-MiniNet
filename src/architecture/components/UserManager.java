package architecture.components;

import framework.Component;
import architecture.interfaces.IUserPort;
import architecture.connectors.SQLConnector;

public class UserManager extends Component implements IUserPort {

    private SQLConnector dbConnector;

    public UserManager() {
        super("UserManager");
    }

    // Injection du connecteur (Binding)
    public void setDbConnector(SQLConnector conn) {
        this.dbConnector = conn;
    }

    @Override
    public void register(String username, String password) {
        // Logique métier : on prépare la donnée
        String userData = "PASS=" + password + ";ROLE=USER";

        // Appel architectural : On délègue le stockage
        printLog("Enregistrement de " + username + " vers le Storage...");
        dbConnector.saveRecord("USERS", username, userData);
    }

    @Override
    public boolean login(String username, String password) {
        // Appel architectural : On récupère la donnée brute
        String data = dbConnector.findRecord("USERS", username);

        if (data == null) {
            printLog("Utilisateur inconnu.");
            return false;
        }

        // Vérification simple (parsing du "faux" format DB)
        if (data.contains("PASS=" + password)) {
            printLog("Login OK pour " + username);
            return true;
        } else {
            printLog("Mot de passe incorrect.");
            return false;
        }
    }

    @Override
    public void addFriend(String currentUser, String newFriend) {
        // Clé unique pour la relation
        String relationKey = currentUser + "_" + newFriend;

        printLog("Ajout de l'ami : " + newFriend + " pour " + currentUser);

        // On sauvegarde dans la table FRIENDS
        // On pourrait vérifier si newFriend existe dans USERS avant,
        // mais pour le prototype on suppose que oui.
        dbConnector.saveRecord("FRIENDS", relationKey, "STATUS=ACCEPTED");
    }

    @Override
    public void removeFriend(String currentUser, String oldFriend) {
        String relationKey = currentUser + "_" + oldFriend;
        printLog("Suppression de l'ami : " + oldFriend);

        // Note: Notre interface IStoragePort n'a pas de "delete" dans l'exemple précédent.
        // Si vous l'avez ajoutée c'est bien, sinon on peut juste ignorer ou mettre "STATUS=DELETED"
        dbConnector.saveRecord("FRIENDS", relationKey, "STATUS=DELETED");
    }
}