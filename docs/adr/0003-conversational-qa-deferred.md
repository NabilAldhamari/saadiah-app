# 3. Local conversational Q&A is deferred, not scoped

**Status:** accepted

## Context

A floating "Ask" entry point was added ahead of a plan and shipped as a disabled placeholder,
visible on every screen. It was never in `docs/DESIGN.md`, and the implementation plan's
"Deliberately excluded" list already ruled out an AI assistant for this version.

The long-term intent is a small, fully on-device language model answering general Islamic
questions, with no network call of any kind. That is a legitimate future direction, but it
carries costs this codebase's current budgets don't accommodate: a usable on-device model is
tens to hundreds of times the entire app's size ceiling (`docs/DESIGN.md` performance budgets),
and a model that can produce fabricated fiqh content needs its own accuracy, disclosure, and
review process before it is defensible to ship in an app whose entire premise is trustworthy
religious content.

## Decision

Remove the Ask entry point and its screen from the shipped app. Do not build toward it further
until a dedicated design pass covers: model size against the app's size budget, on-device
inference cost against the performance budgets, a hallucination/accuracy review process
equivalent to the one required for adhkār content, and a clear in-UI disclosure distinguishing
generated answers from cited sources.

## Consequences

The idea is preserved here rather than as live, non-functional UI. Nothing is lost — it can be
un-deferred by opening a new ADR once the open questions above have answers.
