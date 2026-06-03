# Documentation conceptuelle - Banking Account Management System

Ce dossier contient les documents conceptuels alignes avec le code actuel de `banking-app`.

## Ordre de lecture recommande

1. [Architecture microservices](./BANKING_01_ARCHITECTURE_MICROSERVICES.md)
   - Vue globale de l'application.
   - Services, ports, bases de donnees, Kafka, securite, deployment local et Kubernetes.

2. [Fonctionnalites metier](./BANKING_02_FONCTIONNALITES_METIER.md)
   - Acteurs, roles, use cases.
   - Parcours client, gestion compte, paiements, notifications.
   - Regles metier et scenarios a tester.

3. [Diagrammes UML](./BANKING_03_UML_DIAGRAMMES.md)
   - Cas d'utilisation.
   - Composants.
   - Sequences.
   - Etats.
   - Classes par domaine.
   - Activites metier.

4. [Stack technologique](./BANKING_04_STACK_TECHNOLOGIQUE.md)
   - Technologies utilisees.
   - Diagramme de stack.
   - Securite.
   - Donnees.
   - Kafka.
   - CI/CD.

## Notes

- Les diagrammes sont ecrits en Mermaid.
- Ils peuvent etre affiches dans GitLab, IntelliJ, VS Code ou tout viewer Markdown compatible Mermaid.
- Les documents refletent le code actuel, y compris les limites identifiees:
  - reset/change password pas encore expose en API;
  - notification complete dependante de Kafka;
  - IDs API actuellement bases sur `Long`;
  - JWT HS256 partage en local/dev.

