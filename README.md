# UML Reviewer
gradle + kotlin + java 11 + openjfx 13 + tornadofx 2

## Running the application

### 1. Using gradle
Run the following command in the top directory:
```
./gradlew run
```

### 2. Using IDE
Simply open the project in your IDE and run the `MyAppRun.kt` class. Works in IntelliJ IDEA.


## Building a fat JAR

Run the `shadowJar` Gradle task. 

This should result in `build/libs/UMLReviewer-{version}-all.jar` JAR file. 
