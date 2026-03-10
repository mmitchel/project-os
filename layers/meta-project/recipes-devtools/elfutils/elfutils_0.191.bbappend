FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += "file://0001-libcpu-riscv_disasm-Fix-build-with-gcc-15.patch"

# debuginfod is not needed for native tools and can trigger install issues
# around profile.d generation when native paths are absolute in this setup.
PACKAGECONFIG:remove:class-native = "debuginfod libdebuginfod"
