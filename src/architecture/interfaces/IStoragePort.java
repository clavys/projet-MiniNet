package architecture.interfaces;

// Ce port définit comment on parle à notre "Fausse Base de Données"
public interface IStoragePort {
    // Simule un: INSERT INTO table VALUES (key, value)
    void insert(String tableName, String key, String value);

    // Simule un: SELECT value FROM table WHERE id = key
    String select(String tableName, String key);

    // Simule un: DELETE FROM table WHERE id = key
    void delete(String tableName, String key);

    // Pour vérifier si ça existe
    boolean exists(String tableName, String key);
}
