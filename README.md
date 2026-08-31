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
