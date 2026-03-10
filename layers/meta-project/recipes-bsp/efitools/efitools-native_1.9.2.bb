SUMMARY = "Tools to support reading and manipulating the UEFI signature database"
DESCRIPTION = "Provides cert-to-efi-sig-list and sign-efi-sig-list tools for Secure Boot key generation."
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-2.0-only;md5=801f80980d171dd6425610833a22dbe6"

SRC_URI = "git://git.kernel.org/pub/scm/linux/kernel/git/jejb/efitools.git;branch=master;protocol=https \
           file://0001-kernel_efivars-use-mkstemp-for-temporary-file.patch \
           "
SRCREV = "392836a46ce3c92b55dc88a1aebbcfdfc5dcddce"

DEPENDS = "gnu-efi-native openssl-native"

inherit native

S = "${WORKDIR}/git"

def efi_include_arch(d):
    arch = d.getVar("TARGET_ARCH")
    if arch == "x86":
        return "ia32"
    return arch

EXTRA_OEMAKE = "\
    OPENSSL='${STAGING_BINDIR_NATIVE}/openssl' \
    NM='${NM}' AR='${AR}' \
    OPENSSL_LIB='${STAGING_LIBDIR_NATIVE}' \
    EXTRA_LDFLAGS='${LDFLAGS}' \
    CFLAGS='${CFLAGS} -D_XOPEN_SOURCE=700' \
    OBJCOPY='${OBJCOPY}' \
    INCDIR='-I${S}/include -I${STAGING_INCDIR_NATIVE} -I${STAGING_INCDIR_NATIVE}/efi -I${STAGING_INCDIR_NATIVE}/efi/${@efi_include_arch(d)} -I${STAGING_INCDIR_NATIVE}/efi/protocol' \
"
EXTRA_OEMAKE:append:x86-64 = " ARCH=x86_64"
EXTRA_OEMAKE:append:x86 = " ARCH=ia32"
EXTRA_OEMAKE:append:aarch64 = " ARCH=aarch64"
EXTRA_OEMAKE:append:arm = " ARCH=arm"

do_compile() {
    # Build only the cert-to-efi-sig-list and sign-efi-sig-list utilities
    oe_runmake cert-to-efi-sig-list sign-efi-sig-list
}

do_install() {
    install -d ${D}${bindir}
    install -m 755 cert-to-efi-sig-list ${D}${bindir}/
    install -m 755 sign-efi-sig-list ${D}${bindir}/
}
