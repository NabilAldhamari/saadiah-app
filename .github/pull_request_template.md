## What changed

## Why

## Checklist

- [ ] Tests were written before the implementation
- [ ] `./gradlew check` is green locally
- [ ] All 6 guard scripts pass (`scripts/check-*.sh`)
- [ ] Play Store bundle builds successfully (`./gradlew :android:app:bundleRelease`)
- [ ] No new comment explains *what* the code does
- [ ] No new dependency introduces network access, an annotation processor, or runtime reflection
- [ ] Performance budgets are unaffected, or the change is a measured improvement
- [ ] Any new UI has RTL and font-scale-2.0 screenshot coverage
