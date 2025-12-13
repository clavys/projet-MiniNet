package architecture.components;

import framework.Component;
import architecture.interfaces.IUserPort;
import architecture.connectors.SQLConnector;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

public class UserManager extends Component implements IUserPort {

    private SQLConnector dbConnector;

    public UserManager() {
        super("UserManager");
    }

    public void setDbConnector(SQLConnector conn) {
        this.dbConnector = conn;
    }

    // --- NOUVELLE MÉTHODE PRIVÉE : HACHAGE SHA-256 ---
    private String hash(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));

            // Conversion des bytes en Hexadécimal (String lisible)
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            // En cas d'erreur (peu probable), on log et on retourne null
            System.err.println("Erreur de hachage : " + e.getMessage());
            return null;
        }
    }

    @Override
    public boolean register(String username, String password) {
        //  VÉRIFICATION : L'utilisateur existe-t-il déjà ?
        if (dbConnector.findRecord("USERS", username) != null) {
            printLog("Échec de l'enregistrement : l'utilisateur " + username + " existe déjà.");
            return false; // L'enregistrement a échoué (utilisateur déjà existant)
        }

        // 2. Si l'utilisateur n'existe pas, on procède à l'enregistrement.
        String hashedPassword = hash(password);

        String userData = "PASS=" + hashedPassword + ";ROLE=USER";

        printLog("Enregistrement sécurisé de " + username);
        dbConnector.saveRecord("USERS", username, userData);
        return true;
    }

    @Override
    public boolean login(String username, String password) {
        String data = dbConnector.findRecord("USERS", username);

        if (data == null) {
            printLog("Utilisateur inconnu.");
            return false;
        }

        // SÉCURITÉ : On hache le mot de passe reçu pour le comparer à celui stocké
        String hashedInput = hash(password);

        // On cherche "PASS=le_hash"
        if (hashedInput != null && data.contains("PASS=" + hashedInput)) {
            printLog("Login OK pour " + username);
            return true;
        } else {
            printLog("Mot de passe incorrect.");
            return false;
        }
    }
}