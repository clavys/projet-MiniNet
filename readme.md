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
