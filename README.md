# Perspectives

https://perspectives.pole-emploi.fr

## Présentation du projet

Chaque année, plus de 60% des candidats validés par la [Méthode de Recrutement par Simulation Pôle emploi](https://www.pole-emploi.fr/candidat/les-ateliers-de-pole-emploi/la-methode-de-recrutement-par-si.html) ne sont pas recrutés par les 
entreprises utilisatrices de la méthode (environ 40.000 candidats).

Ces candidats manquent de visibilité vis-à-vis des recruteurs, organismes de formation et conseillers Pôle emploi 
(promotion de leur MRS, pédagogie autour de la méthode) et bénéficient de peu de services post-MRS.

Les recruteurs et OF n’ont aucune visibilité des candidats validés MRS et de la méthode sur pole-emploi.fr.

Perspectives est un site web qui permet :

* aux candidats validés MRS disponibles de rendre visible leur MRS, leurs habiletés, de se positionner 
sur d'autres secteurs en tension et de consulter des offres ne nécessitant ni qualification ni expérience.

* aux recruteurs, organismes de formation et conseillers Pôle-emploi de consulter les profils MRS disponibles, 
le métier validé, les habiletés liées et de comprendre la méthode de validation, de recevoir des profils 
correspondant à leurs offres.

## Stack technique

L'application est développée en Scala 2.12 et s'appuie sur le framework web Play 2.7.
https://www.playframework.com/documentation/2.7.x/Home

Au niveau du front, on utilise la librairie VueJS 2.6.10 : https://vuejs.org/v2/guide/

Deux modules sont présents dans le code :
* un module `webapp` dont le rôle est d'afficher un site web permettant de mettre en relation 
les candidats demandeurs d'emploi avec des offres sans qualification ni expérience, et de permettre 
aux recruteurs de trouver des candidats pertinents en fonction de leurs besoins

* un module `batchs` dont le rôle est de consommer des fichiers de données du SI Pôle Emploi et d'effectuer
des traitements périodiques. Vous trouverez [une synthèse des données et de leur rôle ici](https://drive.google.com/open?id=1J7D5dEhBxKaQ8tBXW7yzStBOhcv6659UrFftFkNXiPM)

## Installation

* Installer docker
* Installer sbt : https://www.scala-sbt.org/1.x/docs/Setup.html

* Builder les images docker localement :
```bash
cd docker
sudo docker build -f dockerfile-postgresql -t perspectives-postgresql .
```

* Démarrer les conteneurs Elasticsearch (6.5) et Postgresql (10.4) en local :

```bash
sudo docker run -p 5432:5432 -e POSTGRES_PASSWORD=perspectives -e POSTGRES_USER=perspectives -v perspectives_postgresql:/var/lib/postgresql/data perspectives-postgresql
sudo docker run -p 9200:9200 -p 9300:9300 -v perspectives_projections_elasticsearch:/usr/share/elasticsearch/data perspectives-elasticsearch
```

## Configuration

Créer des fichiers de conf locaux dans les répertoires de configuration, 
qui ne seront pas versionnés et contiendront les paramètres de conf propres 
au lancement local des applications, notamment l'utilisation de la BDD et 
de l'ES dockerisés lancés en local :

```bash
include "application"

# Pas de HTTPS en local
play.http.session.secure = false
play.filters.csp.directives {
  base-uri = "'self'"
  script-src = "'self' 'unsafe-eval' 'unsafe-inline' http://www.googletagmanager.com http://tagmanager.google.com http://www.google-analytics.com http://*.hotjar.com https://*.crisp.chat https://polyfill.io"
}

db {
  postgresql {
    host = "localhost"
    dbName = "perspectives"
    user = "perspectives"
    password = "perspectives"
  }
}

elasticsearch {
  host = "http://localhost"
  port = 9200
}
```

Il est possible de mocker tous les appels à des connecteurs (APIs, ), 
voici la liste exhaustive des configurations pour mocker les appels :

```bash
useMailjet = false
usePEConnect = false
useImportHabiletesMRS = true
useSlackNotification = false
useGoogleTagManager = false
useReferentielMetier = false
useReferentielRome = false
useReferentielOffre = false
useReferentielProspectCandidat = false
useReferentielHabiletesMRS = false
useLocalisation = false
useReferentielRegion = false
```

## Lancement

#### Batchs

```bash
sbt -mem 2048 -jvm-debug 5005 -Dconfig.file=./batchs/conf/application-local.conf
sbt:perspectives> project batchs
[perspectives-batchs] $ run
```

Pour que les batchs ne se lancent, il faut taper sur l'appli Play démarrée sur le port 9000 : http://localhost:9000

Ensuite ce sont les acteurs Akka qui vont se charger de consommer les fichiers qui seront déposés 
dans le répertoire d'entrée, configuré via `exportPoleEmploi.directory`.

Il faut bien penser à modifier les crons dans la conf `akka.quartz.schedules` pour que les fichiers soient consommés correctement.

#### Webapp

```bash
sbt -mem 2048 -jvm-debug 5005 -Dconfig.file=./webapp/conf/application-local.conf
sbt:perspectives> project webapp
[perspectives-webapp] $ run
```

Et accéder à l'application déployée localement : http://localhost:9000

Le build sbt s'occupe de compiler le code Scala mais également 
de déclencher le build des ressources front via Webpack.

#### Tests

Pour lancer les tests sur le projet :
```bash
sbt -mem 2048 -jvm-debug 5005
sbt:perspectives> ;clean;compile;test
```

## Contribuer

Le projet est open-source, vous pouvez donc contribuer à améliorer le produit sous la forme de pull-requests.

#### Release

Une fois la pull-request validée, le merge dans la branche `master` va déclencher un pipeline [Gitlab-CI](https://docs.gitlab.com/ee/ci/).

Ce pipeline contient un job `🚀_release` qu'il conviendra de déclencher manuellement afin de générer une release et un tag de la nouvelle version.

Une fois ce job terminé, la création du tag de la nouvelle version dans git va générer un nouveau pipeline de génération des images docker des batchs et de la webapp (jobs `📦_package` et `🐳_docker`).

Ces images docker seront publiées dans le registry docker interne au projet gitlab.

#### Déploiement

Le déploiement utilise [docker-swarm](https://docs.docker.com/engine/swarm/) qui permet notamment de ne pas avoir d'interruption de service et un redémarrage 
des conteneurs en cas de souci.

Toute la configuration swarm se trouve dans le répertoire `docker` du projet, 
dans les fichiers `docker-cloud.xxx.yml` spécifiques à chaque environnement.

Pour déployer la version 1.0.78 de l'appli, il suffit de se connecter sur le serveur de production et de lancer les commandes suivantes :

```bash
sudo docker service update --image registry.beta.pole-emploi.fr/perspectives/perspectives/perspectives-batchs:1.0.78 --with-registry-auth perspectives_batchs
sudo docker service update --image registry.beta.pole-emploi.fr/perspectives/perspectives/perspectives-webapp:1.0.78 --with-registry-auth perspectives_webapp
```

A noter qu'il faut préalablement avoir configuré un token d'authentification via 
`docker login registry.beta.pole-emploi.fr` pour pouvoir accéder au registry privé gitlab où sont publiées les images du projet.

Il faut également avoir lancé la stack docker une fois pour pouvoir mettre à jour les services via les commandes précédentes.

Pour déployer la stack complète Perspectives de prod :
```bash
sudo docker stack deploy --compose-file docker-cloud.yml -c docker-cloud.prod.yml perspectives
```
