INITRAMFS_SCRIPTS:append = " initramfs-module-tpm initramfs-module-ostree ${@bb.utils.contains('DISTRO_FEATURES', 'luks', 'initramfs-module-luks', '', d)}"

# Include Plymouth in initramfs when the distro enables the 'plymouth' feature.
# - plymouth: core daemon and themes
# - plymouth-initrd: initrd helper binaries under ${libexecdir}/plymouth
PACKAGE_INSTALL:append = "${@bb.utils.contains('DISTRO_FEATURES', 'plymouth', ' plymouth plymouth-initrd', '', d)}"
