SUMMARY = "Project reference image"
DESCRIPTION = "Minimal project image extending core-image-minimal"
LICENSE = "MIT"

require recipes-core/images/core-image-minimal.bb
require image-project.inc

# Add project packages here as your layer grows.
IMAGE_INSTALL:append = " \
	packagegroup-project-base \
	"
