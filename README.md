# What is this?

A simple server application for storing high score data, intended to be used by online browser based games.

It lets you keep track of high score data using HTTP REST API's.

The server is currently running at [https://highscore.sasagu.com](https://highscore.sasagu.com).

# Highlight

- Cost effective solution designed to run on Google Cloud platform.

# Technology Stack

- Java 21
- Google App Engine
- Spring Boot
- Google Cloud Datastore
- Google Cloud Scheduler
- Maven

# Requirements to run

- An account with Google Cloud Platform with billing enabled.
- A project in Google Cloud

# How to deploy

- Install gcloud CLI and initialize your environment:

```
	gcloud init
```

- Edit pom.xml, update app engine config:
	
```
	<configuration>
		<version>1-0-0</version>
		<!-- 
			set your google cloud project id here.
			Or set it with system property.
			Example: clean package -Dapp.deploy.projectId=[your_project_id] appengine:deploy
		-->
		<projectId></projectId>
	</configuration>	
```

- Run this Maven command/goal to deploy it to Google App Engine: 	

```
	clean package appengine:deploy
```
	
- Setup Cloud Scheduler (App Engine Cron Jobs) by deploying src/main/appengine/cron.yaml.
Run this gcloud command:

```
	gcloud app deploy cron.yaml
```
	
# Things to consider

- src/main/resources/static/index.html - This web page will be displayed when the application
 is accessed with a browser. Most likely you will want to change this.
 
- src/main/appengine/app.yaml - This configures the instance that will be deployed to Google App Engine. 
By default, it creates an F1 class instance with max 1 instance running.

- When running in multiple instances, it is possible to have a race condition when multiple users
try to add score at the same time.
