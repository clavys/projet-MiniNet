package architecture.components;

import framework.Component;
import architecture.interfaces.IStoragePort;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class Storage extends Component implements IStoragePort {

    // Connexion unique à la base de données fichier
    private Connection connection;
    private final String DB_URL = "jdbc:sqlite:mininet.db";

    public Storage() {
        super("Database System (SQLite)");
        initDB();
    }

    // --- INITIALISATION (Création des tables si elles n'existent pas) ---
    private void initDB() {
        try {
            // 1. Chargement du driver (nécessaire pour certains vieux environnements Java)
            Class.forName("org.sqlite.JDBC");

            // 2. Connexion (Crée le fichier mininet.db s'il n'existe pas)
            connection = DriverManager.getConnection(DB_URL);
            printLog("Connecté à la base de données SQLite : mininet.db");

            // 3. Création des tables.
            // Astuce : On utilise une structure générique (KEY, VALUE) pour coller à ton interface actuelle.
            createTable("USERS");
            createTable("POSTS");
            createTable("MESSAGES");
            createTable("FRIENDS");

        } catch (Exception e) {
            System.err.println("ERREUR CRITIQUE STORAGE : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTable(String tableName) throws SQLException {
        // "id" stockera la clé (ex: le pseudo), "data" stockera la valeur (ex: le mot de passe ou le contenu)
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "id TEXT PRIMARY KEY, " +
                "data TEXT NOT NULL)";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    // --- IMPLEMENTATION DE IStoragePort (CRUD SQL) ---

    @Override
    public void insert(String tableName, String key, String value) {
        // SQL : INSERT OR REPLACE permet de mettre à jour si la clé existe déjà
        String sql = "INSERT OR REPLACE INTO " + tableName + " (id, data) VALUES (?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, key);
            pstmt.setString(2, value);
            pstmt.executeUpdate();
            printLog("SQL >> INSERT INTO " + tableName + " [" + key + "]");
        } catch (SQLException e) {
            System.err.println("Erreur Insert: " + e.getMessage());
        }
    }

    @Override
    public String select(String tableName, String key) {
        String sql = "SELECT data FROM " + tableName + " WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, key);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                printLog("SQL >> SELECT " + tableName + " [" + key + "] -> Trouvé");
                return rs.getString("data");
            }
        } catch (SQLException e) {
            System.err.println("Erreur Select: " + e.getMessage());
        }
        return null; // Pas trouvé
    }

    @Override
    public void delete(String tableName, String key) {
        String sql = "DELETE FROM " + tableName + " WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, key);
            pstmt.executeUpdate();
            printLog("SQL >> DELETE FROM " + tableName + " [" + key + "]");
        } catch (SQLException e) {
            System.err.println("Erreur Delete: " + e.getMessage());
        }
    }

    @Override
    public boolean exists(String tableName, String key) {
        return select(tableName, key) != null;
    }

    @Override
    public Map<String, String> selectAll(String tableName) {
        return findAll(tableName); // Redirection pour éviter de dupliquer le code
    }

    @Override
    public Map<String, String> findAll(String tableName) {
        Map<String, String> results = new HashMap<>();
        String sql = "SELECT id, data FROM " + tableName;

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                results.put(rs.getString("id"), rs.getString("data"));
            }
            printLog("SQL >> FIND ALL " + tableName + " (" + results.size() + " résultats)");

        } catch (SQLException e) {
            System.err.println("Erreur FindAll: " + e.getMessage());
        }
        return results;
    }
}