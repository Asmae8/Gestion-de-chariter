Gestion des Actions de Charité - Architecture Microservices
Application web de gestion des actions de charité développée avec Spring Boot, basée sur une architecture microservices.

Description
Ce projet permet de gérer des organisations caritatives, leurs actions humanitaires, les participations des utilisateurs et les dons. L'application se base sur un système de rôles afin de séparer les responsabilités entre l'administrateur général, les responsables d'organisations et les utilisateurs simples.

L'architecture a été décomposée en plusieurs microservices indépendants, communiquant via un serveur de découverte (Eureka) et un point d'entrée unique (API Gateway).

Architecture
Le projet est composé de 5 modules indépendants :

Eureka Server (Port 8761) : Le registre de services. Il permet à tous les microservices de s'enregistrer et de se découvrir mutuellement.

API Gateway (Port 8080) : Le point d'entrée unique. Elle reçoit toutes les requêtes des utilisateurs et les redirige dynamiquement vers le bon microservice en interrogeant Eureka.

Gestion-Chariter (Port 8081) : Le service principal (monolithe découpé). Il gère la sécurité (login), les actions caritatives et les dons. Il communique avec le service des organisations via OpenFeign.

User-Service (Port 8082) : Microservice dédié à la gestion des profils utilisateurs. Il possède sa propre base de données isolée.

Organization-Service (Port 8083) : Microservice dédié à la gestion des associations caritatives. Il possède également sa propre base de données isolée.

Rôles
SUPER_ADMIN : valide les organisations, supervise la plateforme et peut gérer les organisations/actions.

ORG_ADMIN : crée une ou plusieurs organisations et crée des actions pour ses organisations validées.

USER : consulte les actions disponibles, participe aux actions et effectue des dons.

Fonctionnalités principales
Inscription et connexion des utilisateurs (gérées par Gestion-Chariter).

Authentification par JWT stocké dans un cookie HTTP-only.

Gestion des rôles et des autorisations.

Création et validation des organisations (gérées par le microservice organization-service).

Gestion des utilisateurs avec un CRUD complet (géré par le microservice user-service).

Création des actions caritatives pour les organisations validées.

Participation des utilisateurs aux actions.

Enregistrement des dons.

Tableau de bord adapté selon le rôle connecté.

Routage dynamique des requêtes via la Gateway.

Communication inter-services via OpenFeign (ex: récupération des organisations pour le tableau de bord).

Technologies utilisées
Java 17

Spring Boot (v3.5.13)

Spring Cloud (Eureka, Gateway, OpenFeign)

Spring Security (JWT)

Spring Data JPA / Hibernate

Thymeleaf (Interfaces web pour les microservices)

Bases de données H2 persistantes (fichiers locaux isolés pour chaque microservice)

MySQL ou PostgreSQL possibles via profils de configuration

Maven

Git (avec tag de version v1.0.0)

Démarrage
Pour lancer le projet localement, il faut démarrer les applications dans cet ordre précis :

Lancer le Eureka Server (attendre que la page http://localhost:8761 s'affiche).

Lancer le Gestion-Chariter (Port 8081).

Lancer le User-Service (Port 8082).

Lancer l'Organization-Service (Port 8083).

Lancer l'API Gateway (Port 8080).

Accès aux interfaces via la Gateway :

Application principale : http://localhost:8080/

Microservice Utilisateurs : http://localhost:8080/user-service/web/users

Microservice Organisations : http://localhost:8080/organization-service/web/organizations
