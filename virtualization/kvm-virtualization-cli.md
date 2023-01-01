# Notes about doing KVM Virtualization via CLI

Specifically on openSUSE Leap 15.4.
Using this for reference https://doc.opensuse.org/documentation/leap/virtualization/single-html/book-virtualization/index.html

`libvirt` abstracts this? 
At least it has KVM and Xen parts. 
So, now I've installed KVM `libvirt` stuff and I have a CLI tool called `virsh`.

For the record that was: 

```
$ sudo zypper install -t pattern kvm_server
$ sudo zypper install -t pattern kvm_tools
```

Skipping the UEFI stuff. 
Not sure I care if my VM guests are using Secure Boot or UEFI?
And not doing nesting either. 
Maybe that'll bite me because idk if Podman will require nesting to run in a VM.
We'll see.

`libvirtd` is a daemon that runs on the host which manages? provides? the `libvirt` API.

Then you really gotta configure two things:
* Network
* Storage

For the network, I chose a virtual network that does NAT and forwarding, not a bridge.
So that means, it's configured with `virsh` not with the `ip` and `bridge` commands.

It's important to run `virsh` with `sudo` because there may be networks that exist already but you can't see without root.
There is already a `default` network that does NAT so I'm just using that.
All I had to do was start it (`net-start`) and mark it as autostarted (`net-autostart`).

For the storage, there's basically two different kinds: "storage volumes" and "storage pools".
openSUSE looks like it defaults to pools. 
No idea what RHEL does, after looking at their docs. 
But neither of them get really in depth, like it's not something you usually configure.
From the openSUSE docs:
> creating storage pools is currently not supported by SUSE. 
> Therefore, this section is restricted to documenting functions such as starting, stopping, and deleting pools, and volume management. 

Looks like you kinda gloss over this and let `virt-install` deal with storage?

Okay, so now I downloaded the openSUSE MicroOS disk image (qcow2).
The virt-install command to make one is...

```
sudo virt-install \
    --virt-type kvm \
    --name demo-vm \
    --memory 4096 \
    --vcpus 2 \
    --import \
    --disk ~/openSUSE-MicroOS.x86_64-ContainerHost-kvm-and-xen.qcow2 \
    --os-variant opensusetumbleweed \
    --graphics none
```

So the above command kinda worked, but I'm missing config or something, and I don't really understand the openSUSE MicroOS docs.

Gonna try to switch to Fedore CoreOS so that I can get something working at all.

```
IGNITION_CONFIG="/home/admin/config.ign"
IMAGE="/home/admin/fedora-coreos-37.20221211.3.0-qemu.x86_64.qcow2"
VM_NAME="fcos-test-01"
VCPUS="2"
RAM_MB="4096"
STREAM="stable"
DISK_GB="10"

IGNITION_DEVICE_ARG=(--qemu-commandline="-fw_cfg name=opt/com.coreos/config,file=${IGNITION_CONFIG}")

# Setup the correct SELinux label to allow access to the config
#chcon --verbose --type svirt_home_t ${IGNITION_CONFIG}

sudo virt-install --connect="qemu:///system" --name="${VM_NAME}" --vcpus="${VCPUS}" --memory="${RAM_MB}" \
        --os-variant="fedora-coreos-$STREAM" --import --graphics=none \
        --disk="size=${DISK_GB},backing_store=${IMAGE}" \
        --network bridge=virbr0 "${IGNITION_DEVICE_ARG[@]}"
```

That worked!
Kinda.
I had to go create an Ignition file first, which I did with an openSUSE tool here: https://opensuse.github.io/fuel-ignition/edit.

It created a VM called `fcos-test-01` with the IP address 192.168.122.210 but I don't know how I'd get that info now that I exited the serial console.
I can't figure out the `virsh` incantation that will show me the IP of the guest VMs. 
Anyway, I am locked out now. 
Can't SSH in because the VM is behind NAT, so I can only SSH from the VM Host. 
But the public key I picked is the one from my laptop. 
Probably need to recreate this, generate and use a key from the host, or un-NAT this thing. 



## Maybe something worth writing down?

So essentially the flow is like this:

1. Download the latest Fedora CoreOS qcow2 image. 
2. Write a Butane config (some YAML) that describes how the OS should be set up. Feels like Ansible but weirder. 
3. Take the `butane` binary and generate an "Ignition" config file (some JSON) like `butane --pretty --strict virtualization/babys-first-config.bu --output virtualization/babys-first-config.ign`
4. Validate the ignition config file, if you want, with `ignition-validate virtualization/babys-first-config.ign`
5. Run a kinda complicated command on the VM Host to use that Ignition config. Which likes attaches it to a new VM that you create with `virt-install`. Basically, the same `virt-install` command as listed in the previous section
6. Then, while on the VM Host, and only on the VM Host, you can SSH into the VM. If you really want. But you don't need to. The config file by itself works. Tested with `curl http://containerhost:8080` from the VM Host.

Not really sure if I should run Caddy on the VM host and then redirect requests to individual guests VMs? 
Or will it be better to run all the services in a single guest including Caddy? 

Maybe running into issues with `libvirt` firewall rules since I can't redirect traffic to the guest VMs.
That might make the whole "where do I run Caddy" thing moot.
See: https://superuser.com/questions/1474254/firewall-cmd-add-forward-port-dont-work