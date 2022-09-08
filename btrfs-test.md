# BTRFS Test

_2022-09-07_

So, I really don't understand the BTRFS filesystem or the `btrfs` commands. 
I'm going to come up with some test scenarios to figure it out. 

## Scenarios

### Single disk

I don't really want to deal with real disks, so I'm going to use files.

Create a file to use as a disk.
In this case, the file is `/tmp/a` and it's 4 GB.

```
% truncate --size=4G /tmp/a
```

Mount it as a loopback device so we can get `btrfs` to play nice with it.

```
% sudo losetup --show --find /tmp/a
/dev/loop0
```

It prints out the name of the loopback device that it's mounted as.
In this case, that's `/dev/loop0`.

Zero it out, so that BTRFS won't complain about not detecting the filesystem.
And so that if we need to inspect anything, it'll be easier.

```
% sudo dd if=/dev/zero of=/dev/loop0 bs=4096 status=progress
```

Now we make a BTRFS filesystem on that device, make a mount point, and mount it.
It needs to be mounted with `max_inline=0` so that the file we make doesn't get inlined into the filesystem metadata.
We want to be able to find the file's location and corrupt it.
And set the ownership to myself because I'm tired of typing `sudo`.

```
% sudo mkfs.btrfs --data single --metadata single --label test1 /dev/loop0
% sudo mkdir -p /mnt/btrfs/test1
% sudo mount -o max_inline=0 /dev/loop0 /mnt/btrfs/test1
% sudo chown sam:sam /mnt/btrfs/test1
```

Let's make a file on this disk

```
% echo 'Hello world!' > /mnt/btrfs/test1/test.txt
```

Start a scrub?
I don't know if this is necessary. 
But start one anyway and wait until it's done.

```
% sudo btrfs scrub start /mnt/btrfs/test1
% sudo btrfs scrub status /mnt/btrfs/test1
```

Find the location of the file on disk:

```
% filefrag -v /mnt/btrfs/test1/test.txt
Filesystem type is: 9123683e
File size of /mnt/btrfs/test1/test.txt is 13 (1 block of 4096 bytes)
 ext:     logical_offset:        physical_offset: length:   expected: flags:
   0:        0..       0:       3328..      3328:      1:             last,eof
/mnt/btrfs/test1/test.txt: 1 extent found
```

Let's very that it's there:

```
% sudo dd if=/dev/loop0 bs=4096 count=1 skip=3328 | base64
1+0 records in
1+0 records out
4096 bytes (4.1 kB, 4.0 KiB) copied, 5.941e-05 s, 68.9 MB/s
SGVsbG8gd29ybGQhCgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
...
% printf 'SGVsbG8gd29ybGQhCg==' | base64 -d
Hello world!
```

We're going to overwrite that, to simulate corruption.
But I don't want to write that much data all at once.
So let's just write 16 bytes. 
4096 / 16 = 256.
3328 * 256 = 851968.

Double check that works:

```
% sudo dd if=/dev/loop0 bs=16 count=1 skip=851968 | base64
1+0 records in
1+0 records out
16 bytes copied, 1.5368e-05 s, 1.0 MB/s
SGVsbG8gd29ybGQhCgAAAA==
```

And let's overwrite it.
Notice! 
We use `skip` for choosing a location on the _input_ (if).
We use `seek` for choosing a location on the _output_ (of).

```
% printf 'HELLO' | sudo dd of=/dev/loop0 bs=16 count=1 seek=851968
```

Verify it:

```
% sudo dd if=/dev/loop0 bs=16 count=1 skip=851968
HELLO world!
```

At this point, we have a corrupted file on disk. 
It was saved through BTRFS, but then altered significantly once it was on disk.

Does BTRFS detect it? 

```
% sudo btrfs scrub start /mnt/btrfs/test1
scrub started on /mnt/btrfs/test1, fsid c0bdad26-ca9a-4210-99a3-edb6cb652c5b (pid=7389)
ERROR: there are uncorrectable errors
```

Yep!
But it can't be corrected. 
There are no known-good copies. 
So all it can say is that there was a problem.

If you wanted to know which file it was and fix it, you've gotta search through `dmesg`.

```
[ 9239.829882] BTRFS warning (device loop0): checksum error at logical 13631488 on dev /dev/loop0, physical 13631488, root 5, inode 257, offset 0, length 4096, links 1 (path: test.txt)
[ 9239.829896] BTRFS error (device loop0): bdev /dev/loop0 errs: wr 0, rd 0, flush 0, corrupt 1, gen 0
[ 9239.829904] BTRFS error (device loop0): unable to fixup (regular) error at logical 13631488 on dev /dev/loop0
[ 9239.829964] BTRFS info (device loop0): scrub: finished on devid 1 with status: 0
```

The weirder part is that putting the bits back didn't fix it.
Rewriting with `'Hello'` and starting another scrub still shows errors.

And another weird thing is that even once it was changed on disk, `cat` and `vim` both showed the unchanged version.

### Two Disks (RAID 1)

Same as last time, but two disks:

```
% truncate --size=4G /tmp/a
% truncate --size=4G /tmp/b
% sudo losetup --show --find /tmp/a
% sudo losetup --show --find /tmp/b
```

They got mounted as `/dev/loop0` and `/dev/loop1`.

Might as well zero them:

```
% sudo dd if=/dev/zero of=/dev/loop0 bs=4096 status=progress
% sudo dd if=/dev/zero of=/dev/loop1 bs=4096 status=progress
```

Now we make a BTRFS filesystem with the two disks. 
We use `raid1` so that we know a copy of the file is on each disk.

```
% sudo mkfs.btrfs --data raid1 --metadata raid1 --label test2 /dev/loop0 /dev/loop1
% sudo mkdir -p /mnt/btrfs/test2
% sudo mount -o max_inline=0 /dev/loop0 /mnt/btrfs/test2
% sudo chown sam:sam /mnt/btrfs/test2
```

As an aside, that first command didn't work and I had to run it twice? 
That was weird. 
It still gave me errors saying "superblock magic doesn't match."
Which sounds...absurd. 
No idea what that means.

Now we write a file, do a scrub, and see where it's really at on disk.

```
% echo 'Hello world!' > /mnt/btrfs/test2/test.txt
% sudo btrfs scrub start /mnt/btrfs/test2
% sudo btrfs scrub status /mnt/btrfs/test2
% filefrag -v /mnt/btrfs/test2/test.txt
Filesystem type is: 9123683e
File size of /mnt/btrfs/test2/test.txt is 13 (1 block of 4096 bytes)
 ext:     logical_offset:        physical_offset: length:   expected: flags:
   0:        0..       0:      72960..     72960:      1:             last,eof
/mnt/btrfs/test2/test.txt: 1 extent found
```

Is it really there?

```
% sudo dd if=/dev/loop0 bs=4096 count=1 skip=72960
Hello world!
```

Yep!
Let's corrupt it.

```
% printf '!dlrow olleH' | sudo dd of=/dev/loop0 bs=4096 seek=72960
% sudo dd if=/dev/loop0 bs=4096 count=1 skip=72960
!dlrow olleH
```

Does BTRFS detect it? 

```
% sudo btrfs scrub start /mnt/btrfs/test2
scrub started on /mnt/btrfs/test2, fsid a36132d1-e842-4505-a08c-74dced1ab6be (pid=8627)
WARNING: errors detected during scrubbing, corrected
```

Yes but!
It doesn't _actually_ correct it? 

```
% sudo dd if=/dev/loop0 bs=4096 count=1 skip=72960
!dlrow olleH
```

And the file didn't move, right? 

```
% filefrag -v /mnt/btrfs/test2/test.txt
Filesystem type is: 9123683e
File size of /mnt/btrfs/test2/test.txt is 13 (1 block of 4096 bytes)
 ext:     logical_offset:        physical_offset: length:   expected: flags:
   0:        0..       0:      72960..     72960:      1:             last,eof
/mnt/btrfs/test2/test.txt: 1 extent found
```

Which is weird.
Scrub says it's fine, but on the disk it's clearly corrupted still. 
Maybe there is something else going on, and there's another copy I don't know about.
Maybe, for example, scrub inlines the file anyway? 

Let's check by corrupting the copy that's on the other disk. 
I don't really know how you're supposed to do this with built-in tools.
I am using `btrfs_map_physical` from here: https://github.com/osandov/osandov-linux/blob/master/scripts/btrfs_map_physical.c

```
% sudo ./a.out /mnt/btrfs/test2/test.txt | column -ts $'\t'
FILE OFFSET  FILE SIZE  EXTENT OFFSET  EXTENT TYPE    LOGICAL SIZE  LOGICAL OFFSET  PHYSICAL SIZE  DEVID  PHYSICAL OFFSET
0            4096       0              regular,raid1  4096          298844160       4096           1      298844160
                                                                                                   2      277872640
% print "$((277872640 / 4096))"
67840
% sudo dd if=/dev/loop1 bs=4096 count=1 skip=67840
Hello world!
% printf '!dlrow olleH' | sudo dd of=/dev/loop1 bs=4096 seek=67840
% sudo dd if=/dev/loop1 bs=4096 count=1 skip=67840
!dlrow olleH
```

```
% sudo btrfs scrub start /mnt/btrfs/test2
scrub started on /mnt/btrfs/test2, fsid a36132d1-e842-4505-a08c-74dced1ab6be (pid=9046)
WARNING: errors detected during scrubbing, corrected
```

Well it sure says it corrected it.
And now both copies are backwards, according to dd.
What the hell? 
Gonna unmount and try again.

Mounting and unmounting works.
It's gotta be something with having the pages in RAM instead of reading through from disk.
The OS doesn't know that the file changed since we corrupted it on disk.

The key is adding `iflag=direct` and `oflag=direct`.

```
% printf '!dlrow olleH' | sudo dd of=/dev/loop1 bs=4096 seek=67840 oflag=direct
% sudo dd if=/dev/loop1 bs=4096 count=1 skip=67840 iflag=direct
!dlrow olleH
% sudo btrfs scrub start -B /mnt/btrfs/test2
scrub done for a36132d1-e842-4505-a08c-74dced1ab6be
Scrub started:    Wed Sep  7 20:53:39 2022
Status:           finished
Duration:         0:00:00
Total to scrub:   1.32GiB
Rate:             0.00B/s
Error summary:    csum=1
  Corrected:      1
  Uncorrectable:  0
  Unverified:     0
WARNING: errors detected during scrubbing, corrected
% sudo dd if=/dev/loop1 bs=4096 count=1 skip=67840 iflag=direct
Hello world!
```


So scrub really does repair the files on disk as long as there is a working copy. 

Can I remove a disk?

```
% sudo umount /mnt/btrfs/test2
% sudo losetup -d /dev/loop1
% sudo rm /dev/loop1
% sudo mount -o max_inline=0 /dev/loop0 /mnt/btrfs/test2
mount: /mnt/btrfs/test2: wrong fs type, bad option, bad superblock on /dev/loop0, missing codepage or helper program, or other error.
       dmesg(1) may have more information after failed mount system call.
```

That's what is expected. 
Because a device is missing, you need to mount it with the `degraded` flag.

```
% sudo mount -o max_inline=0,degraded /dev/loop0 /mnt/btrfs/test2
% sudo btrfs filesystem show
Label: 'test2'  uuid: a36132d1-e842-4505-a08c-74dced1ab6be
	Total devices 2 FS bytes used 148.00KiB
	devid    1 size 4.00GiB used 673.56MiB path /dev/loop0
	devid    2 size 0 used 0 path /dev/loop1 MISSING
```

Let's create a replacement device and pop it in.
First make the replacement: `/dev/loop2`.

```
% truncate -s 4G /tmp/c
% sudo losetup /dev/loop2 /tmp/c
% sudo dd if=/dev/zero of=/dev/loop2 bs=4096 oflag=direct status=progress
```

Then replace the device. 
The `2` is the `devid` from above, where we did `btrfs filesystem show`

```
% sudo btrfs replace start -B 2 /dev/loop2 /mnt/btrfs/test2
% sudo btrfs filesystem show test2
Label: 'test2'  uuid: a36132d1-e842-4505-a08c-74dced1ab6be
	Total devices 2 FS bytes used 148.00KiB
	devid    1 size 4.00GiB used 1.34GiB path /dev/loop0
	devid    2 size 4.00GiB used 673.56MiB path /dev/loop2
```

No idea how this much got used. 
I only wrote that one text file to them.

```
% sudo btrfs balance start /mnt/btrfs/test2
WARNING:

	Full balance without filters requested. This operation is very
	intense and takes potentially very long. It is recommended to
	use the balance filters to narrow down the scope of balance.
	Use 'btrfs balance start --full-balance' option to skip this
	warning. The operation will start in 10 seconds.
	Use Ctrl-C to stop it.
10 9 8 7 6 5 4 3 2 1
Starting balance without any filters.
Done, had to relocate 6 out of 6 chunks
% sudo btrfs filesystem show test2
Label: 'test2'  uuid: a36132d1-e842-4505-a08c-74dced1ab6be
	Total devices 2 FS bytes used 148.00KiB
	devid    1 size 4.00GiB used 704.00MiB path /dev/loop0
	devid    2 size 4.00GiB used 704.00MiB path /dev/loop2
```

I guess that did something. 
And now I know how to replace a device. 
And that scrub really does work. 