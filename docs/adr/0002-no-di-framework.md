# 2. No dependency injection framework

**Status:** accepted

## Context

Hilt is the default choice for Android applications of this shape. It brings an annotation
processor, which adds a build step to every module and a measurable cost to incremental builds.

## Decision

Dependencies are constructed by hand in a single `AppContainer` and passed through constructors.
No DI framework, no annotation processors anywhere in the build — this also rules out Room and
codegen-based JSON libraries.

## Consequences

Builds stay fast, which matters because the TDD loop's cost is dominated by build time. The graph
is explicit and readable. The cost is that adding a dependency means editing one file by hand;
at this application's size that is a feature, because it makes the graph's growth visible.

Revisit only if the container exceeds roughly 300 lines or the object graph acquires scoping needs
beyond singleton and per-screen.
