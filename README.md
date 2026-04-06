
https://github.com/user-attachments/assets/31f1e10e-b80b-4e5f-80df-0905c4c289a9
# 📒 NumberBook

Application Android connectée à un backend PHP/MySQL permettant de lire les contacts du téléphone, de les synchroniser vers un serveur distant et d'effectuer des recherches par nom ou numéro.

---

## 📱 Aperçu

| Écran principal | Recherche | Synchronisation |
|---|---|---|
| Chargement des contacts depuis le téléphone | Recherche distante par nom ou numéro | Envoi vers la base MySQL via API REST |

---

## ✨ Fonctionnalités

- 📋 Lecture des contacts système Android via `ContentResolver`
- 🔐 Gestion des permissions runtime (`READ_CONTACTS`)
- 📡 Synchronisation des contacts vers un serveur distant
- 🔍 Recherche distante par nom ou numéro de téléphone
- 🎨 Interface moderne avec `RecyclerView`, avatars et badges
- 🔄 Communication client/serveur via **Retrofit 2** + **Gson**
  

---
## demo


https://github.com/user-attachments/assets/5bd45aa3-1ee8-4959-ace6-c9e911a6c07f



## 🏗️ Architecture

```
NumberBook
├── Android App (Java)
│   ├── MainActivity.java        # Logique principale
│   ├── ContactAdapter.java      # RecyclerView adapter
│   ├── ContactApi.java          # Interface Retrofit
│   ├── RetrofitClient.java      # Singleton HTTP client
│   ├── Contact.java             # Modèle de données
│   └── ApiResponse.java         # Réponse serveur
│
└── Backend PHP
    ├── config/Database.php      # Connexion PDO
    ├── model/Contact.php        # Modèle métier
    ├── service/ContactService.php
    └── api/
        ├── insertContact.php    # POST — insertion
        ├── getAllContacts.php   # GET  — liste complète
        └── searchContact.php   # GET  — recherche
```

---

## 🛠️ Technologies

| Côté | Technologie |
|---|---|
| Mobile | Android (Java), Retrofit 2, Gson, RecyclerView |
| Backend | PHP 8, PDO |
| Base de données | MySQL / MariaDB |
| Serveur local | XAMPP / WAMP |

---

## ⚙️ Installation

### 1. Base de données

Importer le script SQL dans phpMyAdmin ou via le terminal :

```sql
CREATE DATABASE IF NOT EXISTS numberbook
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE numberbook;

CREATE TABLE contact (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(150) NOT NULL,
    phone      VARCHAR(50)  NOT NULL,
    source     VARCHAR(50)  DEFAULT 'mobile',
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP
);
```

### 2. Backend PHP

Copier le dossier `numberbook-api/` dans le répertoire web de votre serveur :

```
C:/xampp/htdocs/numberbook-api/   ← Windows (XAMPP)
/var/www/html/numberbook-api/     ← Linux
```

Vérifier la configuration dans `config/Database.php` :

```php
private $host     = "localhost";
private $dbName   = "numberbook";
private $username = "root";
private $password = "";
```

### 3. Application Android

Cloner le projet et l'ouvrir dans **Android Studio** :

```bash
git clone https://github.com/votre-username/NumberBook.git
```

Modifier l'adresse IP dans `RetrofitClient.java` :

```java
private static final String BASE_URL = "http://VOTRE_IP_LOCALE/numberbook-api/api/";
```

> 💡 Pour trouver votre IP locale : `ipconfig` (Windows) ou `ifconfig` (Linux/Mac).
> Le téléphone et le serveur doivent être sur le **même réseau Wi-Fi**.

Synchroniser Gradle puis lancer l'application sur un émulateur ou un appareil physique.

---

## 📡 API REST

Base URL : `http://<IP_SERVEUR>/numberbook-api/api/`

| Méthode | Endpoint | Description | Corps |
|---|---|---|---|
| `POST` | `/insertContact.php` | Insérer un contact | `{"name":"Ali","phone":"0612..."}` |
| `GET` | `/getAllContacts.php` | Récupérer tous les contacts | — |
| `GET` | `/searchContact.php?keyword=ali` | Recherche par nom ou numéro | — |

### Exemple de réponse — insertion

```json
{
  "success": true,
  "message": "Contact inséré avec succès"
}
```

### Exemple de réponse — liste

```json
[
  {
    "id": 1,
    "name": "Ali Mohamed",
    "phone": "+212612345678",
    "source": "mobile",
    "created_at": "2024-01-15 10:30:00"
  }
]
```

---

## 📦 Dépendances Android

```gradle
implementation 'com.squareup.retrofit2:retrofit:2.11.0'
implementation 'com.squareup.retrofit2:converter-gson:2.11.0'
implementation 'androidx.recyclerview:recyclerview:1.3.2'
implementation 'com.google.android.material:material:1.11.0'
```

---

## 🔐 Permissions

```xml
<uses-permission android:name="android.permission.READ_CONTACTS" />
<uses-permission android:name="android.permission.INTERNET" />
```

---

## 🧪 Tests

| Test | Action | Résultat attendu |
|---|---|---|
| Base de données | Vérifier la table `contact` dans phpMyAdmin | Table présente avec les 5 colonnes |
| API liste | Ouvrir `getAllContacts.php` dans le navigateur | JSON avec la liste des contacts |
| API recherche | Ouvrir `searchContact.php?keyword=ali` | JSON filtré |
| API insertion | Requête POST via Postman | `{"success":true}` |
| Chargement | Cliquer sur "Charger contacts" | Permission demandée puis contacts affichés |
| Synchronisation | Cliquer sur "Synchroniser" | Contacts insérés dans MySQL |
| Recherche | Saisir un nom et cliquer "OK" | Liste filtrée depuis le serveur |

---

## 🚀 Améliorations possibles

- [ ] Détection et suppression des doublons avant insertion
- [ ] Normalisation des numéros de téléphone (suppression espaces, tirets)
- [ ] Barre de progression pendant la synchronisation
- [ ] Authentification utilisateur (token JWT)
- [ ] Sauvegarde locale avec **Room**
- [ ] Synchronisation bidirectionnelle
- [ ] Suppression et mise à jour d'un contact distant
- [ ] Recherche instantanée (sans clic)

---

## 📁 Structure des fichiers

```
NumberBook/
│
├── app/src/main/
│   ├── java/com/example/numberbook/
│   │   ├── MainActivity.java
│   │   ├── ContactAdapter.java
│   │   ├── ContactApi.java
│   │   ├── RetrofitClient.java
│   │   ├── Contact.java
│   │   └── ApiResponse.java
│   │
│   ├── res/layout/
│   │   ├── activity_main.xml
│   │   └── item_contact.xml
│   │
│   └── AndroidManifest.xml
│
└── numberbook-api/
    ├── config/Database.php
    ├── model/Contact.php
    ├── service/ContactService.php
    └── api/
        ├── insertContact.php
        ├── getAllContacts.php
        └── searchContact.php
```

---

## 👨‍💻 Auteur

Projet réalisé dans le cadre d'un lab de développement mobile Android connecté.








