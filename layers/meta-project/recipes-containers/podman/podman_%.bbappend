inherit useradd
USERADD_PACKAGES = "${PN}"
GROUPADD_PARAM:${PN} = "-r docker"

PACKAGECONFIG:remove = "docker"
