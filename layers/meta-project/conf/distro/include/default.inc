# Use GCC as the primary compiler toolchain for all recipes.
TOOLCHAIN = "gcc"

# Security and platform feature group — enables layer sanity checks for these
# features and pulls in the relevant packages:
#   integrity      - IMA/EVM file integrity measurement
#   security       - general security hardening (e.g. PAX, SSP)
#   tpm            - TPM 1.2 hardware support
#   tpm2           - TPM 2.0 hardware support
#   virtualization - container and hypervisor support (KVM, namespaces, cgroups)
DISTRO_FEATURES:append = " integrity security tpm tpm2 virtualization"

# General-purpose distro features enabled across all project images.
# These control which subsystems and middleware stacks are built and installed:
#   acl                       - POSIX access control list support
#   alsa                      - ALSA sound API and libraries
#   bluetooth                 - BlueZ Bluetooth stack
#   directfb                  - DirectFB framebuffer graphics library
#   gobject-introspection-data - GObject introspection typelib files (needed by GI consumers at runtime)
#   ipsec                     - IPsec kernel and userspace support
#   ipv4 / ipv6               - IP networking stacks
#   keyboard                  - keyboard input support
#   nfc                       - Near Field Communication support
#   nfs                       - NFS client/server support
#   overlayfs                 - OverlayFS union mount support (used by containers)
#   pam                       - Pluggable Authentication Modules
#   pci                       - PCI bus support
#   pni-names                 - Predictable network interface names
#   polkit                    - PolicyKit privilege management
#   pulseaudio                - PulseAudio sound server
#   seccomp                   - seccomp system call filtering
#   smbfs                     - Samba/CIFS filesystem support
#   usbgadget / usbhost       - USB device and host support
#   vfat                      - FAT/VFAT filesystem (EFI ESP, removable media)
#   wifi                      - Wi-Fi support
#   xattr                     - Extended filesystem attributes
#   zeroconf                  - mDNS/DNS-SD (Avahi) zero-configuration networking
DISTRO_FEATURES:append = " acl alsa bluetooth directfb gobject-introspection-data ipsec ipv4 ipv6 keyboard nfc nfs overlayfs pam pci pni-names polkit pulseaudio seccomp smbfs usbgadget usbhost vfat wifi xattr zeroconf"

# Graphics and display stack.
#   wayland  - Wayland display protocol and compositor support
#   x11      - X11 display server support
#   opengl   - OpenGL (GLES/EGL) support
#   opencl   - OpenCL GPU compute support
#   vulkan   - Vulkan graphics API support
DISTRO_FEATURES:append = " wayland x11 opengl opencl vulkan"

DISTRO_FEATURES:append:raspberrypi5 = " vc4graphics"

# Remove legacy init system and deprecated 3G modem support.
# sysvinit is replaced by systemd; 3g is not used in project images.
DISTRO_FEATURES:remove = " sysvinit 3g"

# Use systemd as the init system.
DISTRO_FEATURES:append = " systemd"
# Suppress the sysvinit backfill warning since we intentionally replaced it.
DISTRO_FEATURES_BACKFILL_CONSIDERED += "sysvinit"

# Boot splash configuration.
# Keep the existing image feature name "splash", but resolve virtual/psplash
# to plymouth so system startup splash rendering is handled by Plymouth.
# This avoids pulling in psplash when both are available providers.
# DISTRO_FEATURES:append = " plymouth"
# PREFERRED_PROVIDER_virtual/psplash = "plymouth"

# Virtual runtime provider selections — map abstract capability names to the
# specific packages that fulfil them for this distro.
# base-utils: core POSIX utilities
VIRTUAL-RUNTIME_base-utils = "packagegroup-core-base-utils"
# base-utils-hwclock: hardware clock utility
VIRTUAL-RUNTIME_base-utils-hwclock = "util-linux-hwclock"
# base-utils-syslog: syslog daemon
VIRTUAL-RUNTIME_base-utils-syslog = "syslog-ng"
# dev_manager: udev device manager (provided via systemd)
VIRTUAL-RUNTIME_dev_manager = "systemd"
# graphical_init_manager: graphical display/login manager
VIRTUAL-RUNTIME_graphical_init_manager = "gdm"
# init_manager: PID 1 init manager
VIRTUAL-RUNTIME_init_manager = "systemd"
# initscripts: SysV initscript compatibility shims
VIRTUAL-RUNTIME_initscripts = "systemd-compat-units"
# login_manager: login and password utilities
VIRTUAL-RUNTIME_login_manager = "shadow-base"
# net_manager: network manager (systemd-networkd)
VIRTUAL-RUNTIME_net_manager = "systemd"
# syslog: syslog implementation
VIRTUAL-RUNTIME_syslog = "syslog-ng"
# container_engine: OCI container engine
VIRTUAL-RUNTIME_container_engine = "podman"
# container_runtime: low-level OCI container runtime
VIRTUAL-RUNTIME_container_runtime = "virtual-runc"
# container_networking: Container Network Interface plugin host
VIRTUAL-RUNTIME_container_networking = "cni"
# container_orchestration: lightweight Kubernetes orchestrator
VIRTUAL-RUNTIME_container_orchestration = "k3s"
# cri: CRI implementation for Kubernetes
VIRTUAL-RUNTIME_cri = "virtual-containerd"
# cni: CNI plugin runtime
VIRTUAL-RUNTIME_cni = "cni"

# Preferred runtime provider for network configuration (systemd-networkd DHCP).
PREFERRED_RPROVIDER_network-configuration = "networkd-dhcp-conf"
# Preferred SSH server implementation.
PREFERRED_RPROVIDER_ssh = "openssh"

# Start systemd in multi-user (non-graphical) mode by default.
# Individual images or machines can override this to graphical.target.
SYSTEMD_DEFAULT_TARGET =  "${@bb.utils.contains('DISTRO_FEATURES', 'x11', 'graphical.target', 'multi-user.target', d)}"

# NetworkManager integration: use symlinks for DNS resolver configuration
# so /etc/resolv.conf is managed by the active network manager.
NETWORK_MANAGER = "networkmanager"
NETWORKMANAGER_DNS_RC_MANAGER_DEFAULT = "symlink"

# Embed build metadata (date, layers, config) into each image for traceability.
INHERIT += "image-buildinfo"
# Delete recipe work directories after packaging to reclaim disk space.
INHERIT += "rm_work"
# Record the repo manifest snapshot that produced each image.
INHERIT += "project-repo-manifest-native"
# Apply project-wide rootfs post-processing hooks (package cleanup, factory /var seed, etc.).
INHERIT += "project-rootfs-postcommands"
# Fix cross-arch SPDX namespace symlinks when PACKAGE_ARCH differs between builds
# (e.g. armv8a_tegra sstate reused in a raspberrypi5/cortexa76 image build).
INHERIT:append = " project-create-spdx"
INHERIT:remove:class-native = "project-create-spdx"

# Remove OSTree push/sign/check image types — signing and upload are handled
# separately outside of BitBake image tasks.
IMAGE_FSTYPES:remove = "ostreepush garagesign garagecheck"
# Do not append a timestamp suffix to image file names.
IMAGE_NAME_SUFFIX = ""

IMAGE_ROOTFS_EXTRA_SPACE = "2097152"

# Downgrade "rmdir failed" messages from meta-updater to NOTE level to reduce
# build log noise; these are expected when OSTree checkout dirs are pruned.
OSTREE_RMDIR_HELPER_MSGTYPE ?= "bbnote"

# Add the preferred SSH server package to every image's feature set so remote
# access is available out of the box.
EXTRA_IMAGE_FATURES:append = " ssh-server-${PREFERRED_RPROVIDER_ssh}"

IMAGE_INSTALL:append:sota = " ostree-booted"

SANITY_TESTED_DISTROS:append = " zorin-18"

VOLATILE_LOG_DIR = "0"
