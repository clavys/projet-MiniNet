package architecture.connectors;

import framework.Connector;
import architecture.interfaces.IStoragePort;

import java.util.Collections;
import java.util.Map;

public class SQLConnector extends Connector {

    // On maintient 3 références vers 3 ports différents
    private IStoragePort userStorage;
    private IStoragePort postStorage;
    private IStoragePort msgStorage;

    public SQLConnector() {
        super("SQL Sharding Router");
    }

    @Override
    public void setComponent(Object component) {
        // Cette méthode générique devient moins utile ici car on a besoin de préciser QUI est QUI.
        // On va préférer une méthode de configuration explicite ci-dessous.
    }

    // Nouvelle méthode de "câblage" spécifique pour le sharding
    public void configureShards(IStoragePort users, IStoragePort posts, IStoragePort msgs) {
        this.userStorage = users;
        this.postStorage = posts;
        this.msgStorage = msgs;
    }

    // --- LOGIQUE DE ROUTAGE (Le "Sharding") ---

    // Méthode helper pour choisir la bonne DB selon la table
    private IStoragePort route(String table) {
        switch (table) {
            case "USERS":   return userStorage;
            case "FRIENDS": return userStorage; // Les amis vont avec les users
            case "POSTS":   return postStorage;
            case "MESSAGES": return msgStorage;
            default:
                System.err.println("Table inconnue : " + table);
                return userStorage; // Fallback
        }
    }

    // --- API DU CONNECTEUR (Mise à jour) ---

    public void saveRecord(String table, String key, String data) {
        // Le connecteur choisit intelligemment la bonne base
        IStoragePort target = route(table);
        target.insert(table, key, data);
    }

    public String findRecord(String table, String key) {
        return route(table).select(table, key);
    }

    public Map<String, String> findAllRecords(String table) {
        IStoragePort target = route(table);
        if (target != null) {
            return target.findAll(table);
        }
        return Collections.emptyMap();
    }
}