# Configuration Guide

## Environment Variables

The application reads configuration from environment variables at startup. All variables are optional unless marked required.

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `API_KEY` | Yes | — | Your API key for authentication |
| `DB_HOST` | Yes | — | Database hostname or IP address |
| `DB_PORT` | No | `5432` | Database port |
| `DB_NAME` | No | `appdb` | Database name |
| `DB_USER` | No | `appuser` | Database user |
| `DB_PASSWORD` | Yes | — | Database password |
| `LOG_LEVEL` | No | `INFO` | Logging verbosity: DEBUG, INFO, WARN, ERROR |
| `HTTP_TIMEOUT_MS` | No | `30000` | HTTP request timeout in milliseconds |
| `FEATURE_WEBHOOKS` | No | `false` | Enable webhook event delivery |
| `FEATURE_ANALYTICS` | No | `false` | Enable usage analytics |

## Configuration File Schema

If you prefer file-based configuration, create a `config.yaml` in the working directory. Environment variables always take precedence over file values.

```yaml
database:
  host: localhost
  port: 5432
  name: appdb
  user: appuser
  password: secret
  pool_size: 10
  connection_timeout_ms: 5000

http:
  timeout: 30000        # milliseconds
  max_retries: 3
  retry_backoff_ms: 500

logging:
  level: INFO
  file: /var/log/app/application.log

features:
  webhooks: false
  analytics: false
```

## Database Connection Strings

If your infrastructure requires a JDBC connection string instead of individual parameters, use the `DB_URL` environment variable:

```
DB_URL=jdbc:postgresql://localhost:5432/appdb?user=appuser&password=secret&ssl=true
```

For read replicas, set `DB_READ_URL` to route SELECT queries to a separate host:
```
DB_READ_URL=jdbc:postgresql://replica.internal:5432/appdb?user=reader&password=secret
```

## Feature Flags

Feature flags allow you to enable or disable functionality without redeploying. They are evaluated at startup and cannot be changed at runtime without a restart.

- **FEATURE_WEBHOOKS**: When enabled, the application will deliver event notifications to configured webhook URLs. See `api-usage.md` for webhook setup.
- **FEATURE_ANALYTICS**: When enabled, anonymized usage metrics are sent to the analytics service. No personally identifiable information is included.

To enable a feature flag:
```bash
FEATURE_WEBHOOKS=true java -jar app.jar
```

## HTTP Timeout Configuration

The `http.timeout` setting (or `HTTP_TIMEOUT_MS` environment variable) controls how long the client waits for a server response before aborting the request. If you are experiencing timeout errors on slow network connections, increase this value:

```yaml
http:
  timeout: 60000   # 60 seconds
```

Note: Setting the timeout too high can cause requests to hang on network failures. A value between 30 000 and 60 000 ms is recommended for most production environments.
