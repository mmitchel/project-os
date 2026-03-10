# DISTRO_FEATURES removes the 3g option which can pull connman. There is an
# RCONFLICTS already between connman and other network management stacks.
RDEPENDS:${PN}:remove = "connman-tools connman-tests connman-client"
