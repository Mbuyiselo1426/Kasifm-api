# Kasie FM API

Spring Boot backend for the Kasie FM Android app. Serves the show schedule and
stream URL that used to be hardcoded in `MainActivity.java`. Built as Phase 1
of the Kasie FM Cloud-Native Roadmap.

## Endpoints

| Method | Path                  | Returns                              |
|--------|-----------------------|---------------------------------------|
| GET    | `/api/schedule`       | List of all shows, ordered by start time |
| GET    | `/api/schedule/{id}`  | A single show                         |
| GET    | `/api/stream-url`     | `{ streamUrl, streamUrlLite }`        |

## Run it - Option A: Docker (recommended, matches Week 4 checkpoint)

```bash
docker-compose up --build
```

Then open http://localhost:8080/api/schedule - you should see JSON with
`#HomeDrive`, `#Throwback`, `#Highlights`, `#NewsHour`.

To stop: `docker-compose down` (add `-v` to also wipe the database volume).

## Run it - Option B: quick local check without Docker (H2 in-memory DB)

Requires Java 17 and Maven installed locally.

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Good for a fast sanity check while you're writing code. Switch back to
`docker-compose up` before you consider Phase 1 done, since Option B never
touches real Postgres.

## Editing the schedule

For now, real show data lives in `DataSeeder.java` (it only seeds on first
run, so edit it and drop the DB volume with `docker-compose down -v` to
re-seed). Replace the `"TBC"` presenter names and descriptions with the real
Kasie FM lineup.

## Next steps (per the roadmap)

- Phase 2: point the Android app's Retrofit client at `/api/schedule` and
  `/api/stream-url` instead of the hardcoded constants.
- Phase 3: this same Dockerfile gets pushed to ECR and deployed on ECS
  Fargate via Terraform - nothing here changes, just where it runs.
