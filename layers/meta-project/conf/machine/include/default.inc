# Append project-wide machine features to all machines that include this file.
# These supplement any features already declared by the machine BSP layer.
#
#   alsa          - ALSA sound subsystem support
#   bluetooth     - Bluetooth hardware and stack support
#   keyboard      - Keyboard input device support
#   pci           - PCI bus support
#   qemu-usermode - QEMU user-mode emulation (enables cross-arch package execution in build)
#   rtc           - Real-time clock hardware support
#   screen        - Display/framebuffer output support
#   serial        - Serial port support
#   usbgadget     - USB device/gadget mode support
#   usbhost       - USB host controller support
#   vfat          - FAT/VFAT filesystem support (required for EFI ESP partition)
#   wifi          - Wi-Fi support
# Append project-wide machine features to all machines that include this file.
# These supplement any features already declared by the machine BSP layer.
#
#   alsa          - ALSA sound subsystem support
#   bluetooth     - Bluetooth hardware and stack support
#   keyboard      - Keyboard input device support
#   pci           - PCI bus support
#   qemu-usermode - QEMU user-mode emulation (enables cross-arch package execution in builds)
#   rtc           - Real-time clock hardware support
#   screen        - Display/framebuffer output support
#   serial        - Serial port support
#   usbgadget     - USB device/gadget mode support
#   usbhost       - USB host controller support
#   vfat          - FAT/VFAT filesystem support (required for EFI ESP partition)
#   wifi          - Wi-Fi support
MACHINE_FEATURES:append = " alsa bluetooth keyboard pci qemu-usermode rtc screen serial usbgadget usbhost vfat wifi"
MACHINE_FEATURES:remove = ""

# MACHINE_FEATURES partitioned to conf/machine settings
MACHINE_FEATURES:append:intel-corei7-64 = " efi tpm2"
MACHINE_FEATURES:append:jetson-orin-nano-devkit = " efi"
MACHINE_FEATURES:append:jetson-orin-nano-devkit-nvme = " efi"
MACHINE_FEATURES:append:qemuarm64 = " efi"
MACHINE_FEATURES:append:qemux86-64 = " efi tpm2"
MACHINE_FEATURES:append:raspberrypi5 = " vc4graphics"
