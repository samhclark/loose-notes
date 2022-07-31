# OpenBSD Install Notes

To be clear if anyone finds this, this is not a guide. 
These are my notes from when I installed it.

## Prep

Downloaded OpenBSD 7.1 from their website.
The full one with all the file sets.
In retrospect, that was maybe not the best idea since I ended up pulling the file sets via HTTP later anyway.
Checked its hash then wrote it to a USB with:

```shell
# dd if=Downloads/install71.img of=/dev/sda bs=1M status=progress
```

Although the `status=progress` part didn't work?
So I gotta look into that or get used to using `pv`. 
Got another small USB ready for the keydisk.

That's about it, I think...

## The Install

Boot from the USB.
Then on the first prompt, like:

```
Welcome to the OpenBSD/amd64 7.1 installation program.
(I)nstall, (U)pgrade, (A)utoinstall or (S)hell?
```

Choose "Shell" since we need to mess with the disks.
Make sure all the disks are ready to use:

```
cd /dev
sh MAKEDEV sd0
sh MAKEDEV sd1
sh MAKEDEV sd2
sh MAKEDEV sd3
sh MAKEDEV sd4
```

`sd0` is the target SSD where we're actually gonna install things.
`sd1` is the USB that has the OpenBSD installed.
`sd2` is the USB that will be used as a keydisk.
`sd3` is the USB that will be used as a _backup_ of our keydisk.
`sd4` doesn't exist yet but will be the encrypted psuedo-device.

Then you kinda follow the OpenBSD FAQ instructions.

We write some random data to the beginning of `sd0` and `sd2`.
I'm not super worried about data recovery since I don't think `dd`ing random data actually erases flash memory anyway.
But I _am_ worried about destroying all the existing partition table info.

```
dd if=/dev/urandom of=/dev/rsd0c bs=1m count=100
dd if=/dev/urandom of=/dev/rsd2c bs=1m count=100
dd if=/dev/urandom of=/dev/rsd3c bs=1m count=100
```

That `rsdXc` thing means "**r**aw **sd0** **c**omplete disk".
The whole disk.
The raw disk.

Anyway, so that's written over.
The FAQ uses the phrase "initialize your disk with `fdisk`".
That phrase describes the next step:

```
fdisk -iy -g -b 960 sd0
```

Why you use both `-i` _and_ `-g`...I have no idea.
I didn't know you could make a disk both MBR and GPT.
Then you use `disklabel` to mess with the partitions

```
disklabel -E sd0
sd0> a a
# That means "add partition 'a'"
# If it complains that the partition already exists 'z' will delete them all.
# But then you need to go back and repeat the `dd` and `fdisk` commands for sd0.
offset: [1024]
size: [2500068623] 50%
FS type: [4.2BSD] RAID
# way under provisioning because the filesystem doesn't do TRIM and this is a 12-year-old SSD
sd0*> w
sd0> q
No label changes.
```

Now we do the same thing for USB that will be our keydisk. 
Well almost the same thing.
The FAQ says, kinda vaguely 

> Initialize your keydisk with fdisk(8), then use disklabel(8) to create a 1 MB RAID partition for the key data.

That means these commands:

```
fdisk -iy sd2
```

Just MBR.
Then:

```
disklabel -E sd2
sd2> a a
offset: [64]
size: [987654321] 1M
FS type: [4.2BSD] RAID
sd2*> w
sd2> q
```

Then you want to set up the encryption.

```
bioctl -c C -k sd2a -l sd0a softraid0
# `-c C` means "RAID level: Crypto"
# the next two you just pay attention to specify partition 'a'
```

It should say something about how the thing is created and attached as sd3.
At this point sd4 is a logical device, psuedo-device. 
You use it and write to it like it's plain, but it'll be encrypted and written to sd0.

Make a copy of the keydisk.

```
fdisk -iy sd3
disklabel -E sd3
sd2> a a
offset: [64]
size: [987654321] 1M
FS type: [4.2BSD] RAID
sd2*> w
sd2> q
dd bs=8192 seek=1 skip=1 if=/dev/rsd2a of=/dev/rsd3a
```

Now you `exit` the shell and it will return you to the installer.

`I` to proceed with the normal install.
Most of it is defaults.
Pick a hostname.
Timezone is `America/Chicago`.
Yes to IPv4 and let it autoconfigure. 
No to IPv6 (shout out US Internet for not supporting it...)
No to root login.
Yes create a new user (should automatically add them to `wheel` if you do it now).

When you get to the disk part, that's the important part.
It asks "which disk do you want to set up" or something like that and **you need to choose sd4**. 

```
Which disk is the root disk? ('?' for details) [sd0] sd4
```

Choose whole disk GPT.
You can let the installer auto partition it.
Then uhh, that's about it. 
Choose all the file sets and get em with http. 

## Post Install

TODO is fix the Alacritty term info stuff.
In the meantime, do this before connecting with SSH

```
TERM=xterm-256color ssh user@openbsd
```

First thing's first, we gotta let `wheel` do root stuff

```
$ su root
# cp /etc/examples/doas.conf /etc/doas.conf
```

While we're `root` change the root password to something harder.
I know you set it to something easy during install, you had to type it. 
But change it to something random with your password manager.

```
# passwd root
# exit
$ 
```

Now we're looking good.
Let's update

```
doas syspatch
```

Turn the system off with

```
doas shutdown -hp now
```

Repeat as needed.
`syspatch` might need to update itself before installing patches.

Go throgh the rest of the `afterboot` steps just to jog your memory here.

We're gonna do the rest with Ansible so let's wrap up.
Make sure that your SSH public keys are on the router in `authorized_keys`. 
Make sure you disabled password login in `/etc/ssh/sshd_config`.
Install Python because Ansible needs it:

```
doas pkg_add python
```

When it complains that it is ambiguous, choose one however you want, I guess.
I chose 3.9 because I'm thinking it'll get fewer updates at this point (3.10.5 was released today).
Copy the path to the tool since we'll need to give it to Ansible.
Mine was `/usr/local/bin/python3`
