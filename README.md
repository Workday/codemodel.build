# The Code Model Project

This project and accompanying framework aims to support the language agnostic representation of
models represented by application designs and code (aka: Code Models), typically for potentially modular applications, 
without any particular concern for the origins of the said designs, their types, paradigms or underlying programming 
languages.

## Philosophy

1. **No New Programming Languages** - this project aims to avoid defining and/or introducing any new programming
   languages for the purposes of representing code models.

2. **No New Terminology or Concepts** - this project aims to avoid defining and/or introducing new terminology.  
   Instead, this project aims to adopt existing, commonly used and/or industry standard terms and concepts where
   possible, thus allowing developers to use standard Internet or otherwise based resources for learning. When terms 
   differ in meaning, or have a more specific meaning for this project, such circumstances will be clearly identified.

3. **Always Modular** - the project aims to be completely and purely modular to support decomposing complex applications
   into separate modules, that may be independently developed, tested, reused and deployed. This means under no
   circumstances is non-modular code ever permitted.

4. **Interfaces First** - this project aims to provide an "interface first" approach for designing and developing
   applications, whereby those interfaces may be used at design and development time, in an IDE in conjunction with
   out-of-the-box compilers, to extract the required type systems, that can then be used to define storage, security,
   access control, privacy, integration and other models associated with an application, with the necessary language and
   framework bindings, across relevant development and deployment stacks.  This means that things like "attributes" are
   defined as part of an "interface" and not an additional bespoke external/alternative format. The
   "interfaces first (and only)" approach ensures symmetric consistency of application design and significantly reduces
   development effort.

5. **Embeddable** - this project aims to be embeddable in existing Java projects to support a). high-velocity
   deployment to production, b). direct and tight integration when required (ie: for access control), c). promote
   development efforts across teams, d). accelerated access to existing customer and their deployments.

## Values

This project values:

* Symmetry and Correctness over Completeness - an incomplete feature that allows symmetry and correctness to be
  achieved is more important than a complete feature that causes asymmetry or introduces incorrectness/ambiguous
  semantics. It's not ok to add a feature without first considering the symmetry of the feature, and it's correctness.

* Coding style. It's highly opinionated and critically important. Developers are expected to match the existing code
  *and* use the automated IDE (IntelliJ) coding style that's provided in the config/intellij folder. If the coding style
  is not for you, then the project probably isn't either. Changing coding styles is a massive invasive effort and in
  most cases a waste of development velocity.

* Null-free Code. Within the code base, all efforts should be made to avoid and prevent the use of **null**. To
  achieve this, **Optional** should always be used to express when a type value may not be present instead of relying on
  possibly ambiguous null-ness of reference types.

## Required Experience

This project requires and makes extensive use Meta-Programming concepts, Java Compiler technology and
Functional-Programming Style. While not all of this is required to model applications using the framework, it is
very helpful to understand these concepts.

## Development Setup

* This project is written using [Java 25](https://www.azul.com/downloads/#downloads-table-zulu) and
  built using [Apache Maven](https://maven.apache.org).  *Both* must be configured before proceeding.

### Apache Maven Configuration

* Building and Installing to the Local Repository (using the Apache Maven Wrapper)
  `./mvnw clean install`

* Building and Installing a Custom Version to the Local Repository (use the Apache Maven Wrapper)
  `./mvnw -Drevision=x.y.x-SNAPSHOT-my-name clean install`

* Building and Deploying a Custom Version to the Workday Experimental Repository (use the Apache Maven Wrapper)
  `./mvnw -Drevision=x.y.x-SNAPSHOT-my-name clean deploy`


### Coding Style

#### Commit Messages:

This project uses [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) style for commit messages in
an attempt to communicate the impact of commits according to Semantic Versioning.

#### Indentation:

* This project uses at least 4 space character indentation (not tabs). This is to allow for folks
  with visual-impairment or such challenges to work efficiently without eyestrain as it's been found that 2 space
  character indention leads to higher chances of visual stress.

* This project uses Google Coding style (with exception to the 4 space indentation rule).

#### Testing:

* Tests are written using *JUnit 5* testing framework, with assertions being expressed using *AspectJ*. All other
  frameworks should be avoided, because they are seemingly unmaintained, non-modular or don't support of modular
  testing.
  For example, Google Truth is non-modular (only built on Java 8) and requires Junit 4 as a hard dependency. Hamcrest is
  virtually unmaintained (and hasn't been updated for many years).

* Test methods must begin with either 'should' or 'shouldNot'.

* Test methods should include javadoc to aid in the understanding of the intent of the tests.

