# MonthlyGoalManager Refocus Audit

## 1) Current Product Summary
- The app currently behaves as a structured monthly goal-management system: users create numeric goals, define target months, track check-ins, update progress percentages, and complete monthly reviews with final evaluations.
- Product behavior is strongly performance-oriented (completion %, key-goal prioritization, archived/completed states, satisfaction ratings, and month-end review workflows).
- Navigation and UI emphasize planning, measuring, and judging outcomes rather than low-pressure reflection.

## 2) Keep
### Technical foundations worth keeping
- Core stack choices are solid for a small offline-first Android app: Compose UI, Room persistence, Hilt DI, and DataStore preferences.
- Existing repository/viewmodel flow pipeline is usable for reactive UI and gradual refactor.
- Data export/import capability is practical user protection and should remain as a trust feature.

### Product concepts worth keeping
- Lightweight check-ins as “small progress moments” (not only final completion) are aligned with emotional safety.
- Monthly timeline framing can remain if reframed as “reflection period” instead of goal scorecard.
- Higher-level intention linking can remain only as optional context (not mandatory planning overhead).

### UI/UX elements worth keeping
- Month navigation and simple list-based home flow are understandable.
- Progress visualization components can be reused if language/colors are softened and performance semantics reduced.
- “Hide completed” preference can be repurposed into decluttering controls.

## 3) Remove
### Features inconsistent with the new philosophy
- “Key goal” framing and sorting; it introduces priority pressure and hierarchy anxiety.
- Completion-state logic that auto-flips at 100% and celebratory/failed polarity language.
- Month-end wizard requiring structured final scoring and strict review completion.
- Satisfaction star ratings and explicit achievement/challenge scoring fields that can feel evaluative.
- Archive/completion-heavy framing in higher-goal area when used as status judgment.

### Technical/UX dead weight
- Unreachable or mismatched routes (`monthlyReviewWizard/...`, `higherGoals/select`) causing navigation inconsistency.
- Duplicate/inconsistent dependency declarations (mixed direct Compose versions + BOM + duplicated Material3).
- Overloaded data model with many fields and entities for an MVP focused on emotional reassurance.
- Sample seed lists (`juneGoals`, `julyGoals`) embedded in production model file.

## 4) Postpone (Not MVP)
- Advanced monthly summary sharing and rich report formatting.
- Higher-goal management depth (icons, archive state toggles, rich editing) beyond lightweight optional grouping.
- Full action-step subsystem until core check-in habit quality is validated.
- Complex import conflict handling and broad settings surface beyond essential safety controls.

## 5) Technical Cleanup Suggestions
### Dependency cleanup
- Consolidate Compose deps around BOM only; remove explicit versioned duplicates.
- Keep one Material3 dependency version; remove duplicates.
- Move Navigation/Room/DataStore/Serialization versions into `libs.versions.toml` for consistency.

### Navigation issues
- Align route names between callers and graph definitions (`monthlyReview` vs `monthlyReviewWizard`, `higherGoals/select`).
- Remove temporary compatibility routes once canonical route scheme is set.
- Introduce typed route constants/sealed destinations to prevent string drift.

### State/data model simplification
- Replace strict goal-centric schema with minimal “ProgressEntry” model (title, optional context, note, timestamp, optional numeric delta).
- Make completion optional metadata, not central logic.
- Defer or remove `FinalCheckIn`, `ActionStep`, and heavy monthly review structures from active MVP flow.

### Architecture simplification
- Split giant `DataModels.kt` into domain models + viewmodel files.
- Reduce `GoalsViewModel` surface area by feature modules/use cases.
- Remove risky Room callback recursion pattern in DI module; avoid creating DB from inside its own creation callback.

## 6) Recommended New MVP Direction
Build a gentle “small progress journal” app: users add tiny forward moves (with optional numbers), see a calm timeline of evidence that they are progressing, and receive non-judgmental monthly reflections like “you moved more than you felt.” Keep interaction lightweight, optional, and forgiving—no hard failure states, no pressure metrics, no strict completion framing. The product should optimize emotional recovery and confidence restoration rather than productivity enforcement.
