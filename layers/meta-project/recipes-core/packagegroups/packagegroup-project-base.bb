SUMMARY = "Project base packagegroup"
DESCRIPTION = "Base package set for project images"

inherit packagegroup

RDEPENDS:${PN} = " \
    ${@bb.utils.contains('MACHINE_FEATURES', 'tpm', 'packagegroup-security-tpm', '', d)} \
    ${@bb.utils.contains('MACHINE_FEATURES', 'tpm2', 'packagegroup-security-tpm2', '', d)} \
    packagegroup-core-ssh-openssh \
    packagegroup-meta-networking-connectivity \
    packagegroup-meta-networking-filter \
    packagegroup-meta-networking-kernel \
    packagegroup-meta-networking-protocols \
    packagegroup-meta-networking-support \
    packagegroup-container \
    packagegroup-podman \
    packagegroup-gnome-apps \
    packagegroup-gnome-desktop \
    packagegroup-project-wsl2 \
    "

EXCLUDE_FROM_WORLD = "1"
