# Troubleshooting Guide

## HTTP Error Codes

### 401 Unauthorized
A 401 error means your API key is missing or invalid. Check that the `Authorization` header is present and contains a valid key. Re-generate your API key from the dashboard if needed. This error is also returned when the key has been revoked.

### 403 Forbidden
A 403 error means your API key is valid but does not have permission to access the requested resource. Verify that your subscription plan includes access to the endpoint you are calling. Contact support if you believe this is a configuration mistake.

### 429 Too Many Requests — Rate Limit Exceeded
A 429 error means you have exceeded the rate limit. The default rate limit is 100 requests per minute per API key. To resolve this:
- Implement exponential backoff and retry logic in your client.
- Cache responses where possible to reduce repeated calls.
- If you consistently need a higher rate limit, contact support to request a quota increase.
- Check the `Retry-After` response header for the number of seconds to wait before retrying.

### 500 Internal Server Error
A 500 error indicates a problem on the server side. This is not caused by your request. Steps to follow:
- Retry the request after a short delay (start with 5 seconds).
- If the error persists for more than 10 minutes, check the status page for active incidents.
- Capture the `X-Request-Id` header from the response and include it when contacting support.

## Connection Timeouts

If your requests are timing out before receiving a response:
- The default HTTP timeout is 30 seconds. Long-running operations may exceed this. Increase the `http.timeout` value in your `config.yaml`.
- Check your network firewall rules — outbound HTTPS (port 443) must be allowed.
- Verify DNS resolution for the API endpoint is working correctly.

## Log File Locations

| Environment | Log path |
|-------------|----------|
| Linux / Docker | `/var/log/app/application.log` |
| macOS (local dev) | `~/Library/Logs/app/application.log` |
| Windows | `%APPDATA%\app\logs\application.log` |

Log level can be configured via the `LOG_LEVEL` environment variable. Accepted values: `DEBUG`, `INFO`, `WARN`, `ERROR`. Default is `INFO`.

To enable verbose debug logging temporarily:
```bash
LOG_LEVEL=DEBUG java -jar app.jar
```
