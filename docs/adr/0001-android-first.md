# 1. Ship Android first

**Status:** accepted

## Context

The product targets Android and iOS. iOS cannot be compiled or tested on Linux CI runners, so
including it from the start means either a permanently red pipeline or a macOS-gated one before a
single feature exists.

## Decision

Milestone 1 is Android only. The shared logic lives in `core/*` as Kotlin Multiplatform modules
with the JVM and Android targets enabled. Adding `iosArm64()` in Milestone 2 is a one-line change
per module rather than a restructuring.

## Consequences

CI is fully green on free Linux runners throughout Milestone 1. iOS work is additive. The cost is
that no `expect`/`actual` boundary is exercised against a real Apple implementation until
Milestone 2 — mitigated by keeping platform-specific surface confined to `core/data` and the
notification layer.
