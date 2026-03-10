# uutils coreutils install (used as hosttools/install in this environment) has
# a bug in v0.8.0 where `install -D src dest/file` fails with "cannot create
# directory" when the parent directory does not already exist, instead of
# creating it. The lvm2 scripts/Makefile uses exactly this pattern for
# fsadm_install and other targets:
#
#   %_install: %.sh
#       $(INSTALL_PROGRAM) -D $< $(sbindir)/$(basename $(<F))
#
# where sbindir = $(DESTDIR)/usr/sbin — so the install into image/usr/sbin
# fails if the directory is not pre-created.
#
# Pre-create the directory before make install runs.
do_install:prepend() {
    install -d ${D}${sbindir}
    install -d ${D}${libdir}
    install -d ${D}${libdir}/device-mapper
    install -d ${D}${libdir}/pkgconfig
    install -d ${D}${libdir}/udev/rules.d
    install -d ${D}${includedir}
    install -d ${D}${sysconfdir}/lvm
}
