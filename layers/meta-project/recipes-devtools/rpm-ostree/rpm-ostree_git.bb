SUMMARY = "rpm-ostree host and target tools"
DESCRIPTION = "rpm-ostree is a hybrid image/package system built on OSTree and RPM. This recipe builds the upstream git tree for both target and native use."
HOMEPAGE = "https://github.com/coreos/rpm-ostree"
LICENSE = "GPL-2.0-or-later & LGPL-2.1-or-later & (Apache-2.0 | MIT)"
SECTION = "devel"

LIC_FILES_CHKSUM = "file://LICENSE;md5=01a124896c40fcd477634ecc07d7efa1"

S = "${WORKDIR}/git"

SRC_URI = " \
    gitsm://github.com/coreos/rpm-ostree;branch=main;protocol=https;name=rpm_ostree \
    git://github.com/containers/composefs-rs;protocol=https;nobranch=1;name=composefs_rs;destsuffix=composefs-rs;type=git-dependency \
    file://0001-configure-remove-polkit-dependency.patch \
    file://0002-Makefile-add-builddir-src-lib-to-include-path.patch \
    file://0003-libmain-guard-ostree_sepolicy_set_null_log.patch \
    file://0004-daemon-add-polkit-compat-shim-header.patch \
    file://0005-daemon-use-polkit-compat-header.patch \
    file://0006-cargo-lock-pin-composefs-rs-msrv.patch \
"

SRCREV_FORMAT = "rpm_ostree_composefs_rs"
SRCREV_rpm_ostree = "7e2f2065a4aa4d5965b4537bb7d74e0b2898650e"
SRCREV_composefs_rs = "651b47f1396bf50d35043351d140ce79846b7e46"

inherit autotools bash-completion cargo cargo-update-recipe-crates gettext pkgconfig rust-target-config

BASEDEPENDS:append = " cargo-native"

DEPENDS = " \
    attr \
    bash-completion \
    cmake \
    curl \
    expat \
    glib-2.0 \
    glib-2.0-native \
    ostree \
    python3-native \
    python3-packaging-native \
    json-c \
    json-glib \
    libarchive \
    libcap \
    libcheck \
    libmodulemd \
    librepo \
    libsolv \
    zstd \
    ostree \
    openssl \
    rpm \
    sqlite3 \
    systemd \
    util-linux \
"

# require ${BPN}-crates.inc

export RUST_BACKTRACE = "full"
export RUSTFLAGS
export RUST_TARGET = "${RUST_HOST_SYS}"

EXTRA_OECONF = " \
    --disable-bin-unit-tests \
    --disable-silent-rules \
    --disable-werror \
"

do_configure() {
	NOCONFIGURE=1 ${S}/autogen.sh
	cargo_common_do_configure
	oe_runconf
}

do_compile:prepend() {
    # gdbus-codegen uses #!/usr/bin/env python3; prepend the native sysroot
    # python3-native directory to PATH so it resolves to the native Python
    # (which has all required modules) rather than the host Python.
    export PATH="${RECIPE_SYSROOT_NATIVE}/usr/bin/python3-native:${PATH}"

        # libdnf expects either LibSolvConfig.cmake or a FindLibSolv.cmake module.
        # In this Yocto sysroot, only pkg-config metadata is available, so provide
        # a minimal find-module compatible with libdnf's expected variables.
        if [ ! -f ${S}/libdnf/cmake/modules/FindLibSolv.cmake ]; then
                cat > ${S}/libdnf/cmake/modules/FindLibSolv.cmake <<'EOF'
include(FindPackageHandleStandardArgs)
find_package(PkgConfig QUIET)
if(PKG_CONFIG_FOUND)
    pkg_check_modules(PC_LIBSOLV QUIET libsolv)
endif()

find_path(LIBSOLV_INCLUDE_DIR
    NAMES solv/pool.h
    HINTS ${PC_LIBSOLV_INCLUDEDIR} ${PC_LIBSOLV_INCLUDE_DIRS}
)

find_library(LIBSOLV_LIBRARY
    NAMES solv
    HINTS ${PC_LIBSOLV_LIBDIR} ${PC_LIBSOLV_LIBRARY_DIRS}
)

find_library(LIBSOLV_EXT_LIBRARY
    NAMES solvext
    HINTS ${PC_LIBSOLV_LIBDIR} ${PC_LIBSOLV_LIBRARY_DIRS}
)

set(LibSolv_VERSION ${PC_LIBSOLV_VERSION})
set(LIBSOLV_INCLUDE_DIRS ${LIBSOLV_INCLUDE_DIR})

find_package_handle_standard_args(LibSolv
    REQUIRED_VARS LIBSOLV_LIBRARY LIBSOLV_EXT_LIBRARY LIBSOLV_INCLUDE_DIR
    VERSION_VAR LibSolv_VERSION
)

mark_as_advanced(LIBSOLV_INCLUDE_DIR LIBSOLV_LIBRARY LIBSOLV_EXT_LIBRARY)
EOF
        fi

    # Drop any stale explicit cargo target linker stanza from prior experiments.
    sed -i '/^\[target\.x86_64-unknown-linux-gnu\]/,+1d' ${WORKDIR}/cargo_home/config || true

    # Force composefs-rs git dependencies onto the compatible checkout fetched by
    # BitBake so cargo does not resolve the upstream 1.88-only revision.
    if ! grep -q '^\[patch\."https://github.com/containers/composefs-rs"\]' ${WORKDIR}/cargo_home/config; then
        cat >> ${WORKDIR}/cargo_home/config <<EOF

[patch."https://github.com/containers/composefs-rs"]
composefs = { path = "${WORKDIR}/composefs-rs/crates/composefs" }
composefs-boot = { path = "${WORKDIR}/composefs-rs/crates/composefs-boot" }
composefs-oci = { path = "${WORKDIR}/composefs-rs/crates/composefs-oci" }
EOF
    fi

    # libdnf currently requires librepo>=1.18.0, but this distro ships 1.17.x.
    # Relax the cmake/pkg-config gate to the available ABI-compatible version.
    sed -i 's/pkg_check_modules(REPO REQUIRED librepo>=1\.18\.0)/pkg_check_modules(REPO REQUIRED librepo>=1.17.0)/' \
        ${S}/libdnf/CMakeLists.txt || true

        # libdnf switched to LRO_USERNAME/LRO_PASSWORD, which are unavailable in
        # librepo 1.17.x from this distro. Rewrite the affected snippets to use
        # LRO_USERPWD when the newer options are not present.
        perl -0777 -i -pe 's|// setup username and password\n\s*auto & username = conf->username\(\)\.getValue\(\);\n\s*if \(!lr_handle_setopt\(priv->repo_handle, error, LRO_USERNAME, username\.empty\(\) \? NULL : username\.c_str\(\)\)\)\n\s*return FALSE;\n\s*auto & password = conf->password\(\)\.getValue\(\);\n\s*if \(!lr_handle_setopt\(priv->repo_handle, error, LRO_PASSWORD, password\.empty\(\) \? NULL : password\.c_str\(\)\)\)\n\s*return FALSE;|// setup username and password\n    auto \& username = conf->username().getValue();\n    auto \& password = conf->password().getValue();\n#ifdef LRO_USERNAME\n    if (!lr_handle_setopt(priv->repo_handle, error, LRO_USERNAME, username.empty() ? NULL : username.c_str()))\n        return FALSE;\n    if (!lr_handle_setopt(priv->repo_handle, error, LRO_PASSWORD, password.empty() ? NULL : password.c_str()))\n        return FALSE;\n#else\n    tmp_cstr = NULL;\n    if (!username.empty()) {\n        tmp_str = formatUserPassString(username, password);\n        tmp_cstr = tmp_str.c_str();\n    }\n    if (!lr_handle_setopt(priv->repo_handle, error, LRO_USERPWD, tmp_cstr))\n        return FALSE;\n#endif|s' ${S}/libdnf/libdnf/dnf-repo.cpp || true

        perl -0777 -i -pe 's|// setup username/password if needed\n\s*handleSetOpt\(h, LRO_USERNAME, config\.username\(\)\.getValue\(\)\.empty\(\) \? NULL : config\.username\(\)\.getValue\(\)\.c_str\(\)\);\n\s*handleSetOpt\(h, LRO_PASSWORD, config\.password\(\)\.getValue\(\)\.empty\(\) \? NULL : config\.password\(\)\.getValue\(\)\.c_str\(\)\);|// setup username\/password if needed\n#ifdef LRO_USERNAME\n    handleSetOpt(h, LRO_USERNAME, config.username().getValue().empty() ? NULL : config.username().getValue().c_str());\n    handleSetOpt(h, LRO_PASSWORD, config.password().getValue().empty() ? NULL : config.password().getValue().c_str());\n#else\n    if (!config.username().getValue().empty()) {\n        auto userpwd = formatUserPassString(config.username().getValue(), config.password().getValue());\n        handleSetOpt(h, LRO_USERPWD, userpwd.c_str());\n    }\n#endif|s' ${S}/libdnf/libdnf/repo/Repo.cpp || true

    # libdnf enables gtk-doc by default; disable it to avoid pulling docs toolchain
    # into this cross build, since rust/libdnf-sys only needs the library artifacts.
    sed -i 's/option(WITH_GTKDOC "Enables libdnf GTK-Doc HTML documentation" ON)/option(WITH_GTKDOC "Enables libdnf GTK-Doc HTML documentation" OFF)/' \
        ${S}/libdnf/CMakeLists.txt || true

    # libdnf tests pull in cppunit, which is unnecessary for the embedded build.
    sed -i 's/option(WITH_TESTS "Enables unit tests" ON)/option(WITH_TESTS "Enables unit tests" OFF)/' \
        ${S}/libdnf/CMakeLists.txt || true

    # The git dependency requests ostree Rust bindings for OSTree 2025.3,
    # but this build sysroot provides libostree 2024.5. Lower the requested API
    # feature to the highest compatible version exposed by the available C library.
    ostree_ext_manifest=$(find ${WORKDIR}/cargo_home/git/checkouts -path '*/crates/ostree-ext/Cargo.toml' | head -n 1)
    if [ -n "${ostree_ext_manifest}" ]; then
        sed -i 's/features = \["v2025_3"\]/features = ["v2023_11"]/g' "${ostree_ext_manifest}"
    fi

    # Build without polkit in this distro profile: drop the unconditional Rust
    # system-deps probe for polkit-gobject-1 from Cargo metadata.
    sed -i '/^polkitgobject = { name = "polkit-gobject-1", version = "0" }$/d' ${S}/Cargo.toml || true

    # The Yocto Rust target runtime in this build does not provide a compatible
    # panic_abort setup for the rpm-ostree release profile. Match the bootc
    # workaround and use unwind for this target build.
    sed -i 's/panic = "abort"/panic = "unwind"/' ${S}/Cargo.toml || true

    # When built from Yocto ${B}, librpmostreeinternals.a is generated under
    # ${B}/.libs, while upstream build.rs assumes ${S}/.libs. Allow overriding.
    perl -i -pe 's#^\s*println!\("cargo:rustc-link-search=\{\}/\.libs", cwd\);#    let libdir = std::env::var("RPMOSTREE_INTERNALS_LIBDIR").unwrap_or_else(|_| format!("{}/.libs", cwd));\n    println!("cargo:rustc-link-search={}", libdir);#' ${S}/build.rs || true
    perl -0777 -i -pe 's|println!\(\n\s*"cargo:rerun-if-changed=\{\}/\.libs/librpmostreeinternals\.a",\n\s*cwd\n\s*\);|println!("cargo:rerun-if-changed={}/librpmostreeinternals.a", libdir);|s' ${S}/build.rs || true

    # Older libostree Rust bindings (pre-v2025_2) do not provide Sign::read_sk().
    # Convert commit signing to a direct set_sk() call with a single key blob.
    perl -0777 -i -pe 's|let file = gio::File::for_path\(Path::new\(path\)\);\n\s*let input = file\.read\(gio::Cancellable::NONE\)\?;\n\s*let reader = sign\.read_sk\(&input\);\n\s*while let Some\(bytes\) = reader\.read_blob\(gio::Cancellable::NONE\)\? \{\n\s*let sk = glib::Variant::from_bytes::<&\[u8\]>\(&bytes\);\n\s*sign\.set_sk\(&sk\)\?;\n\s*sign\.commit\(repo, commit, gio::Cancellable::NONE\)\?;\n\s*\}|let file = gio::File::for_path(Path::new(path));\n    let input = file.read(gio::Cancellable::NONE)?;\n    let bytes = input.read_bytes(1024 * 1024, gio::Cancellable::NONE)?;\n    let sk = glib::Variant::from_bytes::<&[u8]>(&bytes);\n    sign.set_sk(&sk)?;\n    sign.commit(repo, commit, gio::Cancellable::NONE)?;|s' ${S}/rust/src/compose.rs || true

    # Yocto's rust-native uses a custom x86_64-linux target. Make sure rustc can
    # resolve the target JSON and matching stdlib shipped in the native sysroot.
    export RUST_TARGET_PATH="${RECIPE_SYSROOT_NATIVE}/usr/lib/rustlib:${RUST_TARGET_PATH}"

    # The cmake crate driving rust/libdnf-sys does not pick up Yocto's sysroot
    # flags reliably during its compiler link checks. Export them explicitly so
    # CMake's try-compile and the final libdnf build both see the target sysroot.
    sysroot_flag="--sysroot=${RECIPE_SYSROOT}"
    export CPPFLAGS="${CPPFLAGS} ${sysroot_flag}"
    export CFLAGS="${CFLAGS} ${sysroot_flag}"
    export CXXFLAGS="${CXXFLAGS} ${sysroot_flag}"
    export LDFLAGS="${LDFLAGS} ${sysroot_flag}"
    export CMAKE_SYSROOT="${RECIPE_SYSROOT}"
    export CMAKE_FIND_ROOT_PATH="${RECIPE_SYSROOT}"
    export CMAKE_ARGS="${CMAKE_ARGS} -DCMAKE_SYSROOT=${RECIPE_SYSROOT} -DCMAKE_FIND_ROOT_PATH=${RECIPE_SYSROOT} -DCMAKE_C_FLAGS=${sysroot_flag} -DCMAKE_CXX_FLAGS=${sysroot_flag} -DCMAKE_EXE_LINKER_FLAGS=${sysroot_flag} -DCMAKE_TRY_COMPILE_TARGET_TYPE=STATIC_LIBRARY -DWITH_GTKDOC:BOOL=0"

    # Keep glibc linker scripts in the canonical Yocto sysroot form. With
    # --sysroot, ld expects /usr/lib/... entries here, not full workdir paths.
    if [ -f ${RECIPE_SYSROOT}/usr/lib/libm.so ]; then
        sed -i "s# ${RECIPE_SYSROOT}/usr/lib/libm.so.6 # /usr/lib/libm.so.6 #g; s#( ${RECIPE_SYSROOT}/usr/lib/libmvec.so.1 )#( /usr/lib/libmvec.so.1 )#g" \
            ${RECIPE_SYSROOT}/usr/lib/libm.so || true
    fi

    # libc linker script needs the same canonical form for cross ld.
    if [ -f ${RECIPE_SYSROOT}/usr/lib/libc.so ]; then
        sed -i "s# ${RECIPE_SYSROOT}/usr/lib/libc.so.6 # /usr/lib/libc.so.6 #g; s# ${RECIPE_SYSROOT}/usr/lib/libc_nonshared.a # /usr/lib/libc_nonshared.a #g; s# ${RECIPE_SYSROOT}/usr/lib/ld-linux-x86-64.so.2 # /usr/lib/ld-linux-x86-64.so.2 #g" \
            ${RECIPE_SYSROOT}/usr/lib/libc.so || true
    fi

    cat > ${B}/cargo-wrapper <<EOF
#!/bin/sh
# Pass all args through to cargo but inject the source manifest path.
export RUST_TARGET_PATH="${RECIPE_SYSROOT_NATIVE}/usr/lib/rustlib:${RUST_TARGET_PATH}"
export CPPFLAGS="$CPPFLAGS"
export CFLAGS="$CFLAGS"
export CXXFLAGS="$CXXFLAGS"
export LDFLAGS="$LDFLAGS"
export CMAKE_SYSROOT="$CMAKE_SYSROOT"
export CMAKE_FIND_ROOT_PATH="$CMAKE_FIND_ROOT_PATH"
export CMAKE_ARGS="$CMAKE_ARGS"
exec ${RECIPE_SYSROOT_NATIVE}/usr/bin/cargo "\$@" \
    --manifest-path ${S}/Cargo.toml \
    --target ${HOST_SYS}
EOF
    chmod +x ${B}/cargo-wrapper

    # libdnf-sys uses the cmake crate under target/release/build. Reusing a
    # partially configured directory can skip configure and then fail with
    # "No rule to make target 'Makefile'". Force a clean reconfigure.
    find ${B}/target/release/build -maxdepth 3 -type d -path '*/libdnf-sys-*/out/build' -exec rm -rf {} + || true

}

do_compile() {
    export CARGO_NET_GIT_FETCH_WITH_CLI=true
    export CARGO_BUILD_TARGET="${HOST_SYS}"
    export RPMOSTREE_INTERNALS_LIBDIR="${B}/.libs"

    # Cargo materializes git checkouts lazily. Prefetch first so the
    # ostree-ext manifest exists, then lower the OSTree API feature to match
    # the libostree version provided by this distro.
    ${RECIPE_SYSROOT_NATIVE}/usr/bin/cargo fetch --manifest-path ${S}/Cargo.toml --target ${HOST_SYS}
    find ${WORKDIR}/cargo_home/git/checkouts -path '*/crates/ostree-ext/Cargo.toml' -exec \
        sed -i 's/features = \["v2025_3"\]/features = ["v2023_11"]/g' {} + || true

    oe_runmake cargo="${B}/cargo-wrapper"
}

do_compile[network] = "1"
CARGO_DISABLE_BITBAKE_VENDORING = "1"

BBCLASSEXTEND = "native"

INSANE_SKIP:${PN} += "buildpaths"
INSANE_SKIP:${PN}-dbg += "buildpaths"
