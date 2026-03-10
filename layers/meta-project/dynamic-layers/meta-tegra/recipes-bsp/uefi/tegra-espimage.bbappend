# Override EFI installation prefix for tegra-espimage
# The remove_unused_stuff function only keeps /EFI directories, so we need EFI files
# installed directly under /EFI rather than /boot/EFI/BOOT

# Ensure l4t-launcher is used as the EFI provider for Jetson platforms.
# This overrides any machine-level defaults that might use systemd-boot.
EFI_PROVIDER = "l4t-launcher"
EFI_PREFIX = ""
EFIDIR = "/EFI"

# Emit repo manifest metadata for tegra-espimage artifacts.
inherit project-repo-manifest-native
