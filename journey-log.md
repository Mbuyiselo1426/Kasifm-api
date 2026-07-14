### Session 2 — 3 July 2026
**Did:**
- Ran the API locally with H2 first (fast sanity check) — confirmed JSON output
- Installed Docker, fixed a group-permission issue with `newgrp docker`
- Ran `docker compose up --build` — full build took ~7 minutes first time (Maven deps + image pulls)
- Confirmed real Postgres connection in logs, hit /api/schedule, got real JSON back
- Phase 1 officially complete — Week 4 checkpoint passed

**Learned:**
- Image vs container distinction (Dockerfile is the recipe, container is the running instance)
- Why docker-compose's `DB_HOST: db` works — Docker's internal DNS resolves service names
- What Load Balancers and CloudFront actually do and why they matter at scale
- Read Hibernate SQL logs to trace a request end-to-end through the whole stack

**Next session:** Start Phase 2 — Retrofit setup in the Android app


### Session 3 — 8 July 2026
**Did:**
- Discovered `/api/stream-url` was already implemented on the backend but
  never actually wired into the Android app — both `MainActivity` and
  `RadioService` independently hardcoded the same SomaFM placeholder URL.
- Found the real underlying bug: `MainActivity` and `RadioService` each
  built their own separate `ExoPlayer`. The foreground service existed in
  code but was never bound to the UI, so audio playback was tied to the
  Activity's lifecycle instead of the service's — it died whenever the
  Activity did.
- Refactored to a bound + started foreground service pattern:
  `RadioService` now owns the only `ExoPlayer`, exposes
  `play()`/`pause()`/`isPlaying()` through a `Binder`, and reports state
  changes through a `PlaybackStateListener` callback. `MainActivity` starts
  and binds to it in `onStart()`, unbinds (without stopping it) in
  `onStop()`.
- Added the missing `FOREGROUND_SERVICE` permission to
  `AndroidManifest.xml` — without it, `startForeground()` would have thrown
  a `SecurityException` on API 28+, just hadn't been exercised yet.
- Confirmed on the emulator: audio now survives backgrounding.
- Found a follow-on bug: returning to the app after backgrounding didn't
  update the "LIVE" badge/button even though audio was still genuinely
  playing. Root cause: the `PlaybackStateListener` only fires on a *new*
  state transition — if the app is already `LIVE` when you reconnect,
  there's no transition to trigger it.
- Added `RadioService.getCurrentState()` and called it immediately in
  `onServiceConnected()` so the UI syncs to whatever's actually happening on
  reconnect, instead of waiting passively for the next change.
- Added a temporary `Log.d("KasieDebug", ...)` line to confirm the rebind
  callback fires and what state it reports — still being verified on
  physical hardware.

**Learned:**
- The difference between a Service that's merely *declared* and one that's
  actually *reachable* — `exported`/`enabled` in the manifest doesn't mean
  anything is bound or started.
- Bound vs. started services: started keeps something alive independent of
  any Activity; bound lets an Activity call into it and get results. Real
  background audio needs both, not either alone.
- Why a stale Logcat error (`Channel is unrecoverably broken`,
  `No task ids found`) can be pure aftermath noise from a real crash
  elsewhere, and why the actual `FATAL EXCEPTION` / `AndroidRuntime` block
  is the only thing worth chasing — everything else is downstream cleanup.
- Codec2 `We have a failed config` errors on an emulator/Appetize.io session
  are a known audio-codec limitation of those environments, not necessarily
  a bug in app code — confirmed by testing on physical hardware where audio
  played fine despite similar log lines.

**Interview-ready line:**
> "I found a foreground service that existed in code but wasn't actually
> reachable — the Activity was running its own independent player instead.
> I refactored it to a bound-and-started service pattern where the service
> owns the player and reports state back through a callback, which is what
> let playback survive backgrounding. When a related state-sync edge case
> showed up on reconnect, I added a debug log and traced it to the callback
> only firing on state *transitions*, not on demand — rather than guessing
> at fixes blind."

**Blocked on / next time:**
- Confirm the `getCurrentState()` fix works correctly on physical hardware.

---

### Session 4 — 9 July 2026
**Did:**
- Tested on a real physical device (not emulator) for the first time —
  confirmed audio genuinely plays and survives backgrounding.
- Fixed two UI polish issues on real-device testing: explicitly centered
  the "LIVE" badge (`layout_gravity="center_horizontal"`) and added a thin
  yellow accent divider strip under the black toolbar for visual
  separation, instead of a generic Material shadow.
- Confirmed a real discrepancy worth remembering: static XML text
  (`"NOW ON AIR"`) is overwritten at runtime by `updateUiForState()` in
  `MainActivity` (`"LIVE"`) — screenshots of the running app won't match
  the raw layout file's default text, which isn't a bug, just something to
  account for when comparing a screenshot to source.

**Learned:**
- Real-device testing surfaces layout issues emulators can hide or distort
  (status bar height, exact color rendering, real touch feedback).

**Blocked on / next time:**
- Play/pause button corner-radius styling — requested, not yet done.

---

### Session 5 — 9–14 July 2026 — the Kasie FM reality check
**Did:**
- Researched the real Kasie FM 97.1 — a genuine, currently-operating
  community station broadcasting from Katlehong to Thokoza and the wider
  Ekurhuleni area, founded 1997 (`kasiefm971.co.za`).
- Confirmed real presenters (Daniel Sihlangu "Zero 2 Six", Vusi Gcabashe,
  Phly Guy, Ntombi Madonsela, Boipelo Lesenya) and one exact match to the
  app's existing seed data: the real `#HomeDrive` show genuinely airs
  15:00–18:00, presented by Napo.
- Found the real schedule is far richer than the app's four-show model, and
  varies by day of week — a genuine schema gap (`Show` has no
  day-of-week/recurrence concept yet).
- Found the real station's actual live stream URL. Decided **not** to wire
  it in, or use any further real schedule/presenter data, until the station
  has actually been contacted and agreed — this app already uses their real
  logo, which is fine for a private demo but not for anything public.
- Drafted (not yet sent) two outreach email variants to Kasie FM proposing
  a demo and asking permission, rather than presenting the app as already
  theirs.
- Built a facilitator-facing architecture & progress report (Word doc)
  covering the project vision, architecture, phase-by-phase status, the
  RadioService debugging story, and this authorization situation — honestly,
  not hidden.
- Attempted AWS Free Tier signup — blocked, no funds available for the
  identity-verification card hold AWS requires even for free-tier usage.
  Decided to continue AWS learning conceptually and work on
  zero-cost backend/Android polish in the meantime, rather than stall the
  project waiting on it.
- Cleaned up both READMEs — the Android one had leftover unrelated content
  from a different WeThinkCode_ assignment (a robots-server project) mixed
  into it; rewrote it to reflect the actual current architecture, and added
  the Kasie FM authorization status to both READMEs so it's visible to
  anyone (including a facilitator) reading the repos, not just buried in
  this log.

**Learned:**
- AWS requires a valid card even for the Free Tier — it's identity
  verification (a small, usually-refunded hold), not upfront payment, but a
  card with zero available balance can still fail that hold.
- The difference between "inspired by a real thing" and "for a real thing"
  matters a lot in practice — same code, very different obligations around
  branding, data accuracy, and who needs to say yes before what ships
  publicly.

**Interview-ready line:**
> "Partway through, I realized my project's namesake was a real, operating
> community station, not just a name I'd picked. I treated that as a
> product decision, not just a technical one — I kept the prototype
> private, didn't wire in anything beyond what's already public information
> I'd verified, and drafted outreach to the station instead of assuming I
> could use their identity. That's the kind of judgment call that doesn't
> show up in the diff, but it's the one I'd want an employer to know I make
> by default."

**Blocked on / next time:**
- Send outreach email to Kasie FM (drafted, not yet sent).
- Finish the play/pause button corner-radius styling.
- Once AWS card situation is resolved: create Free Tier account, begin
  Step 1 (VPC/IAM/regions) hands-on.
