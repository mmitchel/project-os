#!/bin/sh

set -eu

DEFAULT_UID=1000
DEFAULT_GROUPS="adm,cdrom,sudo,dip,plugdev"

echo "Please create a default UNIX user account."
echo "The username does not need to match your Windows username."
echo "For more information visit: https://aka.ms/wslusers"

if getent passwd "${DEFAULT_UID}" >/dev/null 2>&1; then
    echo "User account already exists, skipping creation"
    exit 0
fi

while true; do
    printf "Enter new UNIX username: "
    read -r username

    if [ -z "${username}" ]; then
        echo "Username cannot be empty"
        continue
    fi

    if /usr/sbin/adduser --uid "${DEFAULT_UID}" --quiet --gecos "" "${username}"; then
        if /usr/sbin/usermod "${username}" -aG "${DEFAULT_GROUPS}"; then
            echo "Created user '${username}' with UID ${DEFAULT_UID}"
            break
        else
            /usr/sbin/deluser "${username}"
            echo "Failed to add groups, user removed. Try again."
        fi
    fi
done
