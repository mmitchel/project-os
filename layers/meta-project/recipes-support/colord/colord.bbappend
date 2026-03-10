# colord's cd_create_profile native tool rejects SOURCE_DATE_EPOCH=0 with
# "$SOURCE_DATE_EPOCH invalid 0". Override it with a valid positive epoch
# (1980-01-01 UTC) which satisfies the g_ascii_strtoull != 0 check.
do_compile:prepend() {
    export SOURCE_DATE_EPOCH="315532800"
}
