SUMMARY = "WSL custom distro configuration and first-boot OOBE"
DESCRIPTION = "Installs /etc/wsl-distribution.conf, /etc/oobe.sh, and a Windows Terminal profile template for WSL custom distro images."
HOMEPAGE = "https://learn.microsoft.com/en-us/windows/wsl/build-custom-distro"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI = " \
    file://wsl-distribution.conf \
    file://oobe.sh \
    file://terminal-profile.json \
    file://project-os.ico \
"

S = "${WORKDIR}"

inherit allarch

do_install() {
    install -D -m 0644 ${WORKDIR}/wsl-distribution.conf ${D}${sysconfdir}/wsl-distribution.conf
    install -D -m 0755 ${WORKDIR}/oobe.sh ${D}${sysconfdir}/oobe.sh
    install -D -m 0644 ${WORKDIR}/terminal-profile.json ${D}${libdir}/wsl/terminal-profile.json
    install -D -m 0644 ${WORKDIR}/project-os.ico ${D}${libdir}/wsl/project-os.ico
}

FILES:${PN} += " \
    ${sysconfdir}/wsl-distribution.conf \
    ${sysconfdir}/oobe.sh \
    ${libdir}/wsl/terminal-profile.json \
    ${libdir}/wsl/project-os.ico \
"
