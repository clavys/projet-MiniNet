package architecture.interfaces;

public interface IMessagePort {
    // Envoie un message d'un utilisateur à un autre
    void sendMessage(String from, String to, String content);

    // Récupère les messages reçus (retourne une String brute pour simplifier)
    String checkMessages(String user);
}