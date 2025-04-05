# Akka Interaction Patterns for Java (Exercises)

This repository is to do assigned exercises from
the [Akka Interaction Pattern for Java](https://akkademy.akka.io/learn/courses/48/akka-interaction-patterns-for-java)
course OFFERED BY [AKKADEMY](https://akkademy.io)

## Prerequisites

- Java (JDK) 23 or more
- Apache Maven 3.9.9 (to be safe, the version should be `3.9.0 <= version < 4.0.0`)
- IntelliJ IDEA or your preferred IDE

\[Optional\] You can use [SDKMAN!](https://sdkman.io/install) to install JDK23, and Maven:

Installation Example:

```bash
sdk install java 23.0.2-librca # or your preferred version
sdk install maven 3.9.9
```

## Dependencies Upgraded from the actual exercises

### Akka Profile dependencies

- `com.typesafe.akka:akka-bom_2.13` - `v2.9.4` to `com.typesafe.akka:akka-bom_3` - `v2.10.0`
    - `akka-actor-typed_2.13`  to `akka-actor-typed_3`
    - `akka-actor-testkit-typed_2.13` to `akka-actor-testkit-typed_3`

### Libraries

- `ch.qos.logback:logback-classic` from `1.2.11` to `1.5.18`
- `junit` from `4.13.2` to `4.13.2` (not update yet)

### Plugins

- `org.apache.maven.plugins:maven-compiler-plugin` from `3.5.1` to `3.14.0`
- `org.codehaus.mojo:exec-maven-plugin` from `1.6.0` to `3.5.0`
- `org.apache.maven.plugins:maven-surefire-plugin` from `2.2.2` to `3.5.3`
