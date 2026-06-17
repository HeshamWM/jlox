# jlox
This is my implementation of the book Crafting Interpreters by Robert Nystrom—an interpreter for the Lox language.

## Requirements
- Java 19+
- Maven

## Run
Compile the project:

```bash
mvn compile
```

Run in prompt mode (scanner over interactive input):

```bash
mvn exec:java
```

Run against a `.lox` file:

```bash
mvn exec:java -Dexec.args="path/to/file.lox"
```

At this stage, running prints scanned tokens.