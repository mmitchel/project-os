#!/bin/bash
set -eo pipefail

TEMPLATECONF="${GITHUB_WORKSPACE}/layers/meta-project/conf/templates/${BITBAKE_TEMPLATE}" \
  . layers/poky/oe-init-build-env build >/dev/null

bitbake "$BITBAKE_TARGET"
