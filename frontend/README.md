# Banking App Frontend

This React frontend is designed to work with the Spring Boot microservice project through the API Gateway at `http://localhost:8080`.

## Run locally

1. Start the backend microservices and the API Gateway.
2. From `frontend/` install dependencies:

```bash
npm install
```

3. Start the frontend:

```bash
npm run dev
```

4. Open the URL shown in the terminal.

## Features

- Accounts list and account creation
- Debit / credit operations
- Transaction submission
- Ledger entry browsing
- Audit event browsing

## API Proxy

The app proxies `/api/*` requests to `http://localhost:8080`.
