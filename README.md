# Kasie FM API

Spring Boot backend for the Kasie FM app. It exposes schedule data and
stream URLs over a simple REST API.

This repository is private and intended for internal project use.

## Endpoints

| Method | Path                  | Returns                                  |
|--------|-----------------------|-------------------------------------------|
| GET    | `/api/schedule`       | List of all shows, ordered by start time  |
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

Initial seed data lives in `DataSeeder.java` and only loads when the
database is empty. After that, you can update a show directly with
`PUT /api/schedule/{id}` and the change stays in the database across
restarts.

## Current-show contract

`GET /api/schedule` returns shows sorted by `startTime` ascending, then ID
ascending. Every object includes the boolean JSON property **`isCurrent`**
(not `current`). Clients select by this marker, never by list position.
At most one show is marked true; gaps return all false and an empty schedule
returns `[]`. A schedule gap does not mean the audio stream is off air.

All flags use one clock instant per response converted to `Africa/Johannesburg`.
Starts are inclusive and ends exclusive: at 09:00 a 06:00–09:00 show is no
longer current. The existing model has no date/day-of-week field, so all rows
repeat daily, including weekends; this does not claim to model the station's
actual weekend lineup. Rows crossing midnight match before their end or after
their start. Equal start/end means zero duration, never a 24-hour show.
The existing PUT validation still only accepts end times later than starts;
overnight support here applies to rows already stored in the database.

If stored slots overlap, the matching show with the lowest database ID wins,
independently of repository/list order. Detail and update responses use the
same full-schedule selection rule. Flags describe the response instant, not
a permanent show property, and are not persisted.

Example (other show fields omitted):

```json
[{"id": 1, "startTime": "06:00", "endTime": "09:00", "isCurrent": false},
 {"id": 2, "startTime": "09:00", "endTime": "12:00", "isCurrent": true}]
```

Verify with `mvn test`. Fixed-clock tests cover station timezone, boundaries,
overnight slots, gaps, overlap/order independence, and the exact JSON marker.
