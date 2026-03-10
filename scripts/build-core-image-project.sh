#!/usr/bin/env bash
set -o errexit   # Exit on any command failure
set -o errtrace  # Ensure ERR trap is inherited by functions and subshells
set -o pipefail  # Catch errors in piped commands
# set -o nounset   # Treat unset variables as errors

# Trap ERR to print the line number and command that failed
trap 'echo "Error at line $LINENO: \"$BASH_COMMAND\""; exit 1' ERR

targets="core-image-project core-image-project-sdk"

for template in machine-qemux86-64 # machine-qemuarm64 machine-raspberrypi5 machine-jetson-orin-nano-devkit-nvme
do
# {
  pushd .
  rm -rf build/conf build/*.lock build/*.sock *.log
  TEMPLATECONF=../../layers/meta-project/conf/templates/$template \
    source layers/poky/oe-init-build-env build
  bitbake $targets
  popd
# } | tee -a /dev/null
done
