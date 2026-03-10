SUMMARY = "Generate Secure Boot PK/KEK/DB key material"
DESCRIPTION = "Generates PK, KEK, and db key/cert bundles and optionally ESL/AUTH enrollment files. Deploys them under DEPLOY_DIR_IMAGE/secureboot."
LICENSE = "MIT"
LIC_FILES_CHKSUMS = "file://${COMMON_LICENSE_DIR}/MIT;md5=801f80980d171dd6425910833169e063"

inherit deploy

S = "${WORKDIR}"

DEPENDS = "openssl-native efitools-native"

SECUREBOOT_KEYS_OUTDIR ?= "${B}/secureboot"
SECUREBOOT_KEYS_DEPLOY_DIR ?= "${DEPLOY_DIR_IMAGE}/secureboot"
SECUREBOOT_KEYS_VALID_DAYS ?= "3650"

# Optional override to keep a stable owner GUID across builds.
SECUREBOOT_KEYS_GUID ?= "f3fd72ab-faf2-4abb-ac6a-a59fc75e7452"

SECUREBOOT_PK_CN ?= "Secure Boot PK"
SECUREBOOT_KEK_CN ?= "Secure Boot KEK"
SECUREBOOT_DB_CN ?= "Secure Boot DB"

do_configure[noexec] = "1"
do_install[noexec] = "1"

create_signing_cert() {
    name="$1"
    cn="$2"

    openssl genrsa -out "${SECUREBOOT_KEYS_OUTDIR}/${name}.key" 2048
    openssl req -new -x509 -sha256 \
        -key "${SECUREBOOT_KEYS_OUTDIR}/${name}.key" \
        -out "${SECUREBOOT_KEYS_OUTDIR}/${name}.crt" \
        -days "${SECUREBOOT_KEYS_VALID_DAYS}" \
        -subj "/CN=${cn}/" \
        -addext "basicConstraints=critical,CA:FALSE" \
        -addext "keyUsage=digitalSignature" \
        -addext "extendedKeyUsage=codeSigning"
    openssl x509 -in "${SECUREBOOT_KEYS_OUTDIR}/${name}.crt" -out "${SECUREBOOT_KEYS_OUTDIR}/${name}.cer" -outform DER
}

do_compile() {
    install -d "${SECUREBOOT_KEYS_OUTDIR}"

    if [ -n "${SECUREBOOT_KEYS_GUID}" ]; then
        OWNER_GUID="${SECUREBOOT_KEYS_GUID}"
    elif [ -f "${SECUREBOOT_KEYS_OUTDIR}/OWNER_GUID" ]; then
        OWNER_GUID="$(tr -d '[:space:]' < "${SECUREBOOT_KEYS_OUTDIR}/OWNER_GUID")"
    else
        OWNER_GUID="$(cat /proc/sys/kernel/random/uuid | tr '[:upper:]' '[:lower:]')"
    fi
    printf '%s\n' "${OWNER_GUID}" > "${SECUREBOOT_KEYS_OUTDIR}/OWNER_GUID"

    create_signing_cert PK "${SECUREBOOT_PK_CN}"
    create_signing_cert KEK "${SECUREBOOT_KEK_CN}"
    create_signing_cert db "${SECUREBOOT_DB_CN}"

    # Generate ESL and AUTH files using efitools
    cert-to-efi-sig-list -g "${OWNER_GUID}" "${SECUREBOOT_KEYS_OUTDIR}/PK.crt" "${SECUREBOOT_KEYS_OUTDIR}/PK.esl"
    cert-to-efi-sig-list -g "${OWNER_GUID}" "${SECUREBOOT_KEYS_OUTDIR}/KEK.crt" "${SECUREBOOT_KEYS_OUTDIR}/KEK.esl"
    cert-to-efi-sig-list -g "${OWNER_GUID}" "${SECUREBOOT_KEYS_OUTDIR}/db.crt" "${SECUREBOOT_KEYS_OUTDIR}/db.esl"

    # Recommended auth chain:
    # - PK signed by PK
    # - KEK signed by PK
    # - db signed by KEK
    sign-efi-sig-list -k "${SECUREBOOT_KEYS_OUTDIR}/PK.key" -c "${SECUREBOOT_KEYS_OUTDIR}/PK.crt" PK "${SECUREBOOT_KEYS_OUTDIR}/PK.esl" "${SECUREBOOT_KEYS_OUTDIR}/PK.auth"
    sign-efi-sig-list -k "${SECUREBOOT_KEYS_OUTDIR}/PK.key" -c "${SECUREBOOT_KEYS_OUTDIR}/PK.crt" KEK "${SECUREBOOT_KEYS_OUTDIR}/KEK.esl" "${SECUREBOOT_KEYS_OUTDIR}/KEK.auth"
    sign-efi-sig-list -k "${SECUREBOOT_KEYS_OUTDIR}/KEK.key" -c "${SECUREBOOT_KEYS_OUTDIR}/KEK.crt" db "${SECUREBOOT_KEYS_OUTDIR}/db.esl" "${SECUREBOOT_KEYS_OUTDIR}/db.auth"
}

do_deploy() {
    install -d "${SECUREBOOT_KEYS_DEPLOY_DIR}"
    cp -f "${SECUREBOOT_KEYS_OUTDIR}"/* "${SECUREBOOT_KEYS_DEPLOY_DIR}/"

    # Also install to static location for uefi-sign.bbclass parse-time file checks
    # install -d "${TOPDIR}/../layers/meta-project/files/secureboot"
    # cp -f "${SECUREBOOT_KEYS_OUTDIR}"/* "${TOPDIR}/../layers/meta-project/files/secureboot/"
}

addtask deploy after do_compile before do_build
