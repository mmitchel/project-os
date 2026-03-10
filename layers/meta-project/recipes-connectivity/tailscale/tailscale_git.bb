FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SUMMARY = "wireguard vpn"
DESCRIPTION = "The easiest, most secure way to use WireGuard and 2FA."

GO_IMPORT = "github.com/tailscale/tailscale"

SRC_URI = "git://${GO_IMPORT};branch=release-branch/${SRC_BRANCH};protocol=https;destdir=${BPN}-${SRC_BRANCH}/src/${GO_IMPORT}"

SRC_BRANCH = "1.72"
SRCREV = "f4a95663c8995b0a2362abef64ee91eceec52228"
PV = "${SRC_BRANCH}+git${SRCPV}"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://src/${GO_IMPORT}/LICENSE;md5=a672713a9eb730050e491c92edf7984d"

inherit go-mod systemd

SYSTEMD_SERVICE:${PN} = "tailscaled.service"
SYSTEMD_AUTO_ENABLE:${PN} = "disable"

do_install:append() {
    install -D -m 0644 ${S}/src/${GO_IMPORT}/cmd/tailscaled/tailscaled.service ${D}${systemd_system_unitdir}/tailscaled.service
}

FILES:${PN} += "${GOBIN_FINAL}/ ${systemd_system_unitdir}/"

INSANE_SKIP:${PN}-dev += "file-rdeps"
