SUMMARY = "Project wsl2 packagegroup"
DESCRIPTION = "WSL2 package set for project images"

inherit packagegroup

RDEPENDS:${PN} = " \
    cloud-init \
    wsl2-conf \
    "

EXCLUDE_FROM_WORLD = "1"
