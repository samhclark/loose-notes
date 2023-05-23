import java.net.InetAddress;
import java.net.UnknownHostException;

public class DnsLookup {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Provide exactly one hostname as an argument to this tool");
            System.exit(-1);
        }

        final InetAddress[] ips;
        try {
            ips = InetAddress.getAllByName(args[1]);
        } catch (UnknownHostException uhe) {
            System.out.println("Unknown host");
            return;
        }

        if (ips.length <= 0) {
            System.out.println("No IP addresses found");
            return;
        }

        System.out.print("IP addresses: ");
        for (int i = 0; i < ips.length; i++) {
            System.out.print(ips[i].getHostAddress().toString() + ", ");
        }
        System.out.print("\n");
    }
}
