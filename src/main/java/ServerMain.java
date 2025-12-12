import io.javalin.Javalin;
import architecture.components.*;
import architecture.connectors.SQLConnector;
import io.javalin.websocket.WsContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import java.util.List;

public class ServerMain {

    // Map<Username, ContextWebSocket>
    static Map<String, WsContext> userSessions = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("--- DÉMARRAGE DU SERVEUR MININET ---");

        // ==========================================
        // 1. INSTANCIATION DES COMPOSANTS (BACKEND)
        // ==========================================

        // INSTANCIATION DES 3 BASES DE DONNÉES (SHARDS) AVEC TABLES SPÉCIFIQUES

        // 1. Base Utilisateurs : contient les users ET les liens d'amitié
        Storage storageUsers = new Storage("users.db", "USERS", "FRIENDS");

        // 2. Base Posts : ne contient que les posts
        Storage storagePosts = new Storage("posts.db", "POSTS");

        // 3. Base Messages : ne contient que les messages privés
        Storage storageMsgs  = new Storage("messages.db", "MESSAGES");

        // Les Managers (Logique métier)
        UserManager userMgr = new UserManager();
        PostManager postMgr = new PostManager();
        MessageService msgSvc = new MessageService();
        RecommendationService recoSvc = new RecommendationService();

        // Le Connecteur Intelligent
        SQLConnector sqlRouter = new SQLConnector();

        // 2. CÂBLAGE ARCHITECTURAL (BINDING) dynamique

        // Tout ce qui concerne USERS et FRIENDS va dans storageUsers
        sqlRouter.registerRoute("USERS", storageUsers);
        sqlRouter.registerRoute("FRIENDS", storageUsers);

        // Tout ce qui concerne POSTS va dans storagePosts
        sqlRouter.registerRoute("POSTS", storagePosts);

        // Tout ce qui concerne MESSAGES va dans storageMsgs
        sqlRouter.registerRoute("MESSAGES", storageMsgs);

        // On branche les Managers sur ce connecteur unique
        userMgr.setDbConnector(sqlRouter);
        postMgr.setDbConnector(sqlRouter);
        msgSvc.setDbConnector(sqlRouter);
        recoSvc.setDbConnector(sqlRouter);

        recoSvc.initGraph(); // Charge les données au démarrage

        // ==========================================
        // 3. DÉMARRAGE DU SERVEUR JAVALIN
        // ==========================================
        // On démarre le serveur sur le port 7000
        Javalin app = Javalin.create().start(7000);

        System.out.println(">> Serveur prêt sur http://localhost:7000");

        // ==========================================
        // 4. DÉFINITION DES ROUTES (L'API)
        // ==========================================
        // C'est ici que tu définis comment ton binôme accède aux fonctions.

        // --- AUTHENTIFICATION ---
        // Route : GET /login?user=toto&pass=123
        app.get("/login", ctx -> {
            String u = ctx.queryParam("user");
            String p = ctx.queryParam("pass");

            System.out.println("API >> Tentative de login pour : " + u);

            boolean success = userMgr.login(u, p);
            // On répond "true" ou "false" en texte brut
            ctx.result(String.valueOf(success));
        });

        // Route : POST /register (Paramètres envoyés dans le corps de requête)
        app.post("/register", ctx -> {
            String u = ctx.formParam("user");
            String p = ctx.formParam("pass");

            userMgr.register(u, p);
            ctx.result("OK");
        });

        // --- POSTS (MUR) ---
        // Route : POST /post (Créer un message)
        app.post("/post", ctx -> {
            String author = ctx.formParam("author");
            String content = ctx.formParam("content");

            postMgr.createPost(author, content);
            ctx.result("Post créé");
        });

        // Route : GET /wall?user=toto (Lire le mur)
        app.get("/wall", ctx -> {
            String user = ctx.queryParam("user");

            // On récupère la String formatée par ton PostManager
            String mur = postMgr.getWall(user);
            ctx.result(mur);
        });

        // --- MESSAGES PRIVÉS ---

        // Route : GET /messages?user=toto
        app.get("/messages", ctx -> {
            String user = ctx.queryParam("user");
            String inbox = msgSvc.checkMessages(user);
            ctx.result(inbox);
        });

        // Route : POST /message?user=toto
        app.post("/message", ctx -> {
            String from = ctx.formParam("from");
            String to = ctx.formParam("to");
            String content = ctx.formParam("content");

            // 1. Persistance (Votre code existant qui appelle le MessageService)
            msgSvc.sendMessage(from, to, content);

            // 2. Notification Temps Réel (Le "Bus d'événements")
            // On regarde si le destinataire est connecté en WebSocket
            WsContext recipientCtx = userSessions.get(to);

            if (recipientCtx != null && recipientCtx.session.isOpen()) {
                // On lui pousse une notification JSON ou Texte
                recipientCtx.send("NOTIFICATION:Nouveau message de " + from);
                System.out.println(">> [WS] Notification envoyée à " + to);
            }

            ctx.result("Message enregistré et notifié si possible.");
        });

        // Ajouter un ami
        app.post("/friend", ctx -> {
            String u = ctx.formParam("user");
            String f = ctx.formParam("friend");
            recoSvc.addFriend(u, f);
            ctx.result("Ami ajouté");
        });

        // Supprimer un ami
        app.post("/removeFriend", ctx -> {
            String u = ctx.formParam("user");
            String f = ctx.formParam("friend");
            recoSvc.removeFriend(u, f);
            ctx.result("Ami supprimé");
        });

        // Voir ses amis (Retourne une liste brute séparée par des virgules pour simplifier)
        app.get("/friends", ctx -> {
            String u = ctx.queryParam("user");
            List<String> friends = recoSvc.getFriends(u);
            ctx.result(String.join(",", friends));
        });

        // Obtenir des recommandations
        app.get("/recommendations", ctx -> {
            String u = ctx.queryParam("user");
            List<String> recos = recoSvc.getRecommendations(u);
            if (recos.isEmpty()) ctx.result("Aucune recommandation.");
            else ctx.result(String.join(",", recos));
        });

        // ==========================================
        // 5. CONFIGURATION DU BUS D'ÉVÉNEMENTS (WebSocket)
        // ==========================================

        // Le client se connectera sur ws://localhost:7000/events?user=Toto
        app.ws("/events", ws -> {

            ws.onConnect(ctx -> {
                String user = ctx.queryParam("user");
                if (user != null) {
                    // --- AJOUT IMPORTANT : Timeout de 30 minutes ---
                    ctx.session.setIdleTimeout(java.time.Duration.ofMinutes(30));
                    // -----------------------------------------------

                    userSessions.put(user, ctx);
                    System.out.println(">> [WS] Connexion active pour : " + user);
                }
            });

            // Quand un client se déconnecte (ferme l'appli)
            ws.onClose(ctx -> {
                String user = ctx.queryParam("user");
                if (user != null) {
                    userSessions.remove(user);
                    System.out.println(">> [WS] Déconnexion de : " + user);
                }
            });

            // (Optionnel) Si le client envoie un message via WS
            ws.onMessage(ctx -> {
                // Pour l'instant on ignore, on utilise WS juste pour le Push vers le client
            });
        });

    }

}