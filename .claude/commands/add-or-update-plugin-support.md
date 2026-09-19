---
name: add-or-update-plugin-support
description: Workflow command scaffold for add-or-update-plugin-support in pinpoint.
allowed_tools: ["Bash", "Read", "Write", "Grep", "Glob"]
---

# /add-or-update-plugin-support

Use this workflow when working on **add-or-update-plugin-support** in `pinpoint`.

## Goal

Add support for a new framework/plugin version (e.g., Spring 7/Spring Boot 4), including new plugin modules, configuration, and test coverage.

## Common Files

- `agent-module/plugins-*/pom.xml`
- `agent-module/agent-testweb/*-plugin-testweb/pom.xml`
- `agent-module/agent-testweb/*-plugin-testweb/src/main/java/**/*.java`
- `agent-module/agent-testweb/*-plugin-testweb/src/main/resources/**`
- `agent-module/plugins-it/**/pom.xml`
- `agent-module/plugins-it/**/src/test/java/**/*.java`

## Suggested Sequence

1. Understand the current state and failure mode before editing.
2. Make the smallest coherent change that satisfies the workflow goal.
3. Run the most relevant verification for touched files.
4. Summarize what changed and what still needs review.

## Typical Commit Signals

- Create or update plugin module directories and pom.xml.
- Add new implementation and configuration files for the new version.
- Add or update test classes for the new version.
- Update root or aggregator pom.xml to include the new modules.
- Update documentation if needed.

## Notes

- Treat this as a scaffold, not a hard-coded script.
- Update the command if the workflow evolves materially.