# Rapport d'Architecture : Projet MiniNet

**Auteurs :** 
**Promotion :** ALMA M2 2025/2026
**Sujet :** Conception et implémentation d'un système logiciel à base de composants et connecteurs.

---

## 1. Définition du Métamodèle Architectural (Niveau M2)

Cette section décrit les concepts fondamentaux utilisés pour modéliser notre architecture. Conformément aux bonnes pratiques d'ingénierie logicielle, notre métamodèle repose sur une séparation stricte entre le calcul (Composants) et la communication (Connecteurs).

### 1.1 Éléments Structurels

* **Configuration**
    * Représente le graphe global du système. Elle agit comme un conteneur qui instancie les composants et les connecteurs et décrit la topologie de leurs interconnexions via des liens d'attachement et de binding.

* **Composant**
    * Unité architecturale encapsulant une fonctionnalité du système.
    * *Spécificité du modèle :* Contrairement aux modèles classiques, le composant contient **directement** ses ports et services, sans classe d'interface intermédiaire.
    * Il possède des propriétés (`Map<String, String>`) et des contraintes techniques.

* **Connecteur**
    * Entité dédiée à la gestion des interactions entre les composants.
    * Il est caractérisé par sa **Glue** (`String`), qui définit le protocole de communication (synchrone, asynchrone, conversion de données, etc.).

### 1.2 Points d'Interaction

* **Port**
    * Point d'interaction visible d'un composant.
    * **RequiredPort :** Le composant exprime un besoin vers l'extérieur.
    * **ProvidedPort :** Le composant offre une fonctionnalité à l'extérieur.

* **Service**
    * Représente une fonctionnalité spécifique ou une opération métier (ex: `login()`).
    * Il est directement rattaché au composant pour préciser la sémantique de l'échange.

* **Rôle**
    * Point de connexion situé sur un Connecteur. Il identifie la partie prise par un composant dans l'interaction (ex: *caller*, *called*).

### 1.3 Liens d'Architecture

* **Attachment (Attachement)**
    * Lien "horizontal" connectant un **Port** (Composant) à un **Rôle** (Connecteur). Il lie l'unité de calcul à l'unité de communication.

* **Binding**
    * Lien "vertical" reliant un Port de la **Configuration** à un Port d'une instance de **Composant**. Il sert à exposer des fonctionnalités internes vers l'extérieur du système.

---

## 2. Description Architecturale de MiniNet (Niveau M1)

L'architecture de MiniNet est une instanciation du métamodèle M2 décrit ci-dessus. Elle vise à fournir un mini réseau social permettant l'inscription, la publication de messages et la gestion d'amis.

### 2.1 Composants du Système (Instances)

Les composants suivants ont été identifiés pour couvrir les exigences fonctionnelles :

#### **1. Client (Console/Web)**
* **Description :** Interface utilisateur minimale (Console ou Web).
* **Rôle :** Point d'entrée pour l'utilisateur final.
* **Ports Requis :** Authentification, Publication, Messagerie.

#### **2. UserManager**
* **Description :** Gère le cycle de vie des utilisateurs et le graphe social.
* **Services Fournis :**
    * `login()`, `register()`.
    * `addFriend()`, `removeFriend()`.
* **Ports :**
    * *Provided :* AuthInterface (reçoit les appels du Client).
    * *Required :* DatabaseAccess (vers le Storage).

#### **3. PostManager**
* **Description :** Gère la création et la récupération des publications publiques.
* **Services Fournis :**
    * `createPost()`.
    * `getWall()` (Consultation des publications des contacts).
* **Ports :**
    * *Provided :* PostInterface.
    * *Required :* DatabaseAccess.

#### **4. MessageService**
* **Description :** Gère les échanges privés entre utilisateurs.
* **Services Fournis :** `sendMessage()`, `readMessages()`.
* **Ports :**
    * *Provided :* MsgInterface.
    * *Required :* DatabaseAccess.

#### **5. Storage (Database)**
* **Description :** Composant technique assurant la persistance des données.
* **Services Fournis :** `save()`, `find()`.
* **Ports :**
    * *Provided :* SQLInterface.

### 2.2 Connecteurs et Interactions (Instances)

Les connecteurs définissent les modes d'interaction :

| Nom Connecteur | Type (Glue) | Rôles | Description |
| :--- | :--- | :--- | :--- |
| **RPC_Connector** | `Call-Return` (Synchrone) | `Caller`, `Called` | Utilisé pour la communication entre le **Client** et les Managers (**User, Post, Message**). Le client attend la réponse. |
| **SQL_Link** | `JDBC / Query` | `Requester`, `Responder` | Connecte les Managers au composant **Storage**. Transporte des requêtes de données. |

### 2.3 Matrice de Traçabilité (M2 vers M1 vers ACME)

Ce tableau illustre la correspondance entre notre métamodèle, l'architecture concrète et sa représentation potentielle en langage de description d'architecture (ADL/ACME).

| Concept M2 | Élément MiniNet (M1) | Traduction ACME (Exemple) |
| :--- | :--- | :--- |
| **Composant** | `UserManager` | `Component UserManager = { Ports { ... } }` |
| **Connecteur** | `RPC_Auth` | `Connector RPC_Auth = { Role caller; Role called; }` |
| **Service** | `login()` | `Property login_service : ServiceType;` |
| **Attachment** | Liaison Client-RPC | `Attachment Client.p_auth to RPC_Auth.caller;` |

Voici la suite de votre rapport, formatée en **Markdown (.md)**. Elle reprend exactement là où la partie précédente s'est arrêtée.

Cette section couvre la spécification formelle (ACME), la justification des choix (crucial pour la note) et les détails d'implémentation.

### 2.4 Évolution vers une architecture Distribuée et Persistante

L'architecture initiale a été enrichie pour répondre à des contraintes de scalabilité et de persistance :

Serveur d'Application : Le backend expose désormais une API REST (via Javalin) permettant au Client de s'exécuter sur une machine distante.

Persistance SQL : Les données ne sont plus volatiles mais stockées dans des bases de données SQLite.

Data Sharding (Répartition) : Pour améliorer les performances, nous avons implémenté une stratégie de sharding au niveau du connecteur de données.

Le système instancie 3 composants Storage distincts : un pour les utilisateurs, un pour les posts, un pour les messages.

Le SQLConnector agit comme un routeur intelligent : il analyse la table demandée (USERS, POSTS, etc.) et dirige la requête vers la bonne instance de base de données. Cela permet de séparer physiquement les données sans changer le code des composants métier (Manager).
-----


## 3. Spécification Formelle (ACME)

Cette section présente la description formelle de l'architecture du système MiniNet en utilisant une syntaxe proche du langage de description d'architecture (ADL) ACME. Cette formalisation permet de vérifier la cohérence structurelle de notre assemblage.

```acme
System MiniNet = {

  // --- DÉCLARATION DES COMPOSANTS ---

  Component Client = {
      Port p_auth_req;  // Vers UserManager
      Port p_post_req;  // Vers PostManager
      Port p_msg_req;   // Vers MessageService
  }

  Component UserManager = {
      Port p_auth_prov; // Reçoit auth
      Port p_db_user;   // Vers Storage
      Property services = { "login", "register", "addFriend", "removeFriend" };
  }

  Component PostManager = {
      Port p_post_prov; // Reçoit commandes posts
      Port p_db_post;   // Vers Storage
      Property services = { "createPost", "getWall" };
  }

  Component MessageService = {
      Port p_msg_prov;  // Reçoit commandes messages
      Port p_db_msg;    // Vers Storage
      Property services = { "sendMessage", "readMessages" };
  }

  // --- COMPOSANTS DE STOCKAGE (Sharding) ---
  Component StorageUsers = {
      Port p_db_prov;
      Property db_file = "users.db";
  }
   
  Component StoragePosts = {
      Port p_db_prov;
      Property db_file = "posts.db";
  }
   
  Component StorageMsgs = {
      Port p_db_prov;
      Property db_file = "messages.db";
  }

  // --- DÉCLARATION DES CONNECTEURS ---

  // Connecteur HTTP/REST Unique (Avec rôles distincts pour respecter le diagramme)
  Connector HTTP_Connector = {
      // Rôles coté Client
      Role caller_auth; 
      Role caller_post; 
      Role caller_msg;
      
      // Rôles coté Serveur
      Role called_auth; 
      Role called_post; 
      Role called_msg;
      
      Property glue = "REST / JSON over HTTP";
  }

  // Connecteur SQL avec logique de Sharding
  Connector SQL_Router_Conn = {
      Role requester;       // Utilisé par les managers (N-to-1)
      Role responder_u;     // Vers StorageUsers
      Role responder_p;     // Vers StoragePosts
      Role responder_m;     // Vers StorageMsgs
      Property glue = "JDBC Routing (Switch on Table Name)";
  }

  // --- ATTACHMENTS (Câblage du système) ---

  // 1. Connexions Client -> Managers (Via le Connecteur HTTP Unique)
  Attachment Client.p_auth_req to HTTP_Connector.caller_auth;
  Attachment HTTP_Connector.called_auth to UserManager.p_auth_prov;

  Attachment Client.p_post_req to HTTP_Connector.caller_post;
  Attachment HTTP_Connector.called_post to PostManager.p_post_prov;

  Attachment Client.p_msg_req to HTTP_Connector.caller_msg;
  Attachment HTTP_Connector.called_msg to MessageService.p_msg_prov;

  // 2. Connexions Managers -> SQL Router (Côté Requête)
  // Note : Plusieurs composants peuvent se connecter au même rôle "requester"
  Attachment UserManager.p_db_user to SQL_Router_Conn.requester;
  Attachment PostManager.p_db_post to SQL_Router_Conn.requester;
  Attachment MessageService.p_db_msg to SQL_Router_Conn.requester;
   
  // 3. Connexions SQL Router -> Storages (Côté Réponse / Distribution)
  Attachment SQL_Router_Conn.responder_u to StorageUsers.p_db_prov;
  Attachment SQL_Router_Conn.responder_p to StoragePosts.p_db_prov;
  Attachment SQL_Router_Conn.responder_m to StorageMsgs.p_db_prov;
}
```

-----

## 4\. Justification des Choix Architecturaux

La conception de MiniNet repose sur plusieurs décisions clés visant à garantir la modularité, l'évolutivité et la maintenabilité du système.

### 4.1 Séparation des Préoccupations (Managers vs Monolithe)

Nous avons choisi de ne pas créer un seul composant "Serveur" monolithique, mais de diviser la logique métier en trois composants distincts : `UserManager`, `PostManager` et `MessageService`.

  * **Justification :** Cette approche permet une évolution indépendante. Par exemple, si la logique de gestion des amis (`UserManager`) devient complexe, elle peut être modifiée sans impacter le service de messagerie. Cela facilite également le travail en parallèle au sein de l'équipe de développement.

### 4.2 Centralisation du Stockage (Composant Storage)

Bien que les managers soient séparés, nous avons opté pour un composant `Storage` unique accessible via un connecteur de données dédié.

  * **Justification :** Cela simplifie la cohérence des données (toutes les données sont au même endroit). L'utilisation d'un connecteur explicite (`SQL_Link`) découple les managers de la technologie de stockage. Si nous devions passer d'une base SQL à une base NoSQL ou un fichier texte, seul le code interne du connecteur et du composant `Storage` changerait, sans affecter la logique métier des Managers.

### 4.3 Choix du style "Call-Return" (RPC Synchrone)

Pour les interactions entre le Client et les Managers, nous utilisons une Glue de type RPC Synchrone.

  * **Justification :** L'expérience utilisateur (UX) d'un réseau social nécessite souvent un retour immédiat (savoir si le login a réussi, si le message est envoyé). Un modèle asynchrone aurait complexifié inutilement le client (gestion de callbacks) pour ce type d'opérations simples.

### 4.4 Connecteur Intelligent pour le Sharding

Nous avons choisi d'implémenter la logique de répartition des données (Sharding) à l'intérieur du SQLConnector plutôt que dans les Managers.

 * **Justification :** C'est un respect strict du principe de séparation des préoccupations. Les composants métier (UserManager, etc.) ne doivent pas savoir combien de bases de données existent ni où elles se trouvent. Ils envoient simplement une requête "Sauvegarde ceci". C'est la responsabilité du connecteur ("la glue") de savoir où et comment acheminer cette donnée. Cela permet d'ajouter de nouvelles bases de données (ex: une pour les Logs) en modifiant uniquement la configuration du connecteur, sans toucher au code métier.

-----

## 5\. Implémentation et Traçabilité (Prototype)

Le prototype a été développé en Java en respectant strictement le métamodèle M2 défini. L'objectif n'était pas d'utiliser des frameworks standards (comme Spring), mais de réifier les concepts architecturaux dans le code.


### 5.1 Structure du Code (Packages)

L'organisation des fichiers du prototype a été conçue pour mettre en évidence la traçabilité explicite entre l'architecture définie et le code, conformément aux objectifs du projet. Elle reflète la distinction stricte entre le niveau d'abstraction (M2) et l'application concrète (M1).

Voici l'arborescence détaillée du projet Java :

```text
src/
├── framework/                 <-- Niveau M2 (Abstractions)
│   ├── Component.java         (Classe abstraite de base)
│   └── Connector.java         (Classe abstraite de base)
│
├── architecture/              <-- Niveau M1 (Implémentation MiniNet)
│   ├── interfaces/            (Réification des Ports Fournis)
│   │   ├── IUserPort.java
│   │   ├── IPostPort.java
│   │   └── IStoragePort.java
│   │
│   ├── components/            (Instances de Composants)
│   │   ├── Client.java
│   │   ├── UserManager.java
│   │   ├── PostManager.java
│   │   └── Storage.java
│   │
│   └── connectors/            (Instances de Connecteurs)
│       ├── RPCConnector.java
│       └── SQLConnector.java
│
└── Main.java                  <-- Configuration (Instanciation & Binding)
```

**Description des paquetages :**

  * **`src/framework/`** : Ce package implémente les concepts du métamodèle architectural défini dans notre diagramme de classe M2. Il contient les classes abstraites `Component` et `Connector` héritant de la notion d'élément architectural. Ce code est générique et garantit que le prototype respecte la structure définie.
  * **`src/architecture/interfaces/`** : Ce package contient les définitions des **Ports Fournis**. Dans notre implémentation, les ports sont modélisés par des interfaces Java, respectant le principe selon lequel le composant contient directement ses définitions de services.
  * **`src/architecture/components/`** : Contient les implémentations concrètes des composants fonctionnels qui encapsulent les fonctionnalités du système (Gestion utilisateurs, Posts, etc.). Ces classes héritent de `framework.Component`.
  * **`src/architecture/connectors/`** : Contient la logique de "Glue" encapsulée dans des classes héritant de `framework.Connector`. Ces classes définissent concrètement les modes d'interaction (appel de procédure RPC ou requête SQL) entre les composants.
  * **`src/Main.java`** : Ce fichier représente la classe **Configuration** du métamodèle M2. Elle a la responsabilité de contenir les instances et les liens. C'est ici que sont instanciés les composants et que les liens d'attachement (*Attachments*) sont créés pour assembler le système final.

<!-- end list -->

```
```

### 5.2 Mapping Architecture $\leftrightarrow$ Code Java

Pour garantir la traçabilité exigée, nous avons adopté les conventions de mappage suivantes :

| Concept Architectural | Implémentation Java | Explication |
| :--- | :--- | :--- |
| **Port (Provided)** | **Interface Java** | Une interface (ex: `IAuthService`) définit le contrat public du port. |
| **Port (Required)** | **Champ privé** | Le besoin d'un service externe est représenté par une référence privée vers l'interface (ex: `private IStorage storagePort;`). |
| **Composant** | **Classe** | Une classe Java qui *implements* les interfaces de ses ports fournis. |
| **Attachment** | **Injection de dépendance** | Lier un port revient à passer l'instance du connecteur ou du composant via un *setter* ou le constructeur dans le `Main`. |

### 5.3 Extrait de code significatif

Cet extrait du `Main.java` montre comment la configuration architecturale est traduite en code :

```java

```

-----

## Conclusion tentative

Ce projet a permis de mettre en pratique les principes de l'architecture logicielle basée sur les composants et connecteurs. En partant d'un métamodèle (M2) clair, nous avons pu concevoir une architecture (M1) modulaire pour le système MiniNet.

L'implémentation du prototype a mis en évidence l'intérêt de cette approche : bien que plus verbeuse initialement que du code "spaghetti", elle offre une structure rigide qui force le découplage. Le système final respecte les contraintes de séparation et permettrait, théoriquement, de remplacer n'importe quel composant (comme la base de données ou l'interface client) sans impact majeur sur le reste du système.

```
```
