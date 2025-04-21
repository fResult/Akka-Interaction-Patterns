# Akka Interaction Patterns for Java (Exercises)

This repository is to do assigned exercises from
the [Akka Interaction Pattern for Java](https://akkademy.akka.io/learn/courses/48/akka-interaction-patterns-for-java)
course OFFERED BY [AKKADEMY](https://akkademy.akka.io)

## Prerequisites

- Java (JDK) 23 or newer
- Apache Maven 3.9.9 (to be safe, the version should be `3.9.0 <= version < 4.0.0`)
- IntelliJ IDEA or your preferred IDE
- Scala 3.6.x or newer \[for [Scala exercise](#scala-exercises)\]
- SBT 1.10.x or newer \[for [Scala exercise](#scala-exercises)\] (to be safe, the version should be `1.10.11 <= version < 2.0.0`)

\[Optional\] You can use [SDKMAN!](https://sdkman.io/install) to install JDK23, Maven, Scala, and SBT:

Installation Example:

```bash
sdk install java 23.0.2-librca # or your preferred version
sdk install maven 3.9.9

sdk install scala 3.6.4
sdk install sbt 1.10.11
```

## Dependencies Upgraded from the actual exercises

### Akka Profile dependencies

- `com.typesafe.akka:akka-bom_2.13` - `v2.9.4` to `com.typesafe.akka:akka-bom_3` - `v2.10.0`
    - `akka-actor-typed_2.13`  to `akka-actor-typed_3`
    - `akka-actor-testkit-typed_2.13` to `akka-actor-testkit-typed_3`

### Libraries

- `ch.qos.logback:logback-classic` from `1.2.11` to `1.5.18`
- `junit` from `4.13.2` to `4.13.2` (not upgrade yet, and as Akka documentation doesn't mention of using JUnit Jupiter (JUnit 5))

### Plugins

- `org.apache.maven.plugins:maven-compiler-plugin` from `3.5.1` to `3.14.0`
- `org.codehaus.mojo:exec-maven-plugin` from `1.6.0` to `3.5.0`
- `org.apache.maven.plugins:maven-surefire-plugin` from `2.2.2` to `3.5.3`

## Further Learning

### Java Exercises

Using OO-Style

1. [Request Response Pattern - Java](./exercises/java/001_request_response_pattern)
2. [Adapted Response Pattern - Java](./exercises/java/002_adapted_response_pattern)
   - Use *record classes* with *sealed interfaces* instead of *final classes*
3. [Timers - Java](./exercises/java/003_timers)
   - Use *record classes* with *sealed interfaces* instead of *final classes*
   - Use *pattern matching* instead of `onMessage` in `Behavior`

### Scala Exercises

Using Functional-Style (with immutable state)

1. [Request Response Pattern - Scala](./exercises/scala/001_request-response-pattern)
2. [Adapted Response Pattern - Scala](./exercises/scala/002_adapted-response-pattern)
3. [Timers - Scala](./exercises/scala/003_timers)
