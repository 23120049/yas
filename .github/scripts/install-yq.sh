#!/usr/bin/env bash
set -euo pipefail

if command -v yq >/dev/null 2>&1; then
  exit 0
fi

YQ_VERSION="${YQ_VERSION:-v4.44.3}"
sudo wget "https://github.com/mikefarah/yq/releases/download/${YQ_VERSION}/yq_linux_amd64" -O /usr/local/bin/yq
sudo chmod +x /usr/local/bin/yq
