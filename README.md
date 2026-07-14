# Kasie FM API

Spring Boot backend for the Kasie FM Android app. Serves the show schedule
and stream URL that used to be hardcoded in `MainActivity.java`.

**Live deployment:** `https://kasifm-api.onrender.com` (Render free tier —
sleeps after inactivity, ~30–60s cold start on first request after idle).

**CI:** GitHub Actions builds and deploys automatically on every push to
main.

> **Status note:** this is a student prototype inspired by the real Kasie FM
> 97.1 community station. It is **not** the station's official backend, and
> the station has not been contacted yet. Seed data is placeholder except
> for one verified detail (`#HomeDrive`, 15:00–18:00, which matches the real
> station's actual schedule). Do not add the station's real presenter names,
> full schedule, or live stream URL here until they've been approached and
> have agreed — see the project journey log.

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
`#HomeDrive`, `#Throwback`, `#Highlights`, `#NewsHour`.

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

Seed data lives in `DataSeeder.java` (only seeds on first run — drop the DB
volume with `docker compose down -v` to re-seed). Presenter fields are
currently placeholder pending Kasie FM's permission (see status note above)
— do not fill these in with the station's real presenter names without
their agreement.

## Next steps (per the roadmap)

- Contact Kasie FM directly before using any more of their real branding,
  schedule, or stream data.
- Add a `dayOfWeek` / recurrence concept to `Show` — the real station's
  schedule varies by day, which the current single-repeating-schedule model
  doesn't support.
- Move deployment from Render to AWS (ECR → ECS Fargate → RDS → ALB →
  CloudFront), via Terraform.
