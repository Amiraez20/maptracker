# 🗺️ TP Android — Géolocalisation avec OSMDroid

> Suivi de position en temps réel sur carte OpenStreetMap — sans clé API, sans carte bancaire.

---

## 📋 Objectifs du TP

- Afficher une carte interactive dans une application Android
- Demander la permission de localisation à l'exécution (runtime permission)
- Écouter les changements de position via le `NETWORK_PROVIDER`
- Déplacer un marker à chaque nouvelle position reçue
- Afficher une boîte de dialogue si la localisation est désactivée
- Zoomer et centrer la caméra sur la position actuelle

---

## 🛠️ Technologies utilisées

| Technologie | Rôle |
|-------------|------|
| **Android Studio** | IDE de développement |
| **Java** | Langage de programmation |
| **OSMDroid 6.1.18** | Bibliothèque de carte (OpenStreetMap) |
| **LocationManager** | Service Android de géolocalisation |
| **NETWORK_PROVIDER** | Fournisseur de position (Wi-Fi / 4G) |

> **Pourquoi OSMDroid ?** Contrairement à Google Maps SDK, OSMDroid est 100% gratuit et ne nécessite ni clé API ni compte de facturation.

---

## 📁 Structure du projet

```
MapTracker/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/maptracker/
│   │   │   └── MapsActivity.java        ← Logique principale
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   └── activity_main.xml    ← Vue MapView OSMDroid
│   │   │   └── values/
│   │   │       └── strings.xml
│   │   └── AndroidManifest.xml          ← Permissions
│   └── build.gradle                     ← Dépendance OSMDroid
```

---

## ⚙️ Installation & Configuration

### 1. Créer le projet

- Android Studio → **New Project** → **Empty Views Activity**
- Nom du projet : `MapTracker`
- Package : `com.example.maptracker`
- Language : `Java`
- Min SDK : `API 24`

### 2. Ajouter la dépendance OSMDroid

Dans `build.gradle (Module: app)`, ajouter dans `dependencies` :

```groovy
implementation 'org.osmdroid:osmdroid-android:6.1.18'
```

Cliquer **Sync Now**.

### 3. Configurer `AndroidManifest.xml`

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    android:maxSdkVersion="29" />
```

Ajouter aussi `android:usesCleartextTraffic="true"` dans la balise `<application>`.

---

## 📄 Fichiers principaux

### `activity_main.xml`

Contient uniquement la vue `MapView` d'OSMDroid qui occupe tout l'écran.

```xml
<org.osmdroid.views.MapView
    android:id="@+id/carte_osm"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

### `MapsActivity.java`

Contient toute la logique de l'application, organisée en méthodes distinctes :

| Méthode | Rôle |
|---------|------|
| `onCreate()` | Point d'entrée — initialise OSMDroid, vérifie la permission |
| `configurerCarte()` | Configure la source de tuiles, le zoom et le marker de départ |
| `demarrerSuiviPosition()` | Lance l'écoute du `NETWORK_PROVIDER` |
| `ecouteurPosition` | `LocationListener` — traite chaque nouvelle position |
| `afficherDialogueGpsDesactive()` | Boîte de dialogue si la localisation est coupée |
| `onRequestPermissionsResult()` | Résultat de la demande de permission |
| `onResume()` / `onPause()` | Cycle de vie OSMDroid (obligatoire) |

---

## 🔑 Concepts clés

### Permission runtime (Android 6+)

La permission dans le `Manifest` ne suffit pas. Il faut aussi la demander à l'exécution :

```java
ActivityCompat.requestPermissions(
    this,
    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
    CODE_DEMANDE_PERMISSION
);
```

Sans ça, `requestLocationUpdates()` lève une `SecurityException`.

### NETWORK_PROVIDER vs GPS_PROVIDER

| Provider | Précision | Vitesse | Intérieur |
|----------|-----------|---------|-----------|
| `NETWORK_PROVIDER` | Moyenne (~50m) | Rapide | ✅ Oui |
| `GPS_PROVIDER` | Haute (~5m) | Lente | ❌ Non |

Ce TP utilise `NETWORK_PROVIDER` pour fonctionner en intérieur et sur émulateur.

### Marker unique

Pour éviter de polluer la carte avec des dizaines de markers, on déplace le même marker à chaque update :

```java
if (marqueurActuel == null) {
    marqueurActuel = new Marker(carteVue);
    carteVue.getOverlays().add(marqueurActuel);
} else {
    marqueurActuel.setPosition(nouvelleCoord);
}
```

### Zoom et animation de caméra

```java
carteVue.getController().animateTo(nouvelleCoord);  // déplacement fluide
carteVue.getController().setZoom(15.0);             // niveau rue/quartier
```

---

## 🧪 Tests sur émulateur
Voir la video de demo:

https://youtube.com/shorts/8nZbtJUZrw4

---

## 🔴 Problèmes fréquents

| Problème | Cause probable | Solution |
|----------|---------------|----------|
| Carte blanche / pas de tuiles | Pas d'Internet ou `usesCleartextTraffic` manquant | Vérifier le Manifest + connexion réseau |
| Marker ne bouge pas | Permission refusée ou provider désactivé | Vérifier Paramètres > Localisation |
| `SecurityException` au lancement | Permission non vérifiée avant `requestLocationUpdates` | Ajouter le check `checkSelfPermission` |
| Dialog GPS ne s'affiche pas | `onProviderDisabled` non déclenché | Désactiver la localisation manuellement dans les paramètres |
| App crash à `onPause` | `removeUpdates` appelé sans listener actif | S'assurer que `ecouteurPosition` est bien initialisé |

---

## 📚 Références

- [OSMDroid Wiki](https://github.com/osmdroid/osmdroid/wiki)
- [Android LocationManager Docs](https://developer.android.com/reference/android/location/LocationManager)
- [OpenStreetMap](https://www.openstreetmap.org)
