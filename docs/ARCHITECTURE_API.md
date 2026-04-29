# Roadmap: Architecture API Impact CO2 dans EcoAct

Cette petite roadmap vous explique l'architecture en 4 couches que nous venons de mettre en place pour l'API.

## 1. La Couche Modèle (`domain.model.impactco2`)
Ces classes, appelées *POJOs* (Plain Old Java Objects), servent uniquement à stocker la donnée que l'API nous renvoie (la désérialisation).
* `ImpactResponse<T>` : C'est le moule global de l'API (qui renvoie toujours `{ "data": [...], "warning": "..." }`).
* `TransportData`, `EcvData`, `AlimentationCategory`... : Sont les modèles stricts des objets renvoyés dans le tableau `"data"`.
* **Pourquoi ?** Plutôt que de manipuler des chaînes de caractères (JSON), on a des objets Java propres (`data.getName()`, `data.getValue()`), ce qui évite de nombreux bugs.

## 2. La Couche Définition de l'API (`data.network.ImpactCo2Api`)
C'est notre "contrat" avec l'API Impact CO2.
* On utilise des annotations `@GET("chemin")` ou `@Path` / `@Query` pour associer une méthode Java à une URL distante exacte.
* **Exemple** : `@GET("transport")` dit à Retrofit que quand j'appelle `getTransport()`, il faut taper sur `https://impactco2.fr/api/v1/transport`.

## 3. La Couche Constructeur (`data.network.RetrofitClient`)
C'est notre moteur HTTP.
* Il construit le client (OkHttp) et s'assure d'inscrire l'URL de base (`https://impactco2.fr/api/v1/`).
* Il contient un `Interceptor` : Une petite douane qui intercepte toute requête sortante pour lui accoler l'en-tête `Authorization: Bearer VOTRE_CLEF_API`.
* Il contient le convertisseur (Gson) qui traduit automatiquement le JSON de l'API en objets Java de l'étape 1.

## 4. La Couche Accès (Le "Repository" : `data.network.ImpactCo2Repository`)
C'est une bonne pratique d'architecture (Pattern Repository) : 
* La vue ou l'interface (Fragment/Activity) ne devrait jamais s'occuper directement de comment fonctionne Retrofit ou construire l'API.
* Le Fragment demande `repository.getAlimentation()`, le repository s'occupe de faire la liaison avec Retrofit et gère les requêtes. Vous pourriez avoir un "DatabaseRepository" et un "NetworkRepository", c'est transparent pour la vue.

---

## 🚦 Récapitulatif du Cycle Typique (Le parcours de l'information) :
1. Le Bouton **"Transport"** est cliqué dans `ImpactDemoFragment`.
2. Le `Fragment` appelle la méthode **`getTransport(50.0, 1, false)`** de son `ImpactCo2Repository`.
3. Le `Repository` demande au client Retrofit (`ImpactCo2Api`) d'effectuer un appel HTTP.
4. Le client OkHttp intercepte la requête, place la **clef d'API** et envoie l'appel sur internet.
5. Impact CO2 renvoie un JSON brut.
6. Gson ("ConverterFactory") lit le JSON brut et le lie aux objets Java (**`ImpactResponse<TransportData>`**).
7. Le résultat remonte la chaîne jusqu'au callback `onResponse` du Fragment, qui manipule l'objet fini dans votre `StringBuilder`.

