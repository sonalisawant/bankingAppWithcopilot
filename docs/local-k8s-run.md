# Run on Local Kubernetes

This setup uses `k8s/overlays/local` and local Docker images (no ECR required).

## 1) Enable Kubernetes in Docker Desktop

- Open Docker Desktop
- Go to Settings -> Kubernetes -> Enable Kubernetes
- Wait until cluster is ready

Verify:

```bash
kubectl config current-context
kubectl get nodes
```

## 2) Build local images

Run from repo root:

```bash
docker build -t banking/account-service:local -f account-service/Dockerfile .
docker build -t banking/transaction-service:local -f transaction-service/Dockerfile .
docker build -t banking/audit-service:local -f audit-service/Dockerfile .
docker build -t banking/api-gateway:local -f api-gateway/Dockerfile .
docker build -t banking/frontend:local -f frontend/Dockerfile ./frontend
```

## 3) Deploy local overlay

```bash
kubectl apply -k k8s/overlays/local
kubectl get pods -n banking -w
```

## 4) Access application

- Frontend: `http://localhost:30080`
- API Gateway: `http://localhost:30888`

Quick check:

```bash
curl -s http://localhost:30888/api/accounts
```

## 5) Cleanup

```bash
kubectl delete -k k8s/overlays/local
```

Optional image cleanup:

```bash
docker image rm banking/account-service:local banking/transaction-service:local banking/audit-service:local banking/api-gateway:local banking/frontend:local
```
