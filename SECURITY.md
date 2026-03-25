# Security Policy

## Supported Versions

The following versions of `build.codemodel` are currently supported with security updates:

| Version | Supported          |
|---------|--------------------|
| 0.19.x  | :white_check_mark: |
| < 0.19  | :x:                |

## Reporting a Vulnerability

This is an Workday sponsored project hosted on GitHub.

To report a security vulnerability, please **do not** open a public GitHub issue. Instead:

1. Navigate to the [Security Advisories](https://https://github.com/workday/codemodel.build/security/advisories) page for
   this repository and click **Report a vulnerability**.

2. Alternatively, contact the project maintainers directly by emailing the developers.

### What to Include

Please include as much of the following information as possible to help us understand and reproduce the issue:

- The type of vulnerability (e.g. dependency with known CVE, insecure API usage, credential leak)
- The affected module(s) and version(s)
- Step-by-step instructions to reproduce the issue
- Any proof-of-concept code or test cases
- The potential impact and severity assessment

### Response Timeline

| Stage                     | Target        |
|---------------------------|---------------|
| Acknowledgement           | 2 business days |
| Severity assessment       | 5 business days |
| Fix or mitigation plan    | 15 business days |

## Security Considerations

`build.codemodel` is a **library** — it does not handle network connections, user authentication, or persistent
storage directly. Security considerations are primarily limited to:

- **Dependency vulnerabilities** — transitive dependencies should be kept up to date
- **Annotation processor execution** — the JDK annotation processor runs at compile time inside the Java compiler;
  only trusted annotation processor configurations should be used
- **Serialisation** — the marshalling module supports object serialisation; only deserialise data from trusted sources
