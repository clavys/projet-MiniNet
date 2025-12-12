package architecture.components;

import framework.Component;
import architecture.interfaces.IRecommendationPort;
import architecture.connectors.SQLConnector;

import java.util.*;

public class RecommendationService extends Component implements IRecommendationPort {

    private SQLConnector dbConnector;

    // --- STRUCTURE EN MÉMOIRE (Le Graphe) ---
    // Map<Utilisateur, Set<SesAmis>>
    private final Map<String, Set<String>> socialGraph = new HashMap<>();

    public RecommendationService() {
        super("RecommendationService");
    }

    public void setDbConnector(SQLConnector conn) {
        this.dbConnector = conn;
    }

    // --- INITIALISATION (Chargement au démarrage) ---
    public void initGraph() {
        printLog("Chargement du graphe social depuis la DB...");
        // On récupère tout le contenu de la table FRIENDS
        Map<String, String> allRelations = dbConnector.findAllRecords("FRIENDS");

        for (Map.Entry<String, String> entry : allRelations.entrySet()) {
            String key = entry.getKey();   // Ex: "Toto_Titi"
            String val = entry.getValue(); // Ex: "STATUS=ACCEPTED"

            if (val.contains("ACCEPTED")) {
                String[] parts = key.split("_");
                if (parts.length == 2) {
                    addToGraphMemory(parts[0], parts[1]);
                }
            }
        }
        printLog("Graphe initialisé avec " + socialGraph.size() + " noeuds.");
    }

    // Helper interne pour mettre à jour la mémoire sans toucher à la DB
    private void addToGraphMemory(String u1, String u2) {
        socialGraph.computeIfAbsent(u1, k -> new HashSet<>()).add(u2);
        socialGraph.computeIfAbsent(u2, k -> new HashSet<>()).add(u1); // Relation bidirectionnelle
    }

    private void removeFromGraphMemory(String u1, String u2) {
        if (socialGraph.containsKey(u1)) socialGraph.get(u1).remove(u2);
        if (socialGraph.containsKey(u2)) socialGraph.get(u2).remove(u1);
    }

    // --- SERVICES DU COMPOSANT (Write-Through) ---

    @Override
    public void addFriend(String user1, String user2) {
        if (user1.equals(user2)) return;

        printLog("Ajout ami : " + user1 + " <-> " + user2);

        // 1. Mise à jour immédiate du graphe (Performance)
        addToGraphMemory(user1, user2);

        // 2. Persistance synchrone (Durabilité)
        // On stocke la relation. Clé = "User1_User2"
        String key = user1 + "_" + user2;
        dbConnector.saveRecord("FRIENDS", key, "STATUS=ACCEPTED");
    }

    @Override
    public void removeFriend(String user1, String user2) {
        printLog("Suppression ami : " + user1 + " <-> " + user2);

        // 1. Mémoire
        removeFromGraphMemory(user1, user2);

        // 2. DB (On pourrait supprimer, ici on note DELETED pour l'historique)
        // Note : Idéalement il faudrait dbConnector.delete(...), mais saveRecord suffit pour écraser
        String key = user1 + "_" + user2;
        dbConnector.saveRecord("FRIENDS", key, "STATUS=DELETED");
    }

    @Override
    public List<String> getFriends(String user) {
        // Lecture purement mémoire (Très rapide)
        Set<String> friends = socialGraph.getOrDefault(user, Collections.emptySet());
        return new ArrayList<>(friends);
    }

    @Override
    public List<String> getRecommendations(String user) {
        printLog("Calcul recommandations pour " + user);
        Set<String> myFriends = socialGraph.getOrDefault(user, Collections.emptySet());
        Set<String> recommendations = new HashSet<>();

        // Algorithme : Amis de mes amis
        for (String friend : myFriends) {
            Set<String> friendsOfFriend = socialGraph.getOrDefault(friend, Collections.emptySet());
            for (String fof : friendsOfFriend) {
                // On ne se recommande pas soi-même et pas quelqu'un qu'on connait déjà
                if (!fof.equals(user) && !myFriends.contains(fof)) {
                    recommendations.add(fof);
                }
            }
        }

        if (recommendations.isEmpty()) {
            // Fallback : recommander quelqu'un au hasard du graphe (optionnel)
        }

        return new ArrayList<>(recommendations);
    }
}