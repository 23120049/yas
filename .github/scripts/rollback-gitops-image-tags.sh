#!/usr/bin/env bash
set -euo pipefail

: "${GITOPS_DIR:?GITOPS_DIR is required}"
: "${ENVIRONMENT:=dev}"
: "${SERVICE:?SERVICE is required}"

ROLLBACK_FILE="${GITOPS_DIR}/rollback_state.yaml"

if [ ! -f "$ROLLBACK_FILE" ]; then
  echo "Missing rollback file: $ROLLBACK_FILE" >&2
  exit 1
fi

case "$SERVICE" in
  storefront|backoffice)
    KIND="ui"
    ;;
  *)
    KIND="backend"
    ;;
esac

OLD_TAG=$(yq eval ".${KIND}.\"${SERVICE}\".image.tag" "$ROLLBACK_FILE" 2>/dev/null)

if [ "$OLD_TAG" = "null" ] || [ -z "$OLD_TAG" ]; then
  echo "No rollback image tag found for ${SERVICE} in ${ROLLBACK_FILE}" >&2
  exit 1
fi

SERVICE="$SERVICE" IMAGE_TAG="$OLD_TAG" ENVIRONMENT="$ENVIRONMENT" GITOPS_DIR="$GITOPS_DIR" \
  bash "$(dirname "$0")/update-gitops-image-tag.sh"

echo "Rollback state applied to ${ENVIRONMENT}/${SERVICE}: ${OLD_TAG}"
