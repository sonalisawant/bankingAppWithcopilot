# Banking App Testing and Observability Guide

This guide explains how to:
- run all services with one command
- execute high-volume transaction tests (10k)
- monitor the system with Prometheus and Grafana
- verify Kafka-driven behavior

## 1) Start the full stack

From project root:

```bash
docker compose up -d --build
```

Core URLs:
- Frontend: `http://localhost:3000`
- API Gateway: `http://localhost:8888`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3001` (default user/password: `admin` / `admin`)

Check running containers:

```bash
docker compose ps
```

## 2) Transaction flow (how deposit/withdraw works with Kafka)

In this project, Kafka event publishing happens during transaction processing.

Flow:
1. Client calls `POST /api/transactions` through API Gateway.
2. `transaction-service` debits source account and credits destination account by calling `account-service`.
3. `transaction-service` stores transaction record.
4. `transaction-service` publishes transaction event to Kafka topic `banking-transactions`.
5. `audit-service` consumes event and saves audit entries.
6. `notification-service` consumes event and logs notification text.

Important:
- Direct account APIs (`/api/accounts/{id}/debit` and `/api/accounts/{id}/credit`) update balance but do not publish transaction event by themselves.

## 3) Quick functional verification

```bash
# Check balances before
curl -s "http://localhost:8888/api/accounts/A1001"
curl -s "http://localhost:8888/api/accounts/A1002"

# Perform one transaction
curl -s -X POST "http://localhost:8888/api/transactions" \
  -H "Content-Type: application/json" \
  -d '{"fromAccountId":"A1001","toAccountId":"A1002","amount":50}'

# Check balances after
curl -s "http://localhost:8888/api/accounts/A1001"
curl -s "http://localhost:8888/api/accounts/A1002"

# Verify audit events
curl -s "http://localhost:8888/api/audits"
```

Optional Kafka verification:

```bash
docker compose exec -T kafka kafka-console-consumer \
  --bootstrap-server kafka:9092 \
  --topic banking-transactions \
  --from-beginning \
  --max-messages 1
```

## 4) High-volume test (10k transactions)

A reusable script is available at:
- `testing-scripts/load-test-transactions.sh`

Default run (10k requests, parallelism 20):

```bash
bash testing-scripts/load-test-transactions.sh
```

Custom run:

```bash
bash testing-scripts/load-test-transactions.sh 10000 30 2 http://localhost:8888
```

Arguments:
1. total requests (default `10000`)
2. parallel workers (default `20`)
3. transaction amount (default `1`)
4. gateway base URL (default `http://localhost:8888`)

Output includes:
- status code summary (2xx/4xx/5xx counts)
- total duration
- approximate requests/sec
- success and failure counts

## 5) Metrics and dashboards

Actuator + Prometheus metrics are enabled on:
- `account-service` (`/actuator/prometheus`)
- `transaction-service` (`/actuator/prometheus`)
- `audit-service` (`/actuator/prometheus`)
- `api-gateway` (`/actuator/prometheus`)

Prometheus scrapes those services every 5 seconds using:
- `observability/prometheus/prometheus.yml`

Grafana has Prometheus datasource auto-provisioned via:
- `observability/grafana/provisioning/datasources/datasource.yml`

### Useful Prometheus queries (Grafana Explore)

Request rate:

```promql
sum(rate(http_server_requests_seconds_count[1m])) by (application, uri, method, status)
```

P95 latency:

```promql
histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[1m])) by (le, application, uri))
```

5xx rate:

```promql
sum(rate(http_server_requests_seconds_count{status=~"5.."}[1m])) by (application, uri)
```

## 6) Suggested test procedure for 10k run

1. Start stack: `docker compose up -d --build`
2. Open Grafana (`http://localhost:3001`) and Prometheus (`http://localhost:9090`)
3. Start load script in terminal:
   - `bash testing-scripts/load-test-transactions.sh`
4. Watch request rate, latency, and 5xx graphs during execution.
5. After completion, validate:
   - balances for test accounts
   - audit records count trend
   - Kafka topic consumption health

## 7) Troubleshooting

### 502 from frontend `/api/*`
- Usually startup timing issue (frontend starts before gateway is ready).
- Wait a few seconds and retry, or restart:

```bash
docker compose up -d --force-recreate api-gateway frontend
```

### Docker credential error (`docker-credential-desktop not found`)
- Ensure Docker Desktop is installed and running.
- If needed, add Docker Desktop binaries to PATH:

```bash
export PATH="$PATH:/Applications/Docker.app/Contents/Resources/bin"
```

Then retry:

```bash
docker compose up -d --build
```

### Check service logs

```bash
docker compose logs --tail=200 api-gateway transaction-service account-service audit-service
```
