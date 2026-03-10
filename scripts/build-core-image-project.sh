#!/usr/bin/env bash
set -eo pipefail
set -x

pushd .
rm -rf build/conf build/*.lock build/*.sock
TEMPLATECONF=$(pwd)/layers/meta-project/conf/templates/qemux86-64 \
  . layers/poky/oe-init-build-env build
bitbake core-image-project
popd

pushd .
rm -rf build/conf build/*.lock build/*.sock
TEMPLATECONF=$(pwd)/layers/meta-project/conf/templates/raspberrypi5 \
  . layers/poky/oe-init-build-env build
bitbake core-image-project
popd

pushd .
rm -rf build/conf build/*.lock build/*.sock
TEMPLATECONF=$(pwd)/layers/meta-project/conf/templates/jetson-orin-nano-devkit-nvme \
  . layers/poky/oe-init-build-env build
bitbake core-image-project
popd  
