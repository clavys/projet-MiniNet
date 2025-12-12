package architecture.connectors;

import framework.Connector;
import architecture.interfaces.IStoragePort;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

public class SQLConnector extends Connector {

    // Au lieu d'avoir 3 variables fixes, on a une table de routage dynamique
    // Clé = Nom de la Table (ex: "USERS"), Valeur = Le port de stockage associé
    private Map<String, IStoragePort> routingTable = new HashMap<>();

    public SQLConnector() {
        super("SQL Dynamic Router");
    }

    @Override
    public void setComponent(Object component) { }

    // --- NOUVELLE MÉTHODE DE CONFIGURATION ---
    // On remplace configureShards(...) par une méthode générique
    public void registerRoute(String tableName, IStoragePort storageNode) {
        this.routingTable.put(tableName, storageNode);
        // On pourrait ajouter un log ici : "Route ajoutée : USERS -> storageUsers"
    }

    // --- LOGIQUE DE ROUTAGE DYNAMIQUE ---
    private IStoragePort route(String table) {
        // On cherche dans la Map
        IStoragePort target = routingTable.get(table);

        if (target == null) {
            System.err.println("ERREUR CRITIQUE : Aucune route définie pour la table " + table);
            // Fallback optionnel : retourner le premier élément ou null
            return null;
        }
        return target;
    }

    // --- API DU CONNECTEUR (Inchangée en signature, mais dynamique en interne) ---

    public void saveRecord(String table, String key, String data) {
        IStoragePort target = route(table);
        if (target != null) target.insert(table, key, data);
    }

    public String findRecord(String table, String key) {
        IStoragePort target = route(table);
        return (target != null) ? target.select(table, key) : null;
    }

    public Map<String, String> findAllRecords(String table) {
        IStoragePort target = route(table);
        return (target != null) ? target.findAll(table) : Collections.emptyMap();
    }
}