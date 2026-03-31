# Kubernetes Runbook

This runbook explains how to run the banking app on Kubernetes in:
- local Kubernetes (Docker Desktop)
- AWS EKS

---

## 1) Run on local Kubernetes (recommended for development)

### Prerequisites

- Docker Desktop running
- Kubernetes enabled in Docker Desktop
- `kubectl` installed

Check cluster:

```bash
kubectl config current-context
kubectl get nodes
```

### Build local images

Run from repo root:

```bash
docker build -t banking/account-service:local -f account-service/Dockerfile .
docker build -t banking/transaction-service:local -f transaction-service/Dockerfile .
docker build -t banking/audit-service:local -f audit-service/Dockerfile .
docker build -t banking/api-gateway:local -f api-gateway/Dockerfile .
docker build -t banking/frontend:local -f frontend/Dockerfile ./frontend
```

### Deploy local overlay

```bash
kubectl apply -k k8s/overlays/local
kubectl get pods -n banking -w
```

### Access app

- Frontend: `http://localhost:30080`
- API Gateway: `http://localhost:30888`

Quick API check:

```bash
curl -s http://localhost:30888/api/accounts
```

### Clean up local deployment

```bash
kubectl delete -k k8s/overlays/local
```

---

## 2) Run on AWS EKS

> Full detailed guide: `docs/eks-deployment.md`

### High-level steps

1. Create ECR repos for each service image.
2. Build and push images to ECR.
3. Create EKS cluster and update kubeconfig.
4. Install AWS Load Balancer Controller.
5. Replace image placeholders in `k8s/base/*.yaml`:
   - `<AWS_ACCOUNT_ID>`
   - `<AWS_REGION>`
6. Deploy:

```bash
kubectl apply -k k8s/base
kubectl get pods -n banking
kubectl get ingress -n banking
```

### Fetch ALB hostname

```bash
kubectl get ingress banking-ingress -n banking -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'
```

Then use:
- `http://<ALB_HOSTNAME>/` for frontend
- `http://<ALB_HOSTNAME>/api/accounts` for API

---

## 3) Troubleshooting

### Pods not starting

```bash
kubectl get pods -n banking
kubectl describe pod <POD_NAME> -n banking
kubectl logs <POD_NAME> -n banking
```

### Image pull issues

- Local K8s: ensure local images exist (`docker images | grep banking/`)
- EKS: confirm ECR URI and IAM permissions for node role

### Reset namespace

```bash
kubectl delete namespace banking --ignore-not-found
kubectl apply -k k8s/overlays/local
```
