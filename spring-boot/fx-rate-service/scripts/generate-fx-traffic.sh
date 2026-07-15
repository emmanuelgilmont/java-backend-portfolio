#!/usr/bin/env bash
# generate-fx-traffic.sh
#
# Generates realistic traffic against fx-rate-service: a mix of valid pairs
# (cache hit/miss) and invalid pairs (4xx errors), to populate the Grafana
# dashboard (latency, error rate, JVM) with real data.
#
# Usage:
#   ./generate-fx-traffic.sh                        # targets localhost:8080, 60 requests
#   ./generate-fx-traffic.sh 192.168.1.50:8080       # targets the homelab
#   ./generate-fx-traffic.sh 192.168.1.50:8080 200   # 200 requests
#   ./generate-fx-traffic.sh 192.168.1.50:8080 0     # runs continuously (Ctrl+C to stop)

set -euo pipefail

HOST="${1:-localhost:8080}"
COUNT="${2:-60}"

# Valid pairs known to StubExternalRateProvider (adjust if your actual list differs)
VALID_PAIRS=("EUR-USD" "EUR-GBP" "USD-JPY" "EUR-JPY" "GBP-USD")

# Deliberately invalid pairs -> trigger UnsupportedPairException (4xx)
INVALID_PAIRS=("XXX-YYY" "ZZZ-EUR" "FOO-BAR")

CURRENCIES=("${VALID_PAIRS[@]}" "${VALID_PAIRS[@]}" "${VALID_PAIRS[@]}" "${INVALID_PAIRS[@]}")
# (valid pairs are duplicated 3x so that ~80% of traffic is "normal" and ~20% errors -
#  a realistic ratio that keeps the error-rate panel readable)

echo "Target: http://${HOST}"
[[ "$COUNT" == "0" ]] && echo "Continuous mode (Ctrl+C to stop)" || echo "Number of requests: ${COUNT}"

i=0
while [[ "$COUNT" == "0" || $i -lt $COUNT ]]; do
  pair="${CURRENCIES[$RANDOM % ${#CURRENCIES[@]}]}"
  code=$(curl -s -o /dev/null -w "%{http_code}" "http://${HOST}/rates/${pair}")
  printf '[%s] GET /rates/%-10s -> %s\n' "$(date +%H:%M:%S)" "$pair" "$code"

  # Small random pause (0.1 - 0.8s) so requests aren't all identical/back-to-back,
  # closer to real traffic for the latency panel.
  sleep "0.$((RANDOM % 8 + 1))"

  i=$((i + 1))
done

echo "Done."