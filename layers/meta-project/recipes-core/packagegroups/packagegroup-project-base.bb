SUMMARY = "Project base packagegroup"
DESCRIPTION = "Base package set for project images"

inherit packagegroup

RDEPENDS:${PN} = " \
    ${@bb.utils.contains('MACHINE_FEATURES', 'tpm', 'packagegroup-security-tpm', '', d)} \
    ${@bb.utils.contains('MACHINE_FEATURES', 'tpm2', 'packagegroup-security-tpm2', '', d)} \
    packagegroup-meta-networking \
    packagegroup-container \
    ossec-hids \
    "
