# The base recipe in meta-filesystems already carries the correct build-time
# dependencies from upstream Meson checks: fuse3 and openssl.

# mount.composefs relies on the fuse userspace helper package being available
# on deployed systems.
RDEPENDS:${PN} += "fuse3-utils"
