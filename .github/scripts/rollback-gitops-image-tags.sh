#!/usr/bin/env bash
set -euo pipefail

: "${GITOPS_DIR:?GITOPS_DIR is required}"
: "${ENVIRONMENT:=dev}"

ROLLBACK_FILE="${GITOPS_DIR}/rollback_state.yaml"

if [ ! -f "$ROLLBACK_FILE" ]; then
  echo "Missing rollback file: $ROLLBACK_FILE" >&2
  exit 1
fi

# Process backend services
BACKEND_SERVICES=$(yq eval '.backend | select(. != null) | keys | .[]' "$ROLLBACK_FILE" 2>/dev/null || true)
for SERVICE in $BACKEND_SERVICES; do
  OLD_TAG=$(yq eval ".backend.\"${SERVICE}\".image.tag" "$ROLLBACK_FILE" 2>/dev/null)
  if [ "$OLD_TAG" != "null" ] && [ -n "$OLD_TAG" ]; then
    SERVICE="$SERVICE" IMAGE_TAG="$OLD_TAG" ENVIRONMENT="$ENVIRONMENT" GITOPS_DIR="$GITOPS_DIR" \
      bash "$(dirname "$0")/update-gitops-image-tag.sh"
  fi
done

# Process ui services
UI_SERVICES=$(yq eval '.ui | select(. != null) | keys | .[]' "$ROLLBACK_FILE" 2>/dev/null || true)
for SERVICE in $UI_SERVICES; do
  OLD_TAG=$(yq eval ".ui.\"${SERVICE}\".image.tag" "$ROLLBACK_FILE" 2>/dev/null)
  if [ "$OLD_TAG" != "null" ] && [ -n "$OLD_TAG" ]; then
    SERVICE="$SERVICE" IMAGE_TAG="$OLD_TAG" ENVIRONMENT="$ENVIRONMENT" GITOPS_DIR="$GITOPS_DIR" \
      bash "$(dirname "$0")/update-gitops-image-tag.sh"
  fi
done

echo "Rollback state applied to ${ENVIRONMENT}."
