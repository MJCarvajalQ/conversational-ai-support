# Setup Instructions

## Prerequisites

Before installing, make sure the following are available on your system:

| Requirement | Minimum version | Check command |
|-------------|----------------|---------------|
| Java (JDK) | 17 | `java -version` |
| Maven | 3.8 | `mvn -version` |
| Docker | 20.10 | `docker --version` |
| Docker Compose | 2.0 | `docker compose version` |
| Git | 2.30 | `git --version` |

An active API key is also required. Generate one from the developer dashboard before proceeding.

## Docker Setup

The recommended way to run the application locally is with Docker Compose.

**1. Clone the repository:**
```bash
git clone https://github.com/your-org/your-app.git
cd your-app
```

**2. Copy the example configuration:**
```bash
cp config.properties.example config.properties
```
Open `config.properties` and set your API key and database credentials.

**3. Start all services:**
```bash
docker compose up -d
```
This starts the application server and a PostgreSQL database container. The application will be available at `http://localhost:8080`.

**4. Verify the application is running:**
```bash
curl http://localhost:8080/health
# Expected response: {"status": "ok"}
```

## First-Run Checklist

Complete the following steps the first time you set up the application:

- [ ] Copy `config.properties.example` → `config.properties` and fill in all required values
- [ ] Verify the database is reachable: `docker compose ps` should show the `db` container as `Up`
- [ ] Run database migrations: `docker compose exec app ./migrate.sh`
- [ ] Create an initial admin account: `docker compose exec app ./create-admin.sh`
- [ ] Test authentication by calling `GET /health` with your API key
- [ ] Register at least one webhook endpoint if you need event notifications

## Running Without Docker

If you prefer to run the application directly with Maven:

```bash
# 1. Set required environment variables
export API_KEY=your-api-key-here
export DB_HOST=localhost
export DB_PASSWORD=your-db-password

# 2. Build the project
mvn clean package -q

# 3. Run the JAR
java -jar target/app.jar
```

Make sure a PostgreSQL instance is running and accessible before starting.

## Upgrading

To upgrade to a newer version:

**Docker:**
```bash
docker compose pull
docker compose up -d
docker compose exec app ./migrate.sh
```

**Manual:**
```bash
git pull origin main
mvn clean package -q
java -jar target/app.jar
```

Always run database migrations after upgrading — new versions may include schema changes. Check the `CHANGELOG.md` for breaking changes before upgrading across major versions.

## Uninstalling

To stop all services and remove containers:
```bash
docker compose down -v
```
The `-v` flag also removes the database volume. Omit it if you want to preserve your data.
