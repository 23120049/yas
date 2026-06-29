#!/usr/bin/env bash
set -euo pipefail

: "${GITOPS_DIR:?GITOPS_DIR is required}"
: "${ENVIRONMENT:=dev}"

ROLLBACK_FILE="${GITOPS_DIR}/rollback_state.yaml"

if [ ! -f "$ROLLBACK_FILE" ]; then
  echo "Missing rollback file: $ROLLBACK_FILE" >&2
  exit 1
fi

SERVICES="$(yq eval 'keys | .[]' "$ROLLBACK_FILE")"
if [ -z "$SERVICES" ]; then
  echo "Rollback file is empty."
  exit 0
fi

for SERVICE in $SERVICES; do
  OLD_TAG="$(yq eval ".\"${SERVICE}\"" "$ROLLBACK_FILE")"
  if [ "$OLD_TAG" = "null" ] || [ -z "$OLD_TAG" ]; then
    continue
  fi

  SERVICE="$SERVICE" IMAGE_TAG="$OLD_TAG" ENVIRONMENT="$ENVIRONMENT" GITOPS_DIR="$GITOPS_DIR" \
    "$(dirname "$0")/update-gitops-image-tag.sh"
done

echo "Rollback state applied to ${ENVIRONMENT}."
