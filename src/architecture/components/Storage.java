package architecture.components;

import framework.Component;
import architecture.interfaces.IStoragePort;
import java.util.HashMap;
import java.util.Map;

public class Storage extends Component implements IStoragePort {

    // Structure : BaseDeDonnées -> Table -> (CléPrimaire -> DonnéeJSON)
    private Map<String, Map<String, String>> database;

    public Storage() {
        super("Database System");
        this.database = new HashMap<>();

        // On initialise les "tables"
        this.database.put("USERS", new HashMap<>());
        this.database.put("POSTS", new HashMap<>());
        this.database.put("MESSAGES", new HashMap<>());
        this.database.put("FRIENDS", new HashMap<>());
        printLog("Service de stockage démarré (In-Memory).");
    }

    @Override
    public void insert(String tableName, String key, String value) {
        if (database.containsKey(tableName)) {
            database.get(tableName).put(key, value);
            printLog("INSERT INTO " + tableName + " VALUES (" + key + ", " + value + ")");
        } else {
            System.err.println("Erreur : La table " + tableName + " n'existe pas.");
        }
    }

    @Override
    public String select(String tableName, String key) {
        if (database.containsKey(tableName)) {
            printLog("SELECT * FROM " + tableName + " WHERE ID=" + key);
            return database.get(tableName).get(key); // Retourne null si pas trouvé
        }
        return null;
    }

    @Override
    public boolean exists(String tableName, String key) {
        return database.containsKey(tableName) && database.get(tableName).containsKey(key);
    }

    @Override
    public void delete(String tableName, String key) {
        if(database.containsKey(tableName)) {
            database.get(tableName).remove(key);
        }
    }
}