# Disable ntpsec to avoid conflict with ntp over /usr/lib/systemd/system/ntpd.service
# The packagegroup-meta-networking-support conditionally adds ntpsec when x11 is in DISTRO_FEATURES
# Keep only ntp as the NTP daemon
RDEPENDS:packagegroup-meta-networking-support:remove = "ntpsec"

# Do not include postfix from packagegroup-meta-networking-daemons.
RDEPENDS:packagegroup-meta-networking-daemons:remove = "postfix"

# Avoid mdns/libnss-mdns file collision during rootfs construction.
# packagegroup-base-zeroconf pulls libnss-mdns (via DISTRO_FEATURES zeroconf),
# while packagegroup-meta-networking-protocols pulls mdns. Both install
# /usr/lib/libnss_mdns.so.2, causing a DNF transaction test conflict.
# Keep libnss-mdns from packagegroup-base and drop mdns from this packagegroup.
RDEPENDS:packagegroup-meta-networking-protocols:remove = "mdns"

# DISTRO_FEATURES removes the 3g option which can pull connman. There is an
# RCONFLICTS already between connman and other network management stacks.
RDEPEND:packagegroup-meta-networking-connectivity:remove = "connman connman-client"
