KERNEL_MODULES_TPM_I2C = " \
    kernel-module-tpm-i2c-atmel \
    kernel-module-tpm-i2c-infineon \
    kernel-module-tpm-i2c-nuvoton \
    kernel-module-tpm-st33zp24 \
    kernel-module-tpm-st33zp24-i2c \
"

RDEPENDS:packagegroup-security-tpm-i2c = " \
    ${@bb.utils.contains('MACHINE_FEATURES', 'tpm', 'packagegroup-security-tpm', '', d)} \
    ${@bb.utils.contains('MACHINE_FEATURES', 'tpm2', 'packagegroup-security-tpm2', '', d)} \
    ${@bb.utils.contains_any('MACHINE_FEATURES', 'tpm tpm2', '${KERNEL_MODULES_TPM_I2C}', '', d)} \
    "
