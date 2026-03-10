FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI += " \
	file://78-mm-sierra-em74xx.rules \
"

# Enable modem protocol support that pulls in libmbim/libqmi dependencies.
PACKAGECONFIG:append = " mbim qmi at"

do_install:append() {
	install -D -m 0644 ${WORKDIR}/78-mm-sierra-em74xx.rules \
		${D}${sysconfdir}/udev/rules.d/78-mm-sierra-em74xx.rules
}

FILES:${PN}:append = " ${sysconfdir}/udev/rules.d/78-mm-sierra-em74xx.rules"
