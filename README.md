# A Pivot Table Proof-of-Concept

A Pivot Table prototype for Nuveen; please see this support ticket
for more details:

[VS-3606](https://support.vaadin.com/browse/VS-3606)

## Gradle

To compile the app with Gradle just run

```
./gradlew
```

To start the app in dev mode, run

```
./gradlew appRun
```

Gradle will automatically download an embedded servlet container (Jetty) and will run your app in it. Your app will be running on
[http://localhost:8080](http://localhost:8080); you can also browse to [http://localhost:8080/exceptions](http://localhost:8080/exceptions)
for a demo pivot table of a list of exceptions. 

To build in production mode, just run:

```bash
./gradlew clean build -Pvaadin.productionMode
```

Vaadin will download nodejs and npm/pnpm automatically for you (handy for CI).

## Maven

To compile the app with Maven just run

```
./mvnw -C clean package
```

To start the app in dev mode, run

```
./mvnw -C clean package jetty:run
```

Maven will automatically download an embedded servlet container (Jetty) and will run your app in it. Your app will be running on
[http://localhost:8080](http://localhost:8080); you can also browse to [http://localhost:8080/exceptions](http://localhost:8080/exceptions)
for a demo pivot table of a list of exceptions. 

To build in production mode, just run:

```bash
./mvn -C clean package -Pproduction
```

Vaadin will download nodejs and npm/pnpm automatically for you (handy for CI).
