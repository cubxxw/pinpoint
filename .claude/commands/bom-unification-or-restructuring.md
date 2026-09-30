---
name: bom-unification-or-restructuring
description: Workflow command scaffold for bom-unification-or-restructuring in pinpoint.
allowed_tools: ["Bash", "Read", "Write", "Grep", "Glob"]
---

# /bom-unification-or-restructuring

Use this workflow when working on **bom-unification-or-restructuring** in `pinpoint`.

## Goal

Unify or restructure Bill of Materials (BOM) files, consolidating dependencies and updating module references.

## Common Files

- `agent-module/agent-testweb/bom/pom.xml`
- `agent-module/plugins-bom/pom.xml`
- `agent-module/plugins-it/pom.xml`
- `agent-module/plugins/pom.xml`
- `agent-module/pom.xml`

## Suggested Sequence

1. Understand the current state and failure mode before editing.
2. Make the smallest coherent change that satisfies the workflow goal.
3. Run the most relevant verification for touched files.
4. Summarize what changed and what still needs review.

## Typical Commit Signals

- Identify BOM files to unify or restructure.
- Move or merge dependency declarations into a central BOM.
- Update module pom.xml files to reference the new BOM.
- Remove or deprecate old BOM files.
- Commit all related changes together.

## Notes

- Treat this as a scaffold, not a hard-coded script.
- Update the command if the workflow evolves materially.