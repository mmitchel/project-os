require image-project.inc

INITRAMFS_SCRIPTS:append = " \
    ${@bb.utils.contains_any('DISTRO_FEATURES', 'tpm tpm2', 'initramfs-module-tpm', '', d)} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'sota', 'initramfs-module-ostree', '', d)} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'luks', 'initramfs-module-luks', '', d)} \
    "

# Include Plymouth in initramfs when the distro enables the 'plymouth' feature.
# - plymouth: core daemon and themes
# - plymouth-initrd: initrd helper binaries under ${libexecdir}/plymouth
PACKAGE_INSTALL:append = " \
    ${@bb.utils.contains('DISTRO_FEATURES', 'plymouth', ' plymouth plymouth-initrd', '', d)} \
    "
