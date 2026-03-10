DESCRIPTION = "Unified Kernel Image build using ukify"
LICENSE = "MIT"
DEPENDS = "systemd-boot-native virtual/kernel"

KERNEL_CMDLINE ??= "${APPEND} rw quiet"

do_compile() {
    ukify build \
        --linux ${KERNEL_IMAGE} \
        --initrd ${DEPLOY_DIR_IMAGE}/${INITRAMFS_IMAGE_NAME}.cpio.gz \
        --cmdline "${KERNEL_CMDLINE}" \
        --output ${B}/vmlinuz.efi
}

do_install() {
    install -Dm0644 ${B}/vmlinuz.efi ${D}/boot/vmlinuz.efi
}

FILES:${PN} += "/boot/vmlinuz.efi"
