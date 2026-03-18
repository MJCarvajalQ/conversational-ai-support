# API Usage Guide

## Authentication

Every API request must include an authentication header. The API uses API key authentication (also referred to as token-based login). Missing or invalid credentials will result in a 401 Unauthorized response.

Include the following header in all requests:
```
Authorization: Bearer YOUR_API_KEY
```

Authentication errors and login failures are the most common cause of 401 responses. If you receive a 401, verify that:
- The `Authorization` header is spelled correctly (case-sensitive).
- The API key has not expired or been revoked.
- You are not accidentally sending the key in the request body instead of the header.

## API Endpoints

Base URL: `https://api.example.com/v1`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/health` | Health check — returns 200 if the API is operational |
| GET | `/customers/{id}` | Retrieve a customer record by ID |
| POST | `/customers` | Create a new customer |
| PUT | `/customers/{id}` | Update an existing customer |
| GET | `/invoices` | List invoices (paginated) |
| POST | `/invoices/{id}/refund` | Initiate a refund for an invoice |
| GET | `/events` | List webhook events |
| POST | `/webhooks` | Register a webhook endpoint |

## Rate Limits

The API enforces rate limits to ensure fair usage across all clients.

| Plan | Requests per minute | Requests per day |
|------|--------------------|--------------------|
| Starter | 30 | 5 000 |
| Pro | 100 | 50 000 |
| Enterprise | 500 | Unlimited |

When you exceed the rate limit, the API returns a **429 Too Many Requests** response. The response includes a `Retry-After` header indicating how many seconds to wait before retrying.

Best practices to stay within rate limits:
- Cache GET responses on the client side when the data does not change frequently.
- Batch multiple operations into a single request where the API supports it.
- Implement exponential backoff for retries.

## Pagination

List endpoints return paginated results. Use the `page` and `limit` query parameters to navigate results.

```
GET /invoices?page=2&limit=25
```

Response includes pagination metadata:
```json
{
  "data": [...],
  "pagination": {
    "page": 2,
    "limit": 25,
    "total": 120,
    "total_pages": 5
  }
}
```

To iterate all pages, keep requesting until `page >= total_pages`.

## Webhooks

Webhooks allow your application to receive real-time notifications when events occur (e.g. a new invoice is created, a refund is processed).

To register a webhook:
```
POST /webhooks
{
  "url": "https://yourapp.com/webhook-handler",
  "events": ["invoice.created", "refund.processed"]
}
```

The API will send an HTTP POST to your URL with a JSON payload whenever one of the subscribed events occurs. Your endpoint must return a 200 response within 5 seconds, otherwise the delivery will be retried up to 3 times with exponential backoff.

Webhook payloads are signed using HMAC-SHA256. Verify the `X-Signature` header in every incoming webhook to ensure it originated from our API:
```
X-Signature: sha256=<hex_digest>
```
