FILESEXTRAPATHS:prepend := "${THISDIR}/${BPN}:"

# Use upstream Yocto kernel metadata cache so generic linux-yocto feature paths
# like cfg/efi.scc and cfg/fs/vfat.scc resolve for this tegra kernel recipe.
KMETA = "kernel-meta"
SRCREV_meta ?= "18d7c49a9ff3d86a6d0f8fa3a2682b729360b80a"

SRC_URI:append = " \
	git://git.yoctoproject.org/yocto-kernel-cache.git;type=kmeta;name=meta;branch=yocto-5.15;destsuffix=${KMETA};protocol=https \
	file://nfs-root.cfg \
"
