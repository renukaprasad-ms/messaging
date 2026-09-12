#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="${ROOT_DIR}/.env"
BACKEND_IMAGE="messaging-backend:latest"
FRONTEND_IMAGE="messaging-frontend:latest"

usage() {
  cat <<'EOF'
Usage:
  ./k8s.sh start
  ./k8s.sh stop
  ./k8s.sh restart
  ./k8s.sh delete
  ./k8s.sh logs <service> [tail]

Services for logs:
  backend | frontend | postgres | redis | kafka

Examples:
  ./k8s.sh logs backend
  ./k8s.sh logs kafka 200
EOF
}

require_env() {
  if [[ ! -f "${ENV_FILE}" ]]; then
    echo "Missing ${ENV_FILE}" >&2
    exit 1
  fi
}

apply_env() {
  require_env
  kubectl create secret generic messaging-env \
    --from-env-file="${ENV_FILE}" \
    --dry-run=client \
    -o yaml | kubectl apply -f -
}

build_and_load_images() {
  docker build -t "${BACKEND_IMAGE}" "${ROOT_DIR}/backend"
  docker build \
    -t "${FRONTEND_IMAGE}" \
    --build-arg VITE_API_BASE_URL="${VITE_API_BASE_URL:-http://localhost:8001}" \
    "${ROOT_DIR}/frontend"

  docker save "${BACKEND_IMAGE}" -o /tmp/messaging-backend.tar
  docker save "${FRONTEND_IMAGE}" -o /tmp/messaging-frontend.tar

  sudo k3s ctr images import /tmp/messaging-backend.tar
  sudo k3s ctr images import /tmp/messaging-frontend.tar
}

load_env() {
  require_env
  while IFS='=' read -r key value || [[ -n "${key}" ]]; do
    key="${key%$'\r'}"
    value="${value%$'\r'}"
    [[ -z "${key}" || "${key}" == \#* ]] && continue
    export "${key}=${value}"
  done < "${ENV_FILE}"
}

scale_if_exists() {
  local kind="$1"
  local name="$2"
  local replicas="$3"

  if kubectl get "${kind}" "${name}" >/dev/null 2>&1; then
    kubectl scale "${kind}" "${name}" --replicas="${replicas}"
  fi
}

start() {
  load_env

  apply_env
  build_and_load_images
  kubectl apply -f "${ROOT_DIR}/k8s/dependencies.yaml"
  kubectl apply -f "${ROOT_DIR}/k8s/backend.yaml"
  kubectl apply -f "${ROOT_DIR}/k8s/frontend.yaml"
  kubectl get pods
}

stop() {
  scale_if_exists statefulset messaging-backend 0
  scale_if_exists deployment messaging-frontend 0
  scale_if_exists deployment postgres 0
  scale_if_exists deployment redis 0
  scale_if_exists deployment kafka 0
  kubectl get pods
}

restart() {
  load_env

  apply_env
  build_and_load_images
  kubectl delete deployment postgres redis kafka --ignore-not-found
  kubectl apply -f "${ROOT_DIR}/k8s/dependencies.yaml"
  kubectl apply -f "${ROOT_DIR}/k8s/backend.yaml"
  kubectl apply -f "${ROOT_DIR}/k8s/frontend.yaml"
  kubectl rollout restart deployment/messaging-frontend
  kubectl delete pod -l app=messaging-backend --ignore-not-found
  kubectl get pods
}

delete_all() {
  kubectl delete -f "${ROOT_DIR}/k8s/frontend.yaml" --ignore-not-found
  kubectl delete -f "${ROOT_DIR}/k8s/backend.yaml" --ignore-not-found
  kubectl delete -f "${ROOT_DIR}/k8s/dependencies.yaml" --ignore-not-found
  kubectl delete secret messaging-env --ignore-not-found
  kubectl get pods
}

logs() {
  local service="${1:-}"
  local tail="${2:-100}"
  local selector

  case "${service}" in
    backend) selector="app=messaging-backend" ;;
    frontend) selector="app=messaging-frontend" ;;
    postgres) selector="app=postgres" ;;
    redis) selector="app=redis" ;;
    kafka) selector="app=kafka" ;;
    *)
      usage
      exit 1
      ;;
  esac

  kubectl logs -l "${selector}" --tail="${tail}" -f
}

command="${1:-}"
case "${command}" in
  start) start ;;
  stop) stop ;;
  restart) restart ;;
  delete) delete_all ;;
  logs) shift; logs "$@" ;;
  *) usage; exit 1 ;;
esac
