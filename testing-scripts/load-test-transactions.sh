#!/usr/bin/env bash

set -euo pipefail

# Usage:
#   bash testing-scripts/load-test-transactions.sh [total_requests] [parallelism] [amount] [gateway_base_url]
#
# Example:
#   bash testing-scripts/load-test-transactions.sh 10000 20 1 http://localhost:8888

TOTAL_REQUESTS="${1:-10000}"
PARALLELISM="${2:-20}"
AMOUNT="${3:-1}"
GATEWAY_BASE_URL="${4:-http://localhost:8888}"

FROM_ACCOUNT_A="${FROM_ACCOUNT_A:-A1001}"
FROM_ACCOUNT_B="${FROM_ACCOUNT_B:-A1002}"

if ! command -v curl >/dev/null 2>&1; then
  echo "curl is required but not installed."
  exit 1
fi

START_TS="$(date +%s)"

echo "Starting transaction load test..."
echo "Requests: ${TOTAL_REQUESTS}, Parallelism: ${PARALLELISM}, Amount: ${AMOUNT}"
echo "Endpoint: ${GATEWAY_BASE_URL}/api/transactions"
echo

TMP_STATUS_FILE="$(mktemp)"
trap 'rm -f "${TMP_STATUS_FILE}"' EXIT

for i in $(seq 1 "${TOTAL_REQUESTS}"); do
  while [ "$(jobs -rp | wc -l | tr -d ' ')" -ge "${PARALLELISM}" ]; do
    sleep 0.05
  done

  (
    if [ $((i % 2)) -eq 0 ]; then
      FROM="${FROM_ACCOUNT_A}"
      TO="${FROM_ACCOUNT_B}"
    else
      FROM="${FROM_ACCOUNT_B}"
      TO="${FROM_ACCOUNT_A}"
    fi

    code="$(curl -s -o /dev/null -w "%{http_code}" \
      -X POST "${GATEWAY_BASE_URL}/api/transactions" \
      -H "Content-Type: application/json" \
      -d "{\"fromAccountId\":\"${FROM}\",\"toAccountId\":\"${TO}\",\"amount\":${AMOUNT}}")"
    echo "${code}" >> "${TMP_STATUS_FILE}"
  ) &
done

wait

STATUS_SUMMARY="$(
  awk '{count[$1]++} END {for (code in count) printf "%s %s\n", code, count[code]}' "${TMP_STATUS_FILE}" | sort -n
)"

END_TS="$(date +%s)"
DURATION=$((END_TS - START_TS))
if [ "${DURATION}" -le 0 ]; then
  DURATION=1
fi
RPS=$((TOTAL_REQUESTS / DURATION))

SUCCESS_COUNT="$(echo "${STATUS_SUMMARY}" | awk '$1 ~ /^2/ {sum += $2} END {print sum + 0}')"
FAILURE_COUNT=$((TOTAL_REQUESTS - SUCCESS_COUNT))

echo "Status code summary:"
echo "${STATUS_SUMMARY}"
echo
echo "Completed in ${DURATION}s (~${RPS} req/s)"
echo "Success: ${SUCCESS_COUNT}"
echo "Failure: ${FAILURE_COUNT}"
echo
echo "Tip: Watch metrics in Grafana while this runs (http://localhost:3001)."
