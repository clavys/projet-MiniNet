package architecture.interfaces;

import java.util.List;

public interface IRecommendationPort {
    void addFriend(String user1, String user2);
    void removeFriend(String user1, String user2);

    // Retourne la liste des amis (lecture rapide mémoire)
    List<String> getFriends(String user);

    // Algorithme : suggère des amis (amis d'amis)
    List<String> getRecommendations(String user);
}