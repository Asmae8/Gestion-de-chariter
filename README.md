# Gestion des Actions de Charite

Application web de gestion des actions de charite developpee avec Spring Boot.

## Description

Ce projet permet de gerer des organisations caritatives, leurs actions humanitaires, les participations des utilisateurs et les dons. L'application se base sur un systeme de roles afin de separer les responsabilites entre l'administrateur general, les responsables d'organisations et les utilisateurs simples.

## Roles

- `SUPER_ADMIN` : valide les organisations, supervise la plateforme et peut gerer les organisations/actions.
- `ORG_ADMIN` : cree une ou plusieurs organisations et cree des actions pour ses organisations validees.
- `USER` : consulte les actions disponibles, participe aux actions et effectue des dons.

## Fonctionnalites principales

- Inscription et connexion des utilisateurs.
- Authentification par JWT stocke dans un cookie HTTP-only.
- Gestion des roles et des autorisations.
- Creation des organisations par les administrateurs d'organisation.
- Validation des organisations par le super administrateur.
- Creation des actions caritatives pour les organisations validees.
- Participation des utilisateurs aux actions.
- Enregistrement des dons.
- Tableau de bord adapte selon le role connecte.

## Technologies utilisees

- Java 17
- Spring Boot
- Spring Security
- Spring Data JPA / Hibernate
- Thymeleaf
- H2 pour le developpement local
- MySQL ou PostgreSQL possibles via profils de configuration
- Maven

## Comptes de demonstration

Les comptes suivants sont crees automatiquement en profil local lorsque `app.seed-demo-data=true`.

```text
SUPER_ADMIN : admin@charity.com / admin123
ORG_ADMIN   : org@charity.com / org12345
USER        : user@charity.com / user1234
```

## Lancement local

```bash
./mvnw spring-boot:run
```

Sous Windows :

```powershell
.\mvnw.cmd spring-boot:run
```

L'application est accessible par defaut sur :

```text
http://localhost:8080
```
