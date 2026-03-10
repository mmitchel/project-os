#!/bin/bash
set -euo pipefail

repo init -u "$MANIFEST_URI" -b "$MANIFEST_BRANCH" -m "$MANIFEST_FILE"

repo sync --no-clone-bundle --no-tags -j"$(nproc)"
