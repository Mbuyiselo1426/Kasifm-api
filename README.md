# Kasie FM API

Spring Boot backend for the Kasie FM app. It exposes schedule data and
stream URLs over a simple REST API.

This repository is private and intended for internal project use.

## Endpoints

| Method | Path                  | Returns                                  |
|--------|-----------------------|-------------------------------------------|
| GET    | `/api/schedule`       | Current Johannesburg day plus active overnight carry-over  |
| GET    | `/api/schedule/{id}`  | A single show                             |
| PUT    | `/api/schedule/{id}`  | Update an existing show                   |
| GET    | `/api/stream-url`     | `{ streamUrl, streamUrlLite }`            |

## Run it — Option A: Docker (recommended)

```bash
docker compose up --build
```

(Older Docker installs may need the hyphenated `docker-compose up --build`
instead — both were tested working during development.)

Then open http://localhost:8080/api/schedule to verify the API is running.

To stop: `docker compose down` (add `-v` to also wipe the database volume).

## Run it — Option B: quick local check without Docker (H2 in-memory DB)

Requires Java 17 and Maven installed locally.

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Good for a fast sanity check while writing code.

## Editing the schedule

The verified 56-row weekly lineup lives in `DataSeeder.java`. Seeding remains
disabled by default; explicitly enable `app.seed.enabled=true` to populate an
empty database. Existing databases are never reseeded, replaced, or backfilled.
Names and all eight daily slots match the project owner’s day-by-day verification
of https://kasiefm971.co.za/shows.html on 2026-09-13. Presenters and descriptions
remain null because they have not been verified. After that, you can update a show directly with
`PUT /api/schedule/{id}` and the change stays in the database across
restarts.

## Weekly schedule contract

Each `Show` is one weekly slot. `dayOfWeek` is the Johannesburg day on which
it **starts**, serialized as an uppercase name (`MONDAY` through `SUNDAY`).
Multiple days require separate rows; the opt-in seeder provides all 56 verified
slots, with 21:00–00:00 ending at the next day boundary.

`GET /api/schedule` returns the current `Africa/Johannesburg` day's rows plus
any still-active overnight rows from the previous day. The entire response
is sorted by `startTime`, then ID, ascending (so a previous-night row may
appear last). At 01:00 Saturday, a FRIDAY 22:00–02:00 row is included with
`dayOfWeek: "FRIDAY"`. At 02:00 it disappears. Sunday-to-Monday wraps normally.
There is no all-week query parameter in this change; retrieval by ID remains
available for editing any day's row.

Every response object includes boolean **`isCurrent`**, never `current`.
All selection and retrieval use one timestamp per list response. Starts are
inclusive and ends exclusive. Wrong-day and undated legacy rows are never
current. At most one row is marked true; overlapping active slots use the
lowest ID as the winner, regardless of list order. Clients must use the
marker rather than item 0. Gaps have all false flags; no rows returns `[]`.
Schedule gaps do not imply the radio stream is offline.

`PUT /api/schedule/{id}` now requires `dayOfWeek` with the existing required
name/start/end fields. Missing, null, invalid, lowercase, and numeric days
return HTTP 400. Earlier end times are valid overnight shows; equal times
return HTTP 400 (not a 24-hour show). Legacy equal-time rows remain inactive.
Detail and update responses retain full-schedule overlap selection; a show
outside the current day/carry-over has `isCurrent: false`.

```json
{"name":"Example night show","dayOfWeek":"FRIDAY","startTime":"22:00","endTime":"02:00"}
```

## Existing PostgreSQL / Neon data

`day_of_week` is a string enum (`varchar(9)`), not an ordinal. The column is
intentionally nullable during migration because existing rows have no known
day. New PUT requests require a day, but legacy rows stay intact and are
excluded from daily retrieval until assigned. No automatic day assignment,
row deletion, duplication, or production SQL execution is performed.

With the existing `spring.jpa.hibernate.ddl-auto=update`, Hibernate is expected
to add the nullable column and may add an enum check constraint, depending on
the dialect/schema state. It cannot infer broadcast days or backfill them.
For a controlled production rollout, review and apply the additive SQL in
`docs/weekly-schedule-migration.sql` before deployment instead. This file is
manual documentation, not a startup migration. After assigning every legacy
row its reviewed day, the file also documents the separate NOT NULL step.
The JPA column remains nullable for this transition; enforce NOT NULL in the
entity in a later migration once all installations are backfilled.

Production requires manual day assignments after this change. Until then,
the API may return an empty schedule even though old rows still exist.
Back up existing data before a planned migration. Existing PUT callers must
include the new required day; Android's extra-field-tolerant reads need no
change. No changes to streaming or database connection settings are required.

## Verification

Run `mvn test` and `mvn -DskipTests package`. Tests use the existing isolated
H2 test profile, never Neon. Coverage includes weekly filtering, ordering,
Johannesburg midnight, overnight/week wrap, overlap selection, JSON day and
marker mapping, update validation, persistence, and legacy undated rows.

## Manual replacement with the verified lineup

`docs/verified-weekly-schedule-data.sql` is a manual PostgreSQL/Neon script,
not a Spring Boot startup migration. It locks `shows`, refuses replacement
if any table references it via a foreign key, creates the data backup
`public.shows_backup_before_verified_20260913`, adds `day_of_week` if missing,
then deletes the old rows and inserts the 56 verified weekly slots in one
transaction. Checks reject incorrect totals, day counts, duplicate slots,
times, or unverified metadata before commit. IDs are generated normally;
sequences are never reset. The backup preserves original IDs and data, but
is a data copy, not a replacement for database schema/constraint backups.

The script intentionally fails if that backup table already exists. Review
any earlier execution before a retry; do not delete the backup just to rerun.
Replacement assigns new show IDs, so review clients that retain old IDs.
Schedule reads/edits can wait briefly while the transaction holds its lock.

Later, run the entire file in the Neon SQL Editor for the intended database,
or use an already configured secure libpq service (no credentials in the repo):

```bash
psql 'service=kasifm-neon' -X -v ON_ERROR_STOP=1 -f docs/verified-weekly-schedule-data.sql
```

The `kasifm-neon` service must be configured separately with the correct
connection details. No SQL or production connection was performed while
preparing this dataset. This replacement assigns every row a day, so the
individual legacy-day backfill above is unnecessary if this script is used.
It does not itself enforce NOT NULL; that remains a separate migration step.
