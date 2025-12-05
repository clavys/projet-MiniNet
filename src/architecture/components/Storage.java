package architecture.components;

import framework.Component;
import architecture.interfaces.IStoragePort;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;

public class Storage extends Component implements IStoragePort {

    private Connection connection;
    private String dbUrl;
    // Liste des tables autorisées pour cette instance
    private List<String> managedTables;

    // MODIFICATION : Le constructeur prend maintenant les tables en arguments variables
    public Storage(String dbName, String... tables) {
        super("Database System (" + dbName + ")");
        this.dbUrl = "jdbc:sqlite:" + dbName;
        // On stocke la liste des tables à créer
        this.managedTables = Arrays.asList(tables);
        initDB();
    }

    private void initDB() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(this.dbUrl);
            printLog("Connecté au fichier : " + this.dbUrl);

            // MODIFICATION : On ne crée QUE les tables demandées
            for (String table : managedTables) {
                createTable(table);
                printLog("Table vérifiée/créée : " + table);
            }

        } catch (Exception e) {
            System.err.println("ERREUR STORAGE : " + e.getMessage());
        }
    }

    private void createTable(String tableName) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "id TEXT PRIMARY KEY, " +
                "data TEXT NOT NULL)";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    // ... Le reste de la classe (insert, select, delete...) ne change pas ...
    @Override
    public void insert(String tableName, String key, String value) {
        // Optionnel : On pourrait vérifier si tableName est dans managedTables ici pour plus de sécurité
        String sql = "INSERT OR REPLACE INTO " + tableName + " (id, data) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, key);
            pstmt.setString(2, value);
            pstmt.executeUpdate();
            printLog("SQL >> INSERT INTO " + tableName + " [" + key + "]");
        } catch (SQLException e) { System.err.println("Erreur Insert: " + e.getMessage()); }
    }

    @Override
    public String select(String tableName, String key) {
        String sql = "SELECT data FROM " + tableName + " WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, key);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("data");
        } catch (SQLException e) { System.err.println("Erreur Select: " + e.getMessage()); }
        return null;
    }

    @Override
    public void delete(String tableName, String key) {
        String sql = "DELETE FROM " + tableName + " WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, key);
            pstmt.executeUpdate();
            printLog("SQL >> DELETE FROM " + tableName + " [" + key + "]");
        } catch (SQLException e) { System.err.println("Erreur Delete: " + e.getMessage()); }
    }

    @Override
    public boolean exists(String tableName, String key) {
        return select(tableName, key) != null;
    }

    @Override
    public Map<String, String> selectAll(String tableName) {
        return findAll(tableName);
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
        } catch (SQLException e) { System.err.println("Erreur FindAll: " + e.getMessage()); }
        return results;
    }
}