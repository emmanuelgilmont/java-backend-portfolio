# Notes — tentative de migration Spring Boot 3.5 → 4.1 / Java 21 → 25

> Checklist générique + retour d'expérience réel, issus d'un essai de migration mené sur un
> module du monorepo (annulé ensuite via `git reset --hard` — décision : les projets existants
> restent pinnés à leur version d'écriture, pas de migration systématique). Conservé ici comme
> référence réutilisable pour le premier vrai module qui adoptera Boot 4.1/Java 25
> (actuellement : la gateway, projet #5).

---

## Checklist générique — avant de migrer un module vers Boot 4.1 / Java 25

### Étape 0 — Avant de toucher au pom.xml

- [ ] Vérifier que le module est déjà propre sur son dernier patch 3.x — pas de warnings de
      dépréciation restants (`mvn compile`).
- [ ] Créer une branche dédiée, ne pas migrer sur `main` directement.
- [ ] Noter la baseline actuelle (Java, version Boot, dépendances clés) avant de commencer.

### Étape 1 — Baseline Java

- [ ] Confirmer que le JDK local est bien en 25 (Boot 4.0.x exige Java 21 minimum, donc un
      module déjà en Java 21 n'a pas de blocage ici, contrairement à un module resté en Java 17).
- [ ] Mettre à jour le JDK sur la machine de dev Windows + vérifier IntelliJ (SDK 25).

### Étape 2 — pom.xml

- [ ] Changer le parent `spring-boot-starter-parent` vers `4.1.0`.
- [ ] Vérifier `<java.version>` → `25`.
- [ ] `mvn compile` et lister toutes les erreurs de compilation avant de corriger quoi que ce soit
      (ne pas corriger au fil de l'eau — avoir la liste complète d'abord).

### Étape 3 — Jakarta EE 11

- [ ] Si le module est déjà en `jakarta.*` depuis la migration Boot 2→3, pas de nouveau
      renommage de packages attendu (Jakarta EE 11 est additif sur EE 10).
- [ ] Vérifier que les dépendances tierces sont compatibles Jakarta EE 11 / Servlet 6.1.

### Étape 4 — Jackson 2 → Jackson 3

- [ ] Vérifier les DTOs avec annotations Jackson custom ou un `ObjectMapper` configuré à la main.
- [ ] Si oui : les group IDs changent de `com.fasterxml.jackson` → `tools.jackson`
      (exception : `jackson-annotations` reste sur `com.fasterxml.jackson.annotation`).
- [ ] Si des tests font du matching JSON exact, s'attendre à des différences de formatting avec
      Jackson 3 et les revoir.
- [ ] Si une lib tierce a encore besoin de Jackson 2, Boot permet de garder les deux en parallèle.

### Étape 5 — Tests

- [ ] Confirmer qu'il n'y a aucun reliquat JUnit 4 — complètement retiré en Boot 4.0.
- [ ] Vérifier l'usage de `@MockBean` / `@SpyBean` — supprimés en 4.0 (remplaçants dès la 3.5).
- [ ] Déclarer Mockito comme agent explicite (voir point 4 des résultats ci-dessous) plutôt que
      de compter sur l'auto-attach dynamique.

### Étape 6 — Config & comportements par défaut

- [ ] `PropertyMapper` ne mappe plus les valeurs source `null` par défaut — vérifier tout code
      de config qui en dépendrait implicitement.
- [ ] Undertow est retiré en Boot 4 — vérifier le serveur embarqué utilisé par le module.
- [ ] Si Spring Security est présent : CSRF activé par défaut pour les endpoints API en
      Spring Security 7 — source fréquente de 403 après migration.

### Étape 7 — Après compilation propre

- [ ] `mvn test` (Layer 1 local).
- [ ] Test manuel des endpoints exposés par le module.
- [ ] Vérifier les logs de démarrage pour tout warning de dépréciation restant.
- [ ] Build + déploiement homelab habituel (SCP + docker compose) pour valider en conditions réelles.
- [ ] **Vérifier l'image de base du `Dockerfile` / `docker-compose.yml`** — un JAR compilé avec
      `--release 25` ne démarre pas sur un runtime Java 21 côté conteneur. La version Java d'un
      module vit à trois endroits indépendants (JDK local, `pom.xml`, image Docker) ; rien ne
      prévient si un seul est oublié.

---

## Résultats observés lors de l'essai (retour d'expérience réel)

> Rencontrés en exécutant cette checklist sur un module réel avant l'annulation du projet.
> Conservés tels quels — c'est la matière première du futur chapitre no-nonsense-backend
> (rédigé dans l'autre projet Claude).

### 1. Certificat TLS cassé après changement de JDK

`mvn test` a échoué au premier lancement avec `certificate_unknown` / `PKIX path building
failed` sur les téléchargements Maven depuis le Nexus interne (`nexus.localhouse`). **Pas un
breaking change Boot 4/Java 25** — chaque JDK a son propre `cacerts`, et le certificat interne
importé dans celui du JDK 21 n'existait pas dans celui du JDK 25 fraîchement installé.

**Fix :** exporter le certificat depuis le `cacerts` du JDK 21 (`keytool -exportcert`) et
l'importer dans celui du JDK 25 (`keytool -importcert`), ou le récupérer directement depuis le
serveur via `openssl s_client -connect nexus.localhouse:443 -showcerts`.

**Piège pour quiconque a une infra interne (Nexus/Artifactory) :** ce problème ne dépend pas du
module migré — il touchera **chaque nouvelle installation de JDK** sur la machine, avant même
de commencer une migration Boot/Java. Facile à rater parce que le message d'erreur pointe vers
Surefire, pas vers le vrai coupable (le changement de JDK).

### 2. Build/tests Maven — propres après le fix ci-dessus

Compilation et tests passent sans modification de code applicatif sur un module simple (pas de
Spring Security, pas de sérialisation Jackson custom). Confirmé sur ce cas : Jakarta EE 11 est
additif sur EE 10 (pas de nouveau renommage de packages), et l'absence de Jackson custom /
Spring Security évite les deux plus grosses sources de breaking changes citées dans les guides
de migration Boot 4.

### 3. Image Docker désynchronisée du JDK local

Le `Dockerfile` et le `docker-compose.yml` référençaient encore `eclipse-temurin:21-jre-jammy`.
Un JAR compilé avec `--release 25` ne démarre pas sur un runtime Java 21
(`UnsupportedClassVersionError` attendue au démarrage du conteneur, non testée jusqu'au bout
avant l'annulation du projet).

**Fix :** remplacer par `eclipse-temurin:25-jre-jammy` (tag confirmé disponible sur Docker Hub,
mêmes architectures amd64/arm64 que la version 21).

**Leçon générale :** la version Java d'un module vit à **trois endroits indépendants** — le JDK
local, le `pom.xml`, l'image de base Docker — et rien ne prévient automatiquement si un seul des
trois est oublié. Le symptôme n'apparaît qu'au déploiement, pas à la compilation locale.

### 4. Mockito — auto-attachement d'agent bientôt bloqué par le JDK

Warning au lancement des tests : *"Mockito is currently self-attaching to enable the
inline-mock-maker. This will no longer work in future releases of the JDK."* Lié à JEP 451
("Prepare to Disallow the Dynamic Loading of Agents"), une tendance amorcée dès Java 21 et pas
strictement propre à Java 25 — possible que ce warning existe déjà sur d'autres modules du
monorepo sans avoir été remarqué.

**Fix :** déclarer Mockito comme agent explicite dans le build plutôt que compter sur
l'auto-attach dynamique, via `maven-dependency-plugin:properties` + `argLine
-javaagent:${org.mockito:mockito-core:jar}` sur `maven-surefire-plugin` — résout le chemin réel
du jar dans le repo local (Nexus inclus) sans coder de version ni de chemin en dur.

### Ce qui n'a **pas** posé de problème (à noter aussi, c'est informatif)

- Aucun breaking change Jackson 2→3 rencontré (module sans sérialisation custom).
- Aucun conflit Servlet 6.1 / Jakarta EE 11 (déjà en `jakarta.*` depuis la migration Boot 2→3).
- Aucun souci de compatibilité observé entre les dépendances testées et Java 25 / Boot 4.1.
- `PropertyMapper` (valeurs `null` non mappées par défaut) : pas d'impact détecté sur ce module.

---

## Sources consultées (juillet 2026)

Synthèse croisée de plusieurs guides de migration Boot 4 publiés entre novembre 2025 et
mai 2026 (OpenLogic, Javarevisited, Moderne, Java Code Geeks, Loiane Groner, Katyella,
JSBisht Labs) + le wiki officiel `spring-projects/spring-boot` "Spring Boot 4.0 Migration Guide"
sur GitHub. Recoupé, pas pris d'une seule source.
