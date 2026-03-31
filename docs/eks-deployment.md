# AWS EKS Deployment Guide

This guide deploys the banking app to Amazon EKS using manifests in `k8s/base`.

## What is included

- Kubernetes resources for:
  - `postgres`, `zookeeper`, `kafka`
  - `account-service`, `transaction-service`, `audit-service`
  - `api-gateway`, `frontend`
  - ALB-backed `Ingress`
- Shared configuration and secret templates
- Kustomize entrypoint (`k8s/base/kustomization.yaml`)

## Prerequisites

- AWS CLI configured (`aws configure`)
- `kubectl` installed
- `eksctl` installed
- `docker` installed
- Access to push images to Amazon ECR

## 1) Create ECR repositories

Set variables:

```bash
export AWS_REGION=us-east-1
export AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
```

Create repos:

```bash
for repo in account-service transaction-service audit-service api-gateway frontend; do
  aws ecr describe-repositories --repository-names banking/$repo --region "$AWS_REGION" >/dev/null 2>&1 || \
  aws ecr create-repository --repository-name banking/$repo --region "$AWS_REGION" >/dev/null
done
```

Login Docker to ECR:

```bash
aws ecr get-login-password --region "$AWS_REGION" | \
docker login --username AWS --password-stdin "$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com"
```

## 2) Build and push images

### Backend services

From repo root:

```bash
mvn -DskipTests clean package
```

Create Dockerfiles for backend services if you do not already have them. Each service should produce an executable jar and run it with `java -jar`.

Build/tag/push example (repeat per service):

```bash
SERVICE=account-service
IMAGE="$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/banking/$SERVICE:latest"

docker build -t "$IMAGE" "./$SERVICE"
docker push "$IMAGE"
```

### Frontend

```bash
IMAGE="$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/banking/frontend:latest"
docker build -t "$IMAGE" ./frontend
docker push "$IMAGE"
```

## 3) Create EKS cluster

Example:

```bash
eksctl create cluster \
  --name banking-eks \
  --region "$AWS_REGION" \
  --nodes 3 \
  --node-type t3.medium
```

Update kubeconfig:

```bash
aws eks update-kubeconfig --region "$AWS_REGION" --name banking-eks
```

## 4) Install AWS Load Balancer Controller

Install controller (required for ALB Ingress). Follow AWS official steps for your EKS version:

- [AWS Load Balancer Controller installation guide](https://docs.aws.amazon.com/eks/latest/userguide/aws-load-balancer-controller.html)

## 5) Update image placeholders

In all files under `k8s/base`, replace:
- `<AWS_ACCOUNT_ID>`
- `<AWS_REGION>`

Quick replacement example (macOS):

```bash
find k8s/base -type f -name "*.yaml" -exec sed -i '' "s/<AWS_ACCOUNT_ID>/$AWS_ACCOUNT_ID/g" {} \;
find k8s/base -type f -name "*.yaml" -exec sed -i '' "s/<AWS_REGION>/$AWS_REGION/g" {} \;
```

## 6) Deploy application

```bash
kubectl apply -k k8s/base
```

Verify:

```bash
kubectl get pods -n banking
kubectl get svc -n banking
kubectl get ingress -n banking
```

Fetch ALB endpoint:

```bash
kubectl get ingress banking-ingress -n banking -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'
```

Use that hostname in browser to access frontend and `/api/*`.

## 7) Post-deploy checks

- Account list:

```bash
curl "http://<ALB_HOSTNAME>/api/accounts"
```

- Transaction test:

```bash
curl -X POST "http://<ALB_HOSTNAME>/api/transactions" \
  -H "Content-Type: application/json" \
  -d '{"fromAccountId":"A1001","toAccountId":"A1002","amount":10}'
```

## 8) Notes and recommendations

- For production:
  - move Kafka/Postgres to managed services (MSK/RDS)
  - use External Secrets or AWS Secrets Manager (instead of plaintext `Secret`)
  - add HPA, PDB, resource requests/limits, readiness/liveness probes
  - use CI/CD (GitHub Actions + ECR + ArgoCD/Flux)
- Current manifests are a practical baseline to get the stack running on EKS quickly.
