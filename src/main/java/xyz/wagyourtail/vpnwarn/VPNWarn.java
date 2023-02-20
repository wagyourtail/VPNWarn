package xyz.wagyourtail.vpnwarn;

import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;

@Mod(modid = VPNWarn.MODID)
public class VPNWarn {

    private static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "vpnwarn";

    public VPNWarn() {

    }

    private static String getMac(byte[] macAddr) {
        if (macAddr == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(18);
        for (byte b : macAddr) {
            if (sb.length() > 0) {
                sb.append(':');
            }
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static boolean isUsingVPN() {
        // get network device info
        Enumeration<NetworkInterface> ifs = null;
        try {
            ifs = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            LOGGER.error("Error getting network interfaces", e);
        }

        if (ifs != null) {
            // loop through network devices
            while (ifs.hasMoreElements()) {
                try {
                    NetworkInterface iface = ifs.nextElement();
                    if (iface.isLoopback()) {
                        continue;
                    }

                    if (!iface.isUp()) {
                        continue;
                    }

                    LOGGER.debug("Interface: " + iface.getDisplayName());
                    LOGGER.debug("  Hardware address: " + getMac(iface.getHardwareAddress()));
                    LOGGER.debug("  PtP: " + iface.isPointToPoint());
                    LOGGER.debug("  Virtual: " + iface.isVirtual());

                    if (iface.isPointToPoint()) {
                        LOGGER.info("  VPN detected!");
                        return true;
                    }

                    if (iface.isVirtual()) {
                        LOGGER.info("  VPN detected!");
                        return true;
                    }

                    if (iface.getDisplayName().contains("tun") || iface.getDisplayName().contains("tap")) {
                        LOGGER.info("  VPN detected!");
                        return true;
                    }

                    if (iface.getDisplayName().contains("ppp")) {
                        LOGGER.info("  VPN detected!");
                        return true;
                    }
                } catch (IOException ignored) {
                }
            }
        }
        String myIP;
        try {
            // get my ip
            URL url = new URL("https://checkip.amazonaws.com");
            myIP = tryUpToThreeTimes(() -> {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
                    return in.readLine();
                }
            });
        } catch (IOException e) {
            LOGGER.error("Error getting my ip", e);
            return false;
        }


        try {
            // get weather the ip is registered to a vpn
            URL url2 = new URL(
                "https://check.getipintel.net/check.php?ip=" + myIP + "&contact=wagyourtail@wagyourtail.xyz");
            float isVPN = Float.parseFloat(tryUpToThreeTimes(() -> {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(url2.openStream()))) {
                    return in.readLine();
                }
            }));

            if (isVPN > .5f) {
                LOGGER.info("  VPN detected!");
                return true;
            }
            if (isVPN < 0) {
                LOGGER.error("  Error getting vpn status! (Error code: " + isVPN + ")");
                return true;
            }
        } catch (IOException e) {
            LOGGER.error("Error getting vpn status", e);
        }
        return false;
    }
    
    private static <T, U extends Throwable> T tryUpToThreeTimes(ThrowingCallable<T, U> callable) throws U {
        U ex = null;
        for (int i = 0; i < 3; i++) {
            try {
                return callable.call();
            } catch (Exception e) {
                ex = (U) e;
            }
        }
        throw ex;
    }

    public interface ThrowingCallable<T, U extends Throwable> {
        T call() throws U;
    }
}
