# codemodel.build

## Codebase Overview

`codemodel.build` is a language-agnostic Java code model framework (Workday, Inc.) that provides a structured, serializable representation of software systems. A `CodeModel` can be populated from compiled classes (via reflection) or `.java` source files (via javac), then enriched, validated, and compiled through a plugin pipeline. It is the foundation for annotation processors and code generation tools.

**Stack:** Java 25 (preview features required), Maven multi-module, Jakarta Inject, custom marshalling framework, Pratt-parser expression engine, JSR-330 DI implementation.

**Structure:**
- `codemodel-foundation` — core `TypeDescriptor`/`TypeUsage`/`Trait` system + naming + marshalling
- `expression-codemodel` — expression AST nodes + extensible operator-precedence parser
- `hierarchical-codemodel` — type hierarchy (ancestors, descendants, assignability)
- `imperative-codemodel` — statement AST nodes (Block, If, While, Return)
- `objectoriented-codemodel` — OOP traits (fields, methods, constructors, access modifiers)
- `jdk-codemodel` — JDK-backed impl via reflection (`JDKCodeModel`) or javac (`JdkInitializer`)
- `dependency-injection` — custom JSR-330 DI built on `jdk-codemodel`
- `codemodel-framework` — pipeline interfaces (Enricher, TypeChecker, Compiler, Completer)
- `codemodel-framework-builder` — concrete `FrameworkBuilder` + `InternalFramework`
- `jdk-annotation-discovery` — `AnnotationDiscovery` SPI + `@Discoverable`
- `jdk-annotation-processor` — `javax.annotation.processing.Processor` driving the full pipeline

For detailed architecture, module-by-module analysis, data flows, and navigation guide, see [docs/CODEBASE_MAP.md](docs/CODEBASE_MAP.md).
