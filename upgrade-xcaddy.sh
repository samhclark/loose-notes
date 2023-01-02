#!/usr/bin/env sh

set -eu

[ $(/usr/bin/id -u) -ne 0 ] &&  printf "Must be run as root\n" && exit 1

cp /usr/local/bin/xcaddy /tmp/xcaddy
PREV_VERSION="$(/tmp/xcaddy version)"

GOBIN=/usr/local/bin go install github.com/caddyserver/xcaddy/cmd/xcaddy@latest

printf "Installed xcaddy %s\nPrevious version (%s) backed up at /tmp/xcaddy\n" \
    "$(/usr/local/bin/xcaddy version)" \
    "$PREV_VERSION"