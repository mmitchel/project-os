SUMMARY = "Mount and manage LUKS-backed ext2/3/4 images via FUSE"
HOMEPAGE = "https://github.com/paolovolpi/fuse-luks"
DESCRIPTION = "Userspace tools to mount, format, check, and tune LUKS-backed ext2/3/4 filesystems without root."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${OEROOT}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

SRC_URI = "git://github.com/paolovolpi/fuse-luks.git;branch=main;protocol=https"
SRCREV = "b7e7cd9837537cc4905f402fba727a3e7e994345"

S = "${WORKDIR}/git"

inherit pkgconfig

DEPENDS = "fuse3 e2fsprogs cryptsetup openssl util-linux"

do_compile() {
    oe_runmake
}

do_install() {
    install -D -m 0755 ${S}/fuse-luks ${D}${bindir}/fuse-luks
    install -D -m 0755 ${S}/fuse-luks-mkfs ${D}${bindir}/fuse-luks-mkfs
    install -D -m 0755 ${S}/fuse-luks-fsck ${D}${bindir}/fuse-luks-fsck
    install -D -m 0755 ${S}/fuse-luks-tune ${D}${bindir}/fuse-luks-tune
}

BBCLASSEXTEND = "native"
