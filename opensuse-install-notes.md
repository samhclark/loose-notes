# openSUSE Leap 15.4 Install Notes

Installing and configuring the OS as a NAS/Server.
I want it to have ZFS, Secure Boot, and an encrypted root partiton that is unlocked automatically with the TPM.


## Installing openSUSE

When it's booting, and it's asking you to select "Install openSUSE" or whatever the wording is, press <kbd>E</kbd> to edit the boot options.
On the one that starts with `linux`, add `YAST_LUKS2_AVAILABLE=1` so that you can select LUKS2 as an option later in the process.

Otherwise, this was a breeze.
I configured the static IP and associated hostname (and FQDN) ahead of time on my router.
The installed picked all that up and I got to skip a lot of screens.

### Partitioning

So, here's the layout: 

```
NAME        MAJ:MIN RM   SIZE RO TYPE  MOUNTPOINTS
sda           8:0    0 232.9G  0 disk
├─sda1        8:1    0   512M  0 part  /boot/efi
├─sda2        8:2    0 230.4G  0 part
│ └─cr_root 254:0    0 230.4G  0 crypt /var
│                                      /usr/local
│                                      /tmp
│                                      /srv
│                                      /root
│                                      /opt
│                                      /home
│                                      /.snapshots
│                                      /
└─sda3        8:3    0     2G  0 part  /boot
```

This was easy to create by clicking the Guided Setup button.
Select the right disk.
Tell it to delete all the existing partions, even if not needed. 
Tell it you want `btrfs` for root and that it should be encrypted.
Also, tell it you want swap, but don't bother expanding it to allow hibernation.

After that's done, your setup look like like mine above, kinda.
You still need to click on Expert Partitioning and then delete the swap space and create a new btrfs partion (I chose the type Operating System?).
Make sure it's listed as unencrypted.
Then you want to edit the partition encrypted root partition. 
Don't really need to change anything, but when you continue through it will ask you what kind of encryption and it has "Regular LUKS1" selected.
Change this to "LUKS2", leave everything else the same.

### The rest

I don't think I made any big changes after this.
At some point it asks what kind of install and I picked "Server" (not "Transactional Server").
On the last page it has some options at the top about enabling Secure Boot and Trusted Boot.
I enabled both.

Then that's about it.

## ZFS

Once you're in, getting ZFS is pretty easy. 
Just follow the instructions here. 
Make sure the URL for the repo you add matches the OS you installed. 
At the time, the guide still said 15.3, but I intalled 15.4.

```
zypper addrepo https://download.opensuse.org/repositories/filesystems/15.4/filesystems.repo
zypper refresh
zypper install zfs
```

But since we've got Secure Boot, and you just added a new kernel module, you gotta add its keys:

```
wget https://build.opensuse.org/projects/filesystems/ssl_certificate -O filesystems.cert
openssl x509 -in filesystems.cert -outform der -out cert.der 
mokutil --import cert.der
# when prompted, type in a password that you'll verify in a minute during boot

# make sure the cert is loaded, take note of its expiration date, you'll need to replace it before then
mokutil --list-new
```

### Auto mounting

None of this is applicable to like anyone else, but this is just my own notes so whatever.
The ZFS datasets are all encrypted. 
`tank` is set as the encryption root and all the datasets inherit its encryption info.
This is set with a password that is pulled from a keyfile.
So I have the keyfile present at `/zfs/keys/tank.keyfile`.

The problem is that the existing ZFS systemd units won't be able to mount the datasets without doing `load-key` first.

Fix this by creating your own systemd unit that loads the key.
Here is mine, stolen from here: https://forum.openmediavault.org/index.php?thread/40525-how-to-auto-load-key-and-mount-natively-encrypted-zfs-pool-no-luks/

```
# cat /etc/systemd/system/zfs-load-key@.service
[Unit]
Description=Load ZFS keys
DefaultDependencies=no
Before=zfs-mount.service
After=zfs-import.target
Requires=zfs-import.target

[Service]
Type=oneshot
RemainAfterExit=yes
ExecStart=/usr/sbin/zfs load-key %I

[Install]
WantedBy=zfs-mount.service
```

Enabled with 

```
sudo systemctl enable zfs-load-key@tank.service
```

That's all I needed to get the ZFS stuff to unlock and mount after boot.


## TPM and Auto Unlocking root

This was pretty easy too, honestly.
The instructions here were pretty straightforward: https://en.opensuse.org/SDB:LUKS2,_TPM2_and_FIDO2#Situation

```
zypper install tpm2.0-tools
systemd-cryptenroll --tpm2-device=auto --tpm2-pcrs=7 /dev/disk/by-id/wwn-...-part2
```

Change `/etc/crypttab` so that the device can use the TMP.
That's adding `tpm2-device=auto` to the line that corresponds with that partition. 
Looks like this once it's done: 

```
# cat /etc/crypttab
cr_root  UUID=a1fd3ae8-8b66-45ab-bf58-e2451504f8df  none  x-initrd.attach,tpm2-device=auto
```

Then you gotta rebuild the kernel

```
dracut -f
```

After reboot, it unlocks on its own, and my devices are unlocked and mounted. 
I still need to figure out how to bind the TPM unlock to more PCR registers. 
And I need to figure out how to hook the unenroll/reenroll step into kernel builds.
Not sure if that needs to be done before or after `dracut -f` every time.
Probably before since that's what worked here?

(that's one command btw)

```
sudo systemd-cryptenroll --wipe-slot=tpm2 --tpm2-device=auto --tpm2-pcrs=7 /dev/disk/by-id/wwn-...-part2
```
