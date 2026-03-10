SUMMARY = "Bootable container system"
DESCRIPTION = "bootc provides tools for building and managing bootable container-based operating systems."
HOMEPAGE = "https://github.com/bootc-dev/bootc"
LICENSE = "MIT & Apache-2.0"
LIC_FILES_CHKSUM = " \
    file://LICENSE-MIT;md5=b377b220f43d747efdec40d69fcaa69d \
    file://LICENSE-APACHE;md5=fa818a259cbed7ce8bc2a22d35a464fc \
"

SRC_URI = "git://github.com/bootc-dev/bootc.git;branch=main;protocol=https"
SRCREV = "87e8ac64b933f77f062556821c65fce2c0380ece"

PV = "0.0+git${SRCPV}"
S = "${WORKDIR}/git"
B = "${S}"

CARGO_MANIFEST_PATH = "${S}/Cargo.toml"

inherit pkgconfig systemd cargo cargo-update-recipe-crates

# include bootc-crates.inc

BASEDEPENDS:append = " cargo-native rust-native"

DEPENDS = " \
    openssl \
    ostree \
    systemd \
    zstd \
"

RDEPENDS:${PN} += " \
    bash \
    coreutils \
    ostree \
    podman \
    skopeo \
    systemd \
    util-linux \
    zstd \
"

RRECOMMENDS:${PN} += "composefs"

# Configure Cargo for Yocto cross-compilation/linker settings.
do_configure() {
    # The current source requires ostree Rust bindings for v2025_2,
    # but this layer ships ostree 2024.5. Lower the requested API feature to
    # the highest compatible version exposed by the available C library.
    sed -i 's/features = \["v2025_2"\]/features = ["v2023_11"]/g' ${S}/crates/ostree-ext/Cargo.toml
    # The Yocto Rust target runtime in this build does not provide a compatible
    # panic_abort setup for this workspace profile, so use unwind for release.
    sed -i 's/panic = "abort"/panic = "unwind"/' ${S}/Cargo.toml
    # Older ostree Rust bindings in this layer do not expose SePolicy::set_null_log().
    sed -i '/SePolicy::set_null_log()/d' ${S}/crates/lib/src/cli.rs
    cargo_common_do_configure
}

# bootc currently relies on fetching Rust dependencies from the network.
do_compile[network] = "1"
CARGO_DISABLE_BITBAKE_VENDORING = "1"

CARGO_BUILD_FLAGS = "-v --target ${HOST_SYS} ${BUILD_MODE} --manifest-path=${CARGO_MANIFEST_PATH} --package bootc --package system-reinstall-bootc"

do_compile() {
    cd ${S}
    export CARGO_NET_GIT_FETCH_WITH_CLI=true
    oe_cargo_build
}

do_install() {
    BOOTC_RUST_TARGET="${HOST_SYS}"

    install -d ${D}${bindir}
    install -m 0755 ${B}/target/${BOOTC_RUST_TARGET}/release/bootc ${D}${bindir}/bootc
    install -m 0755 ${B}/target/${BOOTC_RUST_TARGET}/release/system-reinstall-bootc ${D}${bindir}/system-reinstall-bootc

    install -d ${D}${libdir}/bootc
    install -d ${D}${libdir}/bootc/bound-images.d
    install -d ${D}${libdir}/bootc/kargs.d

    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${S}/systemd/*.service ${D}${systemd_system_unitdir}/
    install -m 0644 ${S}/systemd/*.timer ${D}${systemd_system_unitdir}/
    install -m 0644 ${S}/systemd/*.path ${D}${systemd_system_unitdir}/
    install -m 0644 ${S}/systemd/*.target ${D}${systemd_system_unitdir}/

    install -d ${D}${libdir}/systemd/system-generators
    install -m 0755 ${S}/crates/cli/bootc-generator-stub ${D}${libdir}/systemd/system-generators/bootc-systemd-generator
}

SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE:${PN} = "bootc-fetch-apply-updates.timer bootc-fetch-apply-updates.service"

FILES:${PN} += " \
    ${systemd_system_unitdir}/bootc-destructive-cleanup.service \
    ${systemd_system_unitdir}/bootc-publish-rhsm-facts.service \
    ${systemd_system_unitdir}/bootc-status-updated.target \
    ${systemd_system_unitdir}/bootc-status-updated.path \
    ${systemd_system_unitdir}/bootc-status-updated-onboot.target \
    ${libdir}/systemd/system-generators/bootc-systemd-generator \
"

INSANE_SKIP:${PN}-dbg += "buildpaths"
