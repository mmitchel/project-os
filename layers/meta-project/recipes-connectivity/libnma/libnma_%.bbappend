# Disable Vala API generation because networkmanager is built without vala
# (PACKAGECONFIG:remove = " vala" in networkmanager_%.bbappend), so libnm.vapi
# is never installed. Without it, vapigen fails with:
#   error: Package `libnm' not found in specified Vala API directories
EXTRA_OEMESON:append = " -Dvapi=false"
