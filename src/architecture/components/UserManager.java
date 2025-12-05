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

}