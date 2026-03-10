# Disable ntpsec to avoid conflict with ntp over /usr/lib/systemd/system/ntpd.service
# The packagegroup-meta-networking-support conditionally adds ntpsec when x11 is in DISTRO_FEATURES
# Keep only ntp as the NTP daemon
RDEPENDS:packagegroup-meta-networking-support:remove = "ntpsec"

# Do not include postfix from packagegroup-meta-networking-daemons.
RDEPENDS:packagegroup-meta-networking-daemons:remove = "postfix"
