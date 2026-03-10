# libqb configure probes require GNU date to convert epoch to UTC.
# Ensure coreutils-native is available for both target and native builds.
DEPENDS:append = " coreutils-native"
