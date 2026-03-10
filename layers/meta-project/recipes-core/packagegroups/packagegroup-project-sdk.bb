SUMMARY = "Project SDK packagegroup"
DESCRIPTION = "SDK package set for project images"

inherit packagegroup

RDEPENDS:${PN} = " \
    packagegroup-core-standalone-sdk-target \
    "

EXCLUDE_FROM_WORLD = "1"
