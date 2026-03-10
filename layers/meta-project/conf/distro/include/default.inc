# Disable startup warning on "unapproved" system host OS
SANITY_TESTED_DISTROS:append = " ubuntu-26.04"

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
#   ldconfig                  - run ldconfig to rebuild the dynamic linker cache on target
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
#   usrmerge                  - merge /bin, /sbin, /lib into /usr (required by systemd ≥ 255 and modern distros)
#   vfat                      - FAT/VFAT filesystem (EFI ESP, removable media)
#   wifi                      - Wi-Fi support
#   xattr                     - Extended filesystem attributes
#   zeroconf                  - mDNS/DNS-SD (Avahi) zero-configuration networking
DISTRO_FEATURES:append = " acl alsa bluetooth directfb gobject-introspection-data ipsec ipv4 ipv6 keyboard ldconfig nfc nfs overlayfs pam pci pni-names polkit pulseaudio seccomp smbfs usbgadget usbhost usrmerge vfat wifi xattr zeroconf"

# Graphics and display stack.
#   wayland  - Wayland display protocol and compositor support
#   x11      - X11 display server support
#   opengl   - OpenGL (GLES/EGL) support
#   opencl   - OpenCL GPU compute support
#   vulkan   - Vulkan graphics API support
DISTRO_FEATURES:append = " wayland x11 opengl opencl vulkan"

# vc4graphics: enable the open-source VideoCore IV (VC4/V3D) GPU driver for
# Raspberry Pi 5; required for hardware-accelerated graphics (OpenGL ES/Vulkan)
# via Mesa on RPi5 hardware.
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

# Remove OSTree push/sign/check image types — signing and upload are handled
# separately outside of BitBake image tasks.
IMAGE_FSTYPES:remove = "ostreepush garagesign garagecheck"

# Do not append rootfs.
IMAGE_NAME_SUFFIX = ""

# Do not append a timestamp suffix to image file names.
# IMAGE_VERSION_SUFFIX = ""

# Extra space to reserve in the root filesystem image, in KiB.
# 2097152 KiB = 2 GiB — provides headroom for runtime-generated files,
# package installs, and log accumulation after the image is flashed.
IMAGE_ROOTFS_EXTRA_SPACE = "2097152"

# Additions for sysconfdir/buildinfo
IMAGE_BUILDINFO_VARS:append = " DISTRO_FEATURES MACHINE MACHINE_FEATURES"

# Allows container IMAGE_FSTYPES to contain virtual/kernel without issue of an error.
IMAGE_CONTAINER_NO_DUMMY = "1"

# OTA filesystem image tuning options:
#   -L otaroot   - partition label expected by the OSTree/Aktualizr update stack
#   -i 4096      - bytes-per-inode ratio (ext4 only); reduces inode table overhead
#   -t ext4      - explicit filesystem type selection
#   -n 4096      - node-size in bytes (btrfs only); aligns with page size for
#                  optimal B-tree performance
#   --shrink     - (btrfs only) shrink the image to the minimum size after creation
#   -U <uuid>    - fixed filesystem UUID shared by both image types; OSTree and
#                  the bootloader fstab reference this UUID so it must remain
#                  stable across OTA updates
EXTRA_IMAGECMD:ota-ext4 = "-L otaroot -i 4096 -t ext4 -U ca5a9d06-503c-426f-802d-49908fea5cd1"
EXTRA_IMAGECMD:ota-btrfs = "-L otaroot -n 4096 --shrink -U ca5a9d06-503c-426f-802d-49908fea5cd1"

# Set the ostree repo path
# OSTREE_REPO = "${DEPLOY_DIR}/ostree/${IMAGE_BASENAME}"

# Downgrade "rmdir failed" messages from meta-updater to NOTE level to reduce
# build log noise; these are expected when OSTree checkout dirs are pruned.
OSTREE_RMDIR_HELPER_MSGTYPE = "bbnote"

# Add the preferred SSH server package to every image's feature set so remote
# access is available out of the box.
EXTRA_IMAGE_FATURES:append = " ssh-server-${PREFERRED_RPROVIDER_ssh}"

# For Software Over-The-Air (SOTA/Aktualizr) images, install ostree-booted so
# the running system can mark itself as successfully booted via OSTree, which
# is required to confirm an update and prevent automatic rollback.
IMAGE_INSTALL:append:sota = " ostree-booted"

# Disable the volatile log directory (normally a tmpfs at /var/volatile).
# Setting this to "0" writes logs to persistent storage so they survive a
# reboot, which is required for post-crash and post-update diagnostics.
VOLATILE_LOG_DIR = "0"

# It is recommended to activate "buildhistory" for testing the PR service
# bitbake-prserv-tool import <file>
# bitbake-prserv-tool export <file>
# PRSERV_HOST = "localhost:0"
# INHERIT += "buildhistory"
# BUILDHISTORY_COMMIT = "1"
