#!/usr/bin/env bash
set -euo pipefail

: "${GITOPS_DIR:?GITOPS_DIR is required}"
: "${ENVIRONMENT:?ENVIRONMENT is required}"
: "${SERVICE:?SERVICE is required}"
: "${IMAGE_TAG:?IMAGE_TAG is required}"

case "$SERVICE" in
  storefront|backoffice)
    KIND="ui"
    ;;
  *)
    KIND="backend"
    ;;
esac

SERVICE_FILE="${GITOPS_DIR}/values/${ENVIRONMENT}/dynamic-tags/${SERVICE}.yaml"
AGGREGATE_FILE="${GITOPS_DIR}/values/${ENVIRONMENT}/dynamic-tags/image-tags.yaml"

if [ ! -f "$SERVICE_FILE" ]; then
  echo "Missing service values file: $SERVICE_FILE" >&2
  exit 1
fi

if [ -n "${ROLLBACK_FILE:-}" ] && [ -f "$SERVICE_FILE" ]; then
  CURRENT_TAG="$(yq eval ".${KIND}.image.tag" "$SERVICE_FILE")"
  if [ "$CURRENT_TAG" != "null" ] && [ -n "$CURRENT_TAG" ]; then
    touch "$ROLLBACK_FILE"
    yq eval -i ".\"${SERVICE}\" = \"${CURRENT_TAG}\"" "$ROLLBACK_FILE"
    echo "Backed up ${SERVICE}=${CURRENT_TAG} to ${ROLLBACK_FILE}"
  fi
fi

yq eval -i ".${KIND}.image.tag = \"${IMAGE_TAG}\"" "$SERVICE_FILE"

if [ -f "$AGGREGATE_FILE" ]; then
  yq eval -i ".${KIND}.\"${SERVICE}\".image.tag = \"${IMAGE_TAG}\"" "$AGGREGATE_FILE"
fi

echo "Updated ${ENVIRONMENT}/${SERVICE} to ${IMAGE_TAG}"
