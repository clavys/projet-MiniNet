import io.javalin.Javalin;
import architecture.components.*;
import architecture.connectors.SQLConnector;

import java.util.List;

public class ServerMain {
    public static void main(String[] args) {
        System.out.println("--- DÉMARRAGE DU SERVEUR MININET ---");

        // ==========================================
        // 1. INSTANCIATION DES COMPOSANTS (BACKEND)
        // ==========================================

        //INSTANCIATION DES 3 BASES DE DONNÉES (SHARDS)
        // Chaque composant est indépendant et possède son propre fichier
        Storage storageUsers = new Storage("users.db");
        Storage storagePosts = new Storage("posts.db");
        Storage storageMsgs  = new Storage("messages.db");

        // Les Managers (Logique métier)
        UserManager userMgr = new UserManager();
        PostManager postMgr = new PostManager();
        MessageService msgSvc = new MessageService();
        RecommendationService recoSvc = new RecommendationService();

        // Le Connecteur Intelligent
        SQLConnector sqlRouter = new SQLConnector();

        // 2. CÂBLAGE ARCHITECTURAL (BINDING)

        // Configuration du connecteur : on lui donne les 3 destinations
        sqlRouter.configureShards(storageUsers, storagePosts, storageMsgs);

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

        // Route : POST /messages?user=toto
        app.post("/message", ctx -> {
            String from = ctx.formParam("from");
            String to = ctx.formParam("to");
            String content = ctx.formParam("content");
            msgSvc.sendMessage(from, to, content);
            ctx.result("Message envoyé");
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
    }

}