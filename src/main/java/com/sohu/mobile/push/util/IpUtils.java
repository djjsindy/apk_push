package com.sohu.mobile.push.util;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by IntelliJ IDEA.
 * User: yaqinzhang
 * Date: 12-8-6
 * Time: 上午11:22
 * To change this template use File | Settings | File Templates.
 */
public class IpUtils {
        private static String localIp;

    static {
        Enumeration<NetworkInterface> networkInterface;
        try {
            networkInterface = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            throw new IllegalStateException(e);
        }
        String ip = null;
        boolean isFound = false;
        while (networkInterface.hasMoreElements()) {
            NetworkInterface ni = networkInterface.nextElement();
            Enumeration<InetAddress> inetAddress = ni.getInetAddresses();
            while (inetAddress.hasMoreElements()) {
                InetAddress ia = inetAddress.nextElement();
                if (ia instanceof Inet6Address) {
                    continue; // ignore ipv6
                }
                if (!ia.isLoopbackAddress()
                        && ia.getHostAddress().indexOf(":") == -1) {
                    ip = ia.getHostAddress();
                    isFound = true;
                    break;
                }
            }
            if (isFound) {
                break;
            }
        }
        localIp = ip;
    }

    /**
     * 只取ipv4且如有多个ip,只取第一个
     *
     * 暂不支持ipv6
     *
     * @return
     * @throws java.net.SocketException
     */
    public static String getLocalIp() {
        return localIp;
    }

    /**
     * @param ip
     * @return true or false 暂不支持ipv6
     */
    public static boolean isLocalIp(String ip) {
        boolean match = false;
        Enumeration<NetworkInterface> networkInterface;
        try {
            networkInterface = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            throw new IllegalStateException(e);
        }
        ip = ip == null ? "" : ip.trim();
        while (networkInterface.hasMoreElements()) {
            NetworkInterface ni = networkInterface.nextElement();
            Enumeration<InetAddress> inetAddress = ni.getInetAddresses();
            while (inetAddress.hasMoreElements()) {
                InetAddress ia = inetAddress.nextElement();
                if (ia instanceof Inet6Address) {
                    continue; // ignore ipv6
                }
                if (!ia.isLoopbackAddress()
                        && ia.getHostAddress().indexOf(":") == -1) {
                    if (ip.equals(ia.getHostAddress())) {
                        match = true;
                        break;
                    }
                }
            }
            if (match) {
                break;
            }
        }
        return match;
    }
}
