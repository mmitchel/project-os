# Geary embeds absolute build paths in shipped binaries for this release.
# Skip buildpaths QA for the affected output packages.
INSANE_SKIP:${PN} += "buildpaths"
INSANE_SKIP:${PN}-dbg += "buildpaths"
