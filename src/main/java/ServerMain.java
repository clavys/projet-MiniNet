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
        System.out.println("--- DÉMARRAGE DU SERVEUR MININET (WebSocket Enhanced) ---");

        Storage storageUsers = new Storage("users.db", "USERS", "FRIENDS");
        Storage storagePosts = new Storage("posts.db", "POSTS");
        Storage storageMsgs  = new Storage("messages.db", "MESSAGES");

        UserManager userMgr = new UserManager();
        PostManager postMgr = new PostManager();
        MessageService msgSvc = new MessageService();
        RecommendationService recoSvc = new RecommendationService();

        // Binding
        SQLConnector sqlRouter = new SQLConnector();
        sqlRouter.registerRoute("USERS", storageUsers);
        sqlRouter.registerRoute("FRIENDS", storageUsers);
        sqlRouter.registerRoute("POSTS", storagePosts);
        sqlRouter.registerRoute("MESSAGES", storageMsgs);

        userMgr.setDbConnector(sqlRouter);
        postMgr.setDbConnector(sqlRouter);
        msgSvc.setDbConnector(sqlRouter);
        recoSvc.setDbConnector(sqlRouter);

        recoSvc.initGraph();

        Javalin app = Javalin.create().start(7000);
        System.out.println(">> Serveur prêt sur http://localhost:7000");

        // --- ROUTES API ---

        app.get("/login", ctx -> {
            boolean success = userMgr.login(ctx.queryParam("user"), ctx.queryParam("pass"));
            ctx.result(String.valueOf(success));
        });

        app.post("/register", ctx -> {
            userMgr.register(ctx.formParam("user"), ctx.formParam("pass"));
            ctx.result("OK");
        });

        // POST (Broadcast wall) ---
        app.post("/post", ctx -> {
            String author = ctx.formParam("author");
            String content = ctx.formParam("content");
            postMgr.createPost(author, content);

            // BROADCAST a tous les users online
            // Format du message : "POST:Auteur"
            userSessions.values().forEach(session -> {
                if (session.session.isOpen()) {
                    session.send("POST:" + author);
                }
            });

            ctx.result("Post created");
        });

        app.get("/wall", ctx -> ctx.result(postMgr.getWall(ctx.queryParam("user"))));

        app.get("/messages", ctx -> ctx.result(msgSvc.checkMessages(ctx.queryParam("user"))));

        // --- MESSAGE ---
        app.post("/message", ctx -> {
            String from = ctx.formParam("from");
            String to = ctx.formParam("to");
            String content = ctx.formParam("content");

            msgSvc.sendMessage(from, to, content);

            WsContext recipientCtx = userSessions.get(to);
            if (recipientCtx != null && recipientCtx.session.isOpen()) {
                // ex "MSG:Alice"
                recipientCtx.send("MSG:" + from);
            }
            ctx.result("Message sent");
        });

        app.post("/friend", ctx -> {
            recoSvc.addFriend(ctx.formParam("user"), ctx.formParam("friend"));
            ctx.result("Ami ajouté");
        });

        app.post("/removeFriend", ctx -> {
            recoSvc.removeFriend(ctx.formParam("user"), ctx.formParam("friend"));
            ctx.result("Ami supprimé");
        });

        app.get("/friends", ctx -> {
            List<String> friends = recoSvc.getFriends(ctx.queryParam("user"));
            ctx.result(String.join(",", friends));
        });

        app.get("/recommendations", ctx -> {
            List<String> recos = recoSvc.getRecommendations(ctx.queryParam("user"));
            ctx.result(recos.isEmpty() ? "Aucune" : String.join(",", recos));
        });

        // ==========================================
        // CONFIGURATION DU BUS D'ÉVÉNEMENTS (WebSocket)
        // ==========================================
        // Le client se connectera sur ws://localhost:7000/events?user=Toto
        app.ws("/events", ws -> {
            ws.onConnect(ctx -> {
                String user = ctx.queryParam("user");
                if (user != null) {
                    ctx.session.setIdleTimeout(java.time.Duration.ofMinutes(30));
                    userSessions.put(user, ctx);
                    System.out.println(">> [WS] Connecté : " + user);
                }
            });
            ws.onClose(ctx -> {
                String user = ctx.queryParam("user");
                if (user != null) userSessions.remove(user);
            });
        });
    }
}