# weather-service

Spring Boot microservice that fetches current conditions and a short-range forecast
for any Belgian location, using the free [Open-Meteo](https://open-meteo.com/) API.

## What it does

- Resolves a place name or Belgian postcode via the Open-Meteo Geocoding API
- Returns current conditions + up to 3-day forecast as JSON
- Translates WMO weather codes into human-readable descriptions (English)
- Caches geocoding results (7 days) and forecasts (15 minutes) with Caffeine
- Falls back to configured alternatives when the default location can't be resolved

## Tech stack

- Java 21 · Spring Boot 3 · Spring RestClient
- HOCON config (`application.conf`)
- Caffeine cache
- Docker / Docker Compose

## Endpoints

```
GET /v1/weather?place=Leuven&days=2   # place and days are optional
GET /v1/weather/default               # uses configured default (see application.conf)
GET /v1/weather/human?place=Leuven    # human-readable summary, plain text, place is optional
```

`days` is capped at 3 and defaults to 3. `place` accepts a city name or a 4-digit Belgian postal code (not both).

## Sample response

### JSON (`/v1/weather`)

```json
{
  "place": {
    "name": "Leuven",
    "postcode": "3000",
    "country": "BE",
    "timezone": "Europe/Brussels"
  },
  "current": {
    "temperatureC": 14.2,
    "windKmh": 22.0,
    "windDirectionDeg": 210,
    "precipitationMm": 0.0,
    "weatherCode": 3,
    "weatherCodeMeaning": "Overcast"
  },
  "daily": [
    {
      "date": "2025-03-19",
      "tempMaxC": 15.1,
      "tempMinC": 9.3,
      "precipSumMm": 1.2,
      "precipProbMaxPct": 40,
      "windMaxKmh": 28.0,
      "weatherCode": 61,
      "weatherCodeMeaning": "Rain (light)",
      "uvIndex": 3.2,
      "windKmh": 22.0,
      "windDirectionDeg": 210
    }
  ]
}
```

### Plain text (`/v1/weather/human`)

```
Today's weather in Leuven, 2025-03-19
Rain (light), wind 22 km/h from the SW.
Temperature: min 9.3°C - max 15.1°C ; currently 14.2°C.
Rain: 1.2 mm expected (40% chance). UV index max: 3.2.
```

## Run locally

```bash
# Maven (sans Docker)
./mvnw spring-boot:run
```

## Try it

`place` accepts a city name or a 4-digit Belgian postal code (not both). `days` is optional and defaults to 3 (max 3).

```bash
# Weather by city name
curl "http://localhost:8765/v1/weather?place=Leuven"

# Weather by postal code
curl "http://localhost:8765/v1/weather?place=3000"

# With forecast days
curl "http://localhost:8765/v1/weather?place=Leuven&days=2"

# Default location (Zoutleeuw)
curl "http://localhost:8765/v1/weather/default"

# Human-readable summary
curl "http://localhost:8765/v1/weather/human?place=Leuven"

# Human-readable summary, default location
curl "http://localhost:8765/v1/weather/human"
```

## Deploy (Docker, on a remote server)

```bash
# 1. Build the JAR
./mvnw package -DskipTests

# 2. Transfer the JAR to docker/
scp target/weather-0.0.1-SNAPSHOT.jar user@server:/path/to/docker/

# 3. Start
cd docker && docker compose up -d

# Restart after a new JAR
docker compose restart weather-service
```

## Configuration

All settings live in `src/main/resources/application.conf`:

| Key                          | Default              | Description                |
|------------------------------|----------------------|----------------------------|
| `weather.defaultQuery`       | `3440 Zoutleeuw, BE` | Used by `/weather/default` |
| `weather.forecastDays`       | `3`                  | Max days returned          |
| `weather.cache.forecastTtl`  | `15m`                | Forecast cache TTL         |
| `weather.cache.geocodingTtl` | `7d`                 | Geocoding cache TTL        |

## Design notes

Weather codes from Open-Meteo follow the [WMO 4677 standard](https://www.nodc.noaa.gov/archive/arc0021/0002199/1.1/data/0-data/HTML/WMO-CODE/WMO4677.HTM).
Rather than i18n files, descriptions are stored directly in a `WMOWeatherCode` enum with an O(1) lookup map —
a deliberate trade-off: the WMO table is stable, English-only, and small enough to fit comfortably in an enum.

## Docker networking

This service can optionally join a shared Docker network to be called by other services.

Create the network once on the host:

```bash
docker network create bot-network
```