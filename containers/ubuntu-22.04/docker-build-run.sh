#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
REPO_ROOT=$(cd -- "${SCRIPT_DIR}/../../.." && pwd)
UBUNTU_VERSION=$(basename -- "${SCRIPT_DIR}")

IMAGE_TAG=${IMAGE_TAG:-project-os/${UBUNTU_VERSION}:latest}
BUILD_DIR=${BUILD_DIR:-build}
BITBAKE_TARGET=${BITBAKE_TARGET:-${1:-core-image-project}}
BITBAKE_TEMPLATE=${BITBAKE_TEMPLATE:-qemux86-64}

if ! command -v docker >/dev/null 2>&1; then
  echo "docker is required but was not found in PATH" >&2
  exit 1
fi

docker build \
  --tag "${IMAGE_TAG}" \
  --file "${SCRIPT_DIR}/Dockerfile" \
  "${REPO_ROOT}"

docker run --rm -i \
  --user "$(id -u):$(id -g)" \
  --env HOME=/tmp/build-home \
  --env BITBAKE_TARGET="${BITBAKE_TARGET}" \
  --env BITBAKE_TEMPLATE="${BITBAKE_TEMPLATE}" \
  --volume "${REPO_ROOT}:/workspace/project-os" \
  --workdir /workspace/project-os \
  "${IMAGE_TAG}" \
  bash -lc "mkdir -p \"\${HOME}\" && \
    repo init -u . -m manifests/default.xml && \
    repo sync -j\"\$(nproc)\" && \
    TEMPLATECONF=\"\${PWD}/layers/meta-project/conf/templates/\${BITBAKE_TEMPLATE}\" \
    . layers/poky/oe-init-build-env \"${BUILD_DIR}\" >/dev/null && \
    bitbake \"\${BITBAKE_TARGET}\""
