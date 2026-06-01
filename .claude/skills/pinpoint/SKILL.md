```markdown
# pinpoint Development Patterns

> Auto-generated skill from repository analysis

## Overview
This skill provides guidance on contributing to the `pinpoint` Java codebase. It covers coding conventions, common development workflows (such as dependency upgrades, plugin support, and BOM unification), and testing patterns. Whether you're updating dependencies, adding new plugin support, or restructuring BOM files, this guide helps ensure consistency and efficiency across the project.

## Coding Conventions

### File Naming
- **Java files:** Use PascalCase for class and file names.
  - Example: `MyClass.java`, `PluginSupport.java`

### Import Style
- **Relative imports** are preferred within the codebase.
  ```java
  import com.example.module.MyClass;
  ```

### Export Style
- **Named exports** (public classes and methods).
  ```java
  public class PluginManager {
      public void registerPlugin(Plugin plugin) { ... }
  }
  ```

### Commit Patterns
- **Freeform commit messages** with an average length of ~60 characters.
- No strict prefixing, but clear and descriptive messages are encouraged.
  - Example: `Upgrade testcontainers version in all modules`

## Workflows

### Dependency Upgrade Across Multiple Modules
**Trigger:** When a shared dependency version needs updating across all relevant modules/plugins.  
**Command:** `/upgrade-dependency`

1. Identify the dependency and the new version to be used.
2. Update the dependency version in all affected `pom.xml` files.
   - Example:
     ```xml
     <dependency>
       <groupId>org.testcontainers</groupId>
       <artifactId>testcontainers</artifactId>
       <version>1.18.0</version> <!-- Updated version -->
     </dependency>
     ```
3. Update any related test code or configuration if necessary.
4. Commit all changes together with a clear bump message.
   - Example commit: `Bump testcontainers to 1.18.0 in all modules`

**Files involved:**
- `**/pom.xml`
- `**/src/test/java/**/*.java`

---

### Add or Update Plugin Support
**Trigger:** When adding support for a new framework/plugin version (e.g., Spring 7/Spring Boot 4).  
**Command:** `/add-plugin-support`

1. Create or update plugin module directories and their `pom.xml`.
2. Add new implementation and configuration files for the new version.
   - Example: `agent-module/plugins-spring7/pom.xml`
3. Add or update test classes for the new version.
   - Example: `agent-module/agent-testweb/spring7-plugin-testweb/src/test/java/...`
4. Update the root or aggregator `pom.xml` to include the new modules.
5. Update documentation if needed.
6. Commit all related changes with a descriptive message.

**Files involved:**
- `agent-module/plugins-*/pom.xml`
- `agent-module/agent-testweb/*-plugin-testweb/pom.xml`
- `agent-module/agent-testweb/*-plugin-testweb/src/main/java/**/*.java`
- `agent-module/agent-testweb/*-plugin-testweb/src/main/resources/**`
- `agent-module/plugins-it/**/pom.xml`
- `agent-module/plugins-it/**/src/test/java/**/*.java`
- `pom.xml`

---

### BOM Unification or Restructuring
**Trigger:** When centralizing or reorganizing dependency management across modules.  
**Command:** `/unify-bom`

1. Identify BOM files to unify or restructure.
2. Move or merge dependency declarations into a central BOM file.
   - Example: Consolidate dependencies into `agent-module/plugins-bom/pom.xml`
3. Update module `pom.xml` files to reference the new BOM.
   - Example:
     ```xml
     <dependencyManagement>
       <dependencies>
         <dependency>
           <groupId>com.example</groupId>
           <artifactId>plugins-bom</artifactId>
           <version>${project.version}</version>
           <type>pom</type>
           <scope>import</scope>
         </dependency>
       </dependencies>
     </dependencyManagement>
     ```
4. Remove or deprecate old BOM files.
5. Commit all related changes together.

**Files involved:**
- `agent-module/agent-testweb/bom/pom.xml`
- `agent-module/plugins-bom/pom.xml`
- `agent-module/plugins-it/pom.xml`
- `agent-module/plugins/pom.xml`
- `agent-module/pom.xml`

## Testing Patterns

- **Test framework:** Not explicitly detected; likely uses JUnit or similar.
- **Test file pattern:** Java test files are located under `src/test/java/` with standard naming conventions.
- **TypeScript test files:** Use the `*.test.ts` pattern (if present).
  - Example: `MyFeature.test.ts`

**Example Java test class:**
```java
public class PluginManagerTest {
    @Test
    public void shouldRegisterPlugin() {
        // test logic here
    }
}
```

## Commands

| Command               | Purpose                                                      |
|-----------------------|--------------------------------------------------------------|
| /upgrade-dependency   | Upgrade a dependency version across all modules/plugins      |
| /add-plugin-support   | Add or update support for a new plugin or framework version  |
| /unify-bom            | Unify or restructure BOM files for centralized dependencies  |
```
