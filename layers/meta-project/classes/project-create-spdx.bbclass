# project-create-spdx.bbclass
#
# Fixes an upstream limitation in create-spdx-2.2.bbclass where
# image_combine_spdx fatal-errors when an externalDocumentRef in a package's
# SPDX file points to a namespace that was registered under a different
# PACKAGE_ARCH (e.g. a dependency whose sstate was first populated during a
# Tegra build, leaving its by-namespace symlink under armv8a_tegra/ rather
# than cortexa76/ expected by a subsequent raspberrypi5 build).
#
# Fix: override image_combine_spdx to monkey-patch oe.sbom.doc_find_by_namespace
# in memory for the duration of combine_spdx, falling back to a global arch scan
# when the primary SPDX_MULTILIB_SSTATE_ARCHS-scoped lookup misses.
#
# No files are written to DEPLOY_DIR_SPDX so there is no conflict with the
# sstate output manifests of individual do_create_spdx tasks.

python image_combine_spdx() {
    import os
    import oe.sbom
    from pathlib import Path
    from oe.rootfs import image_list_installed_packages

    # Wrap doc_find_by_namespace to fall back to an all-arch scan when the
    # primary SPDX_MULTILIB_SSTATE_ARCHS-scoped lookup misses.  This handles
    # cross-arch sstate reuse where a dependency's namespace symlink was written
    # under a different PACKAGE_ARCH (e.g. armv8a_tegra vs cortexa76).
    _original_find = oe.sbom.doc_find_by_namespace

    def _find_with_fallback(spdx_deploy, search_arches, doc_namespace):
        result = _original_find(spdx_deploy, search_arches, doc_namespace)
        if result is None:
            ns_root = Path(spdx_deploy) / "by-namespace"
            if ns_root.is_dir():
                all_archs = [p.name for p in ns_root.iterdir() if p.is_dir()]
                result = _original_find(spdx_deploy, all_archs, doc_namespace)
                if result:
                    bb.note("project-create-spdx: cross-arch SPDX fallback resolved %s" % doc_namespace)
        return result

    oe.sbom.doc_find_by_namespace = _find_with_fallback
    try:
        image_name = d.getVar("IMAGE_NAME")
        image_link_name = d.getVar("IMAGE_LINK_NAME")
        imgdeploydir = Path(d.getVar("IMGDEPLOYDIR"))
        img_spdxid = oe.sbom.get_image_spdxid(image_name)
        packages = image_list_installed_packages(d)

        combine_spdx(d, image_name, imgdeploydir, img_spdxid, packages, Path(d.getVar("SPDXIMAGEWORK")))

        def make_image_link(target_path, suffix):
            if image_link_name:
                link = imgdeploydir / (image_link_name + suffix)
                if link != target_path:
                    link.symlink_to(os.path.relpath(target_path, link.parent))

        spdx_tar_path = imgdeploydir / (image_name + ".spdx.tar.zst")
        make_image_link(spdx_tar_path, ".spdx.tar.zst")
    finally:
        oe.sbom.doc_find_by_namespace = _original_find
}
