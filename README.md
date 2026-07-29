# Kasie FM API

Spring Boot backend for the Kasie FM Android app. Serves the show schedule
and stream URL that used to be hardcoded in `MainActivity.java`.

**Live deployment:** `https://kasifm-api.onrender.com` (Render free tier —
sleeps after inactivity, ~30–60s cold start on first request after idle).

**CI:** GitHub Actions builds and deploys automatically on every push to
main.

> **Status note:** this is a student prototype inspired by the real Kasie FM
> 97.1 community station. It is **not** the station's official backend.
> Kasie FM has been contacted and has agreed to this project using their
> real schedule, presenter names, and branding — see the project journey
> log for the authorization history.

## Endpoints

| Method | Path                  | Returns                                  |
|--------|-----------------------|-------------------------------------------|
| GET    | `/api/schedule`       | List of all shows, ordered by start time  |
| GET    | `/api/schedule/{id}`  | A single show                             |
| GET    | `/api/stream-url`     | `{ streamUrl, streamUrlLite }`            |

## Run it — Option A: Docker (recommended)

```bash
docker compose up --build
```

(Older Docker installs may need the hyphenated `docker-compose up --build`
instead — both were tested working during development.)

Then open http://localhost:8080/api/schedule — you should see JSON with
the full weekday (Mon–Fri) show lineup.

To stop: `docker compose down` (add `-v` to also wipe the database volume).

## Run it — Option B: quick local check without Docker (H2 in-memory DB)

Requires Java 17 and Maven installed locally.

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Good for a fast sanity check while writing code. Switch back to
`docker compose up` before committing, since Option B never touches real
Postgres.

## Editing the schedule

Seed data lives in `DataSeeder.java` and re-syncs (clears + re-inserts) on
every startup, so editing it and redeploying is enough to update
production — no manual DB reset needed. Reflects the real Kasie FM
weekday lineup per the station's agreement (see status note above).

## Next steps (per the roadmap)

- Add a `dayOfWeek` / recurrence concept to `Show` — the real station's
  schedule varies by day, which the current single-repeating-schedule model
  doesn't support.
- Move deployment from Render to AWS (ECR → ECS Fargate → RDS → ALB →
  CloudFront), via Terraform.
