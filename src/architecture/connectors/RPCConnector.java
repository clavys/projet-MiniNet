package architecture.connectors;

import framework.Connector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class RPCConnector extends Connector {

    // L'adresse de VOTRE serveur (Backend)
    private final String SERVER_URL = "http://localhost:7000";

    // Le client qui sait parler HTTP
    private final HttpClient client;

    public RPCConnector() {
        super("HTTP/REST Glue"); // On change le nom de la Glue
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5)) // Timeout si le serveur est éteint
                .build();
    }

    @Override
    public void setComponent(Object component) {
        // Côté Client HTTP, on ne se connecte plus à un objet Java "Component".
        // On se connecte à une URL. Cette méthode devient vide ou sert à configurer l'URL.
    }

    // =========================================================================
    //  ADAPTATION : On transforme les méthodes Java en appels HTTP (GET/POST)
    // =========================================================================

    // --- LOGIN (Route: GET /login?user=...&pass=...) ---
    public boolean callLogin(String username, String password) {
        try {
            // 1. On fabrique l'URL
            String url = SERVER_URL + "/login?user=" + encode(username) + "&pass=" + encode(password);

            // 2. On crée la requête
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            // 3. On envoie et on attend la réponse
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // 4. On analyse la réponse (Le serveur renvoie "true" ou "false")
            return Boolean.parseBoolean(response.body().trim());

        } catch (Exception e) {
            System.err.println("Erreur HTTP Login : " + e.getMessage());
            return false;
        }
    }

    // --- CREATE POST (Route: POST /post avec body form-data) ---
    public void callCreatePost(String author, String content) {
        try {
            // Pour un POST, c'est un peu plus verbeux, on simule un formulaire Web
            String formData = "author=" + encode(author) + "&content=" + encode(content);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/post"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formData))
                    .build();

            client.send(request, HttpResponse.BodyHandlers.ofString());
            // On ne retourne rien, comme la méthode void originale

        } catch (Exception e) {
            System.err.println("Erreur HTTP Post : " + e.getMessage());
        }
    }

    // --- GET WALL (Route: GET /wall?user=...) ---
    public String callGetWall(String user) {
        try {
            String url = SERVER_URL + "/wall?user=" + encode(user);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();

        } catch (Exception e) {
            return "Erreur de connexion au serveur.";
        }
    }

    // --- Utilitaire pour gérer les espaces et caractères spéciaux dans les URL ---
    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    // --- REGISTER (Route: POST /register) ---
    public void callRegister(String username, String password) {
        try {
            // On prépare les données (user=...&pass=...)
            String formData = "user=" + encode(username) + "&pass=" + encode(password);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/register"))
                    .header("Content-Type", "application/x-www-form-urlencoded") // Important pour Javalin .formParam()
                    .POST(HttpRequest.BodyPublishers.ofString(formData))
                    .build();

            // On envoie
            client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("   [HTTP] Inscription envoyée pour " + username);

        } catch (Exception e) {
            System.err.println("Erreur HTTP Register : " + e.getMessage());
        }
    }

    // --- MESSAGERIE PRIVÉE ---

    // Envoi : POST /message (car on crée une donnée)
    // Note : Il faut que ton binôme ajoute app.post("/message", ...) côté Serveur !
    public void callSendMessage(String from, String to, String content) {
        try {
            String formData = "from=" + encode(from) + "&to=" + encode(to) + "&content=" + encode(content);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/message")) // Assure-toi que cette route existe côté Serveur
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formData))
                    .build();

            client.send(request, HttpResponse.BodyHandlers.ofString());

        } catch (Exception e) {
            System.err.println("Erreur HTTP SendMessage : " + e.getMessage());
        }
    }

    // Lecture : GET /messages?user=...
    public String callCheckMessages(String user) {
        try {
            String url = SERVER_URL + "/messages?user=" + encode(user);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();

        } catch (Exception e) {
            return "Erreur lecture messages.";
        }
    }

    // --- AMIS (Si tu l'as implémenté) ---

    public void callAddFriend(String currentUser, String friendName) {
        // Tu peux utiliser une route POST /friend
        try {
            String formData = "user=" + encode(currentUser) + "&friend=" + encode(friendName);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/friend"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formData))
                    .build();
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- SUPPRIMER UN AMI ---
    // Route : POST /removeFriend
    public void callRemoveFriend(String currentUser, String friendName) {
        try {
            // On prépare les données (user=...&friend=...)
            String formData = "user=" + encode(currentUser) + "&friend=" + encode(friendName);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/removeFriend")) // Route spécifique pour la suppression
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formData))
                    .build();

            // On envoie la requête
            client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("   [HTTP] Suppression d'ami demandée : " + friendName);

        } catch (Exception e) {
            System.err.println("Erreur HTTP RemoveFriend : " + e.getMessage());
        }
    }
}