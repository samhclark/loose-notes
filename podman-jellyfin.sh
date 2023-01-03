# But not really a shell script...

# LinuxServer Jellyfin
podman run --detach \
  --name=jellyfin \
  --env=PUID=1000 \
  --env=PGID=1000 \
  --env=TZ=America/Chicago \
  --publish=8096:8096 \
  --volume=/etc/jellyfin:/config:Z \
  --volume=/var/mnt/hostfs/tv-shows:/data/tvshows \
  --volume=/var/mnt/hostfs/movies:/data/movies \
  --device=/dev/dri:/dev/dri \
  --restart=unless-stopped \
  lscr.io/linuxserver/jellyfin:latest

# Plain Jellyfin
podman run --detach \
    --name=jellyfin \
    --tz=America/Chicago \
    --publish=8096:8096 \
    --volume=/etc/jellyfin:/config:Z \
    --volume=/var/mnt/hostfs:/data:rw \
    --device=/dev/dri:/dev/dri \
    --restart=unless-stopped \
    --userns=keep-id:uid=1000,gid=1000 \
    --group-add keep-groups \
    registry.hub.docker.com/jellyfin/jellyfin:latest

# Plain Podman, from Jellyfin's docs
# This one works!
podman run \
    --detach \
    --label "io.containers.autoupdate=registry" \
    --name jellyfin \
    --publish 8096:8096/tcp \
    --rm \
    --user $(id -u):$(id -g) \
    --userns keep-id \
    --security-opt label=disable \
    --volume /var/cache/jellyfin:/cache:Z \
    --volume /etc/jellyfin:/config:Z \
    --mount type=bind,source=/var/mnt/hostfs,destination=/media \
    docker.io/jellyfin/jellyfin:latest