# Corruption Clicker

Corruption Clicker is an incremental/clicker game built with Java and JavaFX. The game features upgrades, persistent save data, and a modular architecture for easy extension.

## Features
- Built with Java 25 and JavaFX 25
- Modular code structure (core, model, persistence, UI)
- Upgrade system with definitions and instances
- Persistent save/load functionality
- Customizable UI with CSS styling

## Requirements
- Java 25+
- Maven 3.6+

## Building the Project
To build the project and create a runnable JAR:

```sh
mvn clean package
```

The shaded JAR will be located in `target/corruptionclicker-1.0-SNAPSHOT-all.jar`.

## Running the Game
You can run the game using the JavaFX Maven plugin:

```sh
mvn javafx:run
```

Or run the shaded JAR directly:

```sh
java -jar target/corruptionclicker-1.0-SNAPSHOT-all.jar
```
