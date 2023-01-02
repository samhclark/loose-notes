# Caddy Setup

How Caddy is set up on my VM Host as a reverse proxy for the services running in the guest VMs.

There are two permanent firewall rules that redirect ports 80 and 443 to unpriveleged ports:

```
sudo firewall-cmd --permanent --zone=public --add-forward-port=port=80:proto=tcp:toport=8080
sudo firewall-cmd --permanent --zone=public --add-forward-port=port=443:proto=tcp:toport=4443
```

Caddy itself runs as a user-level SystemD service. 
The service is defined at `/usr/systemd/system/caddy.service` along with other user-defined systemd service. 
The contents are mostly copied from [Caddy's GitHub repo](https://github.com/caddyserver/dist/blob/master/init/caddy.service) but I added a ton of systemd sandboxing restrictions. 

```
# caddy.service
#
# For using Caddy with a config file.
#
# Make sure the ExecStart and ExecReload commands are correct
# for your installation.
#
# See https://caddyserver.com/docs/install for instructions.
#
# WARNING: This service does not use the --resume flag, so if you
# use the API to make changes, they will be overwritten by the
# Caddyfile next time the service is restarted. If you intend to
# use Caddy's API to configure it, add the --resume flag to the
# `caddy run` command or use the caddy-api.service file instead.

[Unit]
Description=Caddy
Documentation=https://caddyserver.com/docs/
After=network.target network-online.target
Requires=network-online.target

[Service]
Type=notify
User=caddy
Group=caddy
ExecStart=/usr/local/bin/caddy run --environ --config /etc/caddy/Caddyfile
ExecReload=/usr/local/bin/caddy reload --config /etc/caddy/Caddyfile --force
TimeoutStopSec=5s
CapabilityBoundingSet=
LimitNOFILE=1048576
LimitNPROC=512
LockPersonality=yes
MemoryDenyWriteExecute=yes
NoNewPrivileges=yes
PrivateDevices=yes
PrivateNetwork=no
PrivateTmp=yes
ProcSubset=pid
ProtectClock=yes
ProtectControlGroups=yes
ProtectHome=yes
ProtectHostname=yes
ProtectKernelLogs=yes
ProtectKernelModules=yes
ProtectKernelTunables=yes
ProtectProc=invisible
ProtectSystem=strict
ReadWritePaths=/var/lib/caddy/.local/share/caddy
RemoveIPC=yes
RestrictAddressFamilies=AF_INET AF_INET6 AF_UNIX
RestrictNamespaces=yes
RestrictRealtime=yes
RestrictSUIDSGID=yes
SystemCallErrorNumber=EPERM
SystemCallFilter=@system-service

[Install]
WantedBy=multi-user.target
```

The systemd service relies on a few things. 
It requires that there is a user named `caddy` and a group named `caddy`. 
They were created with these commands: 

```
sudo groupadd --system caddy
sudo useradd --system \
    --gid caddy \
    --create-home \
    --home-dir /var/lib/caddy \
    --shell /usr/sbin/nologin \
    --comment "Caddy web server" \
    caddy
```

It also requires that a Caddyfile exists at the location specified (`/etc/caddy/Caddyfile` in this case).
And it requires that there is a `caddy` binary available. 

The `caddy` binary that I need has to be built with CloudFlare's DNS provider, so I'm building it with `xcaddy`. 
That means there's two binaries in `/usr/local/bin` that I care about and I wrote two accompanying scripts to help keep them updated.

```
$ ls -l /usr/local/bin
total 43908
-rwxr-xr-x 1 root root 40681472 Jan  1 20:45 caddy
-rwxr-xr-x 1 root root      457 Jan  1 20:45 upgrade-caddy.sh
-rwxr-xr-x 1 root root      405 Jan  1 19:55 upgrade-xcaddy.sh
-rwxr-xr-x 1 root root  4271161 Jan  1 19:55 xcaddy
```

The contents of those scripts are available in this repo. 


## Usage

### Upgrading

To upgrade Caddy, you need to build a new upgraded `caddy` binary and then restart the systemd service. 
To build a new version of `caddy`, you want to upgrade `xcaddy` too just to be safe. 

So it would look like this: 

```
sudo upgrade-xcaddy.sh
sudo upgrade-caddy.sh
sudo systemctl restart caddy
```

### Changing config

If you're only changing configuration (the Caddyfile at `/etc/caddy/Caddyfile`) then after making those changes, you only need to *reload* caddy.

That's:

```
sudo systemctl reload caddy
```