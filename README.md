# codemodel
A language-agnostic Java framework for representing, enriching, and compiling code models.

[![CI](https://github.com/Workday/codemodel.build/actions/workflows/main-pull-request.yml/badge.svg)](https://github.com/Workday/codemodel.build/actions/workflows/main-pull-request.yml)
[![Maven Central](https://img.shields.io/maven-central/v/build.codemodel/codemodel-foundation)](https://central.sonatype.com/artifact/build.codemodel/codemodel-foundation)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)

## Overview

`codemodel` provides a structured, serializable representation of software systems — independent of
any particular programming language, paradigm, or source format. A code model can be populated from
any source (compiled JVM classes, source files, a TypeScript AST, a visual language, or anything
else), then enriched, validated, and compiled through a plugin pipeline. The JDK-backed modules are
the reference implementation, not the limit of the framework.

## Modules

| Module | Purpose |
|--------|---------|
| `codemodel-foundation` | Core `TypeDescriptor` / `TypeUsage` / `Trait` system, naming, marshalling |
| `expression-codemodel` | Expression AST nodes and extensible operator-precedence parser |
| `hierarchical-codemodel` | Type hierarchy: ancestors, descendants, assignability |
| `imperative-codemodel` | Statement AST nodes: Block, If, While, Return |
| `objectoriented-codemodel` | OOP traits: fields, methods, constructors, access modifiers |
| `jdk-codemodel` | JDK-backed implementation via reflection or javac |
| `dependency-injection` | Custom JSR-330 DI built on `jdk-codemodel` |
| `codemodel-framework` | Pipeline interfaces: Enricher, TypeChecker, Compiler, Completer |
| `codemodel-framework-builder` | Concrete `FrameworkBuilder` and `InternalFramework` |
| `jdk-annotation-discovery` | `AnnotationDiscovery` SPI and `@Discoverable` |
| `jdk-annotation-processor` | `javax.annotation.processing.Processor` driving the full pipeline |

## Requirements

- Java 25+
- Maven (wrapper included — no separate install needed)

## Using this Library

Add individual modules as dependencies. All modules share the same version:

```xml
<dependency>
    <groupId>build.codemodel</groupId>
    <artifactId>codemodel-foundation</artifactId>
    <version>VERSION</version>
</dependency>
```

Replace `VERSION` with the latest version shown in the Maven Central badge above.

## Building from Source

```bash
./mvnw clean install
```

To build a custom version:

```bash
./mvnw -Drevision=x.y.z-SNAPSHOT-my-name clean install
```

## Contributing

Code style is enforced by Checkstyle: no tabs, no star imports, final locals and parameters, braces
required on all blocks, no `assert` statements. Import order: third-party, standard Java, then
static. IntelliJ configuration is at `config/intellij/CodeStyle.xml`.

Commit messages follow [Conventional Commits](https://www.conventionalcommits.org/).

## License

Apache 2.0 — see [LICENSE](LICENSE)
