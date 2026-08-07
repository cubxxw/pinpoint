---
name: dependency-upgrade-across-multiple-modules
description: Workflow command scaffold for dependency-upgrade-across-multiple-modules in pinpoint.
allowed_tools: ["Bash", "Read", "Write", "Grep", "Glob"]
---

# /dependency-upgrade-across-multiple-modules

Use this workflow when working on **dependency-upgrade-across-multiple-modules** in `pinpoint`.

## Goal

Upgrade a dependency version (e.g., testcontainers, springdoc-openapi-ui) across many modules and plugins, updating relevant pom.xml files and sometimes related test code.

## Common Files

- `**/pom.xml`
- `**/src/test/java/**/*.java`

## Suggested Sequence

1. Understand the current state and failure mode before editing.
2. Make the smallest coherent change that satisfies the workflow goal.
3. Run the most relevant verification for touched files.
4. Summarize what changed and what still needs review.

## Typical Commit Signals

- Identify the dependency and new version.
- Update the dependency version in all affected pom.xml files.
- Update any related test code or configuration if needed.
- Commit all changes together with a bump message.

## Notes

- Treat this as a scaffold, not a hard-coded script.
- Update the command if the workflow evolves materially.