---
name: gradle-build-verification
description: Use this skill after generating or modifying any Kotlin, Gradle, or resource files in the LandStack Android Studio project, and before telling the user the project is ready, builds cleanly, or has zero errors. Runs an actual Gradle build and reports real pass/fail results instead of assuming the code compiles.
---

# Gradle Build Verification Skill

## Goal
The project's explicit requirement is that it builds and runs with zero errors on the first Gradle sync. Never state that the project builds cleanly without having actually run a build in this session.

## Instructions
1. From the project root (the folder containing `gradlew` / `gradlew.bat`), run a debug build using your command-execution tool:
   - Windows: `gradlew.bat assembleDebug`
   - macOS/Linux: `./gradlew assembleDebug`
2. Read the actual output. If it fails, identify the specific file(s) and line(s) named in the error, fix them, and re-run the same command. Repeat until it passes.
3. Only after a real pass, tell the user the project builds cleanly -- and say so specifically (e.g. "ran a debug build just now, it passed"), not as a general assumption.
4. If `gradlew`/`gradlew.bat` is missing or the wrapper itself is broken, say that plainly rather than reporting a false pass.
5. For UI-only or resource-only changes, a build pass is still required -- Compose and resource errors often only surface at compile time.

## Constraints
- Never report "should build fine" as a substitute for actually running the build.
- Never skip this skill for a "small" change -- small changes break builds too.
- Never mark a task done in the same turn a build was started without waiting for and reading its actual result.
