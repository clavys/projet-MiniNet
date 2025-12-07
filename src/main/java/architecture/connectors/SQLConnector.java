package architecture.connectors;

import framework.Connector;
import architecture.interfaces.IStoragePort;

import java.util.Collections;
import java.util.Map;

public class SQLConnector extends Connector {

    private IStoragePort storagePort;

    public SQLConnector() {
        super("SQL/JDBC Connector");
    }

    @Override
    public void setComponent(Object component) {
        if (component instanceof IStoragePort) {
            this.storagePort = (IStoragePort) component;
        }
    }

    // --- Méthodes exposées aux Managers (l'API du connecteur) ---

    public void saveRecord(String table, String key, String data) {
        // La "Glue" ici est simple, mais elle pourrait crypter les données par exemple
        storagePort.insert(table, key, data);
    }

    public String findRecord(String table, String key) {
        return storagePort.select(table, key);
    }

    public Map<String, String> findAllRecords(String table) {
        if (storagePort != null) {
            return storagePort.findAll(table);
        }
        return Collections.emptyMap();
    }
}