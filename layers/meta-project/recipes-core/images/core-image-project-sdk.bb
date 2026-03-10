SUMMARY = "Project reference image"
DESCRIPTION = "Minimal project image extending core-image-minimal"
LICENSE = "MIT"

require recipes-core/images/core-image-minimal.bb
require image-project.inc

# Add project packages here as your layer grows.
IMAGE_INSTALL:append = " \
	packagegroup-project-base \
	packagegroup-core-buildessential \
	packagegroup-core-sdk \
	packagegroup-core-tools-debug \
	packagegroup-core-tools-profile \
	packagegroup-core-tools-testapps \
	packagegroup-rust-sdk-target \
	packagegroup-self-hosted \
	"
