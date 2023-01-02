#!/usr/bin/env sh

set -eu

[ $(/usr/bin/id -u) -ne 0 ] &&  printf "Must be run as root\n" && exit 1

cp /usr/local/bin/caddy /tmp/caddy
PREV_VERSION="$(/tmp/caddy version)"

CADDY_VERSION=latest /usr/local/bin/xcaddy build \
    --output /usr/local/bin/caddy \
    --with github.com/caddy-dns/cloudflare

printf "Installed caddy version %s\nPrevious version (%s) backed up at /tmp/caddy\n" \
    "$(/usr/local/bin/caddy version)" \
    "$PREV_VERSION"