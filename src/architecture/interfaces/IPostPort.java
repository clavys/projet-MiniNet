package architecture.interfaces;

public interface IPostPort {
    // Crée une publication
    void createPost(String author, String content);

    // Récupère le "Mur" (Fil d'actualité)
    String getWall(String user);
}