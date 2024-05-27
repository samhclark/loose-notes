#!/usr/bin/env sh

set -e
set -u

disk="${1}"

dd \
    if=/dev/zero \
    ibs=4k \
    iflag=fullblock \
    of="${disk}" \
    obs=64k \
    oflag=direct \
    conv=fsync \
    status=progress 

