package com.rAs.android.rpgamepad;

import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XFakeWifiEnabler {
    private static final String CONNECTIVITY_MANAGER = "android.net.ConnectivityManager";
    private static final String WIFI_MANAGER = "android.net.wifi.WifiManager";

    private static final Pattern IPV4_PATTERN = Pattern.compile("^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");

    private XC_LoadPackage.LoadPackageParam lpparam;

    public XFakeWifiEnabler(XC_LoadPackage.LoadPackageParam lpparam) {
        this.lpparam = lpparam;
    }

    public void apply() {

        XposedHelpers.findAndHookMethod(CONNECTIVITY_MANAGER, lpparam.classLoader, "getActiveNetworkInfo", fakeNetworkEnabler);
        XposedHelpers.findAndHookMethod(CONNECTIVITY_MANAGER, lpparam.classLoader, "getActiveNetworkInfoForUid", int.class, fakeNetworkEnabler);
//        XposedHelpers.findAndHookMethod(CONNECTIVITY_MANAGER, lpparam.classLoader, "getProvisioningOrActiveNetworkInfo", fakeNetworkEnabler);
        XposedHelpers.findAndHookMethod(CONNECTIVITY_MANAGER, lpparam.classLoader, "getAllNetworkInfo", fakeAllNetworkEnabler);
        XposedHelpers.findAndHookMethod(CONNECTIVITY_MANAGER, lpparam.classLoader, "getNetworkInfo", int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                int networkType = (Integer) param.args[0];
                if (networkType == ConnectivityManager.TYPE_WIFI)
                    applyFakeNetwork(param);
            }
        });

        XposedHelpers.findAndHookMethod(CONNECTIVITY_MANAGER, lpparam.classLoader, "isActiveNetworkMetered", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(false);
            }
        });



        XposedHelpers.findAndHookMethod(WIFI_MANAGER, lpparam.classLoader, "isWifiEnabled", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(true);
            }
        });

        XposedHelpers.findAndHookMethod(WIFI_MANAGER, lpparam.classLoader, "getWifiState", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(WifiManager.WIFI_STATE_ENABLED);
            }
        });

        XposedHelpers.findAndHookMethod(WIFI_MANAGER, lpparam.classLoader, "getConnectionInfo", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(createWifiInfo());
            }
        });

        XposedHelpers.findAndHookMethod(WIFI_MANAGER, lpparam.classLoader, "getDhcpInfo", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                ConnectionInfo info = getConnectionInfo();
                if (info != null)
                    param.setResult(createDhcpInfo(info));
            }
        });
    }

    public static class ConnectionInfo {
        private NetworkInterface networkInterface;
        private InetAddress inetAddress;
        private String ipStr;
        private int ip;
        private int netmask;

        public NetworkInterface getNetworkInterface() {
            return networkInterface;
        }

        public void setNetworkInterface(NetworkInterface networkInterface) {
            this.networkInterface = networkInterface;
        }

        public InetAddress getInetAddress() {
            return inetAddress;
        }

        public void setInetAddress(InetAddress inetAddress) {
            this.inetAddress = inetAddress;
        }

        public String getIpStr() {
            return ipStr;
        }

        public void setIpStr(String ipStr) {
            this.ipStr = ipStr;
        }

        public int getIp() {
            return ip;
        }

        public void setIp(int ip) {
            this.ip = ip;
        }

        public int getNetmask() {
            return netmask;
        }

        public void setNetmask(int netmask) {
            this.netmask = netmask;
        }
    }

    private XC_MethodHook fakeNetworkEnabler = new XC_MethodHook() {
        @Override
        protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
            applyFakeNetwork(param);
        }
    };

    private XC_MethodHook fakeAllNetworkEnabler = new XC_MethodHook() {
        @Override
        protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
            NetworkInfo[] networks = (NetworkInfo[]) param.getResult();
            int i;
            boolean isWifiExists = false;
            for (i = 0; i < networks.length; i++) {
                if (networks[i].getType() == ConnectivityManager.TYPE_WIFI) {
                    isWifiExists = true;
                    break;
                }
            }

            if (isWifiExists) {
                if(networks[i].isConnected()) {
                    return;
                } else {
                    networks[i] = getFakeWifiNetworkInfo();
                }
            } else {
                NetworkInfo[] newNetworks = new NetworkInfo[networks.length + 1];
                System.arraycopy(networks, 0, newNetworks, 0, networks.length);
                newNetworks[networks.length] = getFakeWifiNetworkInfo();

                networks = newNetworks;
            }

            param.setResult(networks);
        }
    };

    private void applyFakeNetwork(XC_MethodHook.MethodHookParam param) throws Throwable {
        if (param.getResult() != null) {
            NetworkInfo network = (NetworkInfo) param.getResult();
            if (network.getType() == ConnectivityManager.TYPE_WIFI && network.isConnected()) {
                return;
            }
        }

        param.setResult(getFakeWifiNetworkInfo());
    }

    private NetworkInfo getFakeWifiNetworkInfo() throws Exception {
        NetworkInfo networkInfo;
        if (Build.VERSION.SDK_INT >= 21) {
            networkInfo = (NetworkInfo) XposedHelpers.newInstance(NetworkInfo.class, 0, 0, null, null);
        } else {
            networkInfo = (NetworkInfo) XposedHelpers.newInstance(NetworkInfo.class, 0);
        }

        XposedHelpers.setIntField(networkInfo, "mNetworkType", ConnectivityManager.TYPE_WIFI);
        XposedHelpers.setObjectField(networkInfo, "mTypeName", "WIFI");
        XposedHelpers.setObjectField(networkInfo, "mState", NetworkInfo.State.CONNECTED);
        XposedHelpers.setObjectField(networkInfo, "mDetailedState", NetworkInfo.DetailedState.CONNECTED);
        XposedHelpers.setBooleanField(networkInfo, "mIsAvailable", true);
        return networkInfo;
    }

    private WifiInfo createWifiInfo() throws Exception
    {
        WifiInfo info = (WifiInfo) XposedHelpers.newInstance(WifiInfo.class);

        ConnectionInfo ip = getConnectionInfo();
        InetAddress addr = (ip != null ? ip.getInetAddress() : null);
        XposedHelpers.setIntField((Object)info, "mNetworkId", 1);
        XposedHelpers.setObjectField((Object)info, "mSupplicantState", SupplicantState.COMPLETED);
        XposedHelpers.setObjectField((Object)info, "mBSSID", "66:55:44:33:22:11");
        XposedHelpers.setObjectField((Object)info, "mMacAddress", "11:22:33:44:55:66");
        XposedHelpers.setObjectField((Object)info, "mIpAddress", addr);
        XposedHelpers.setIntField((Object)info, "mLinkSpeed", 65);  // Mbps
        if (Build.VERSION.SDK_INT >= 21) XposedHelpers.setIntField((Object)info, "mFrequency", 5000); // MHz
        XposedHelpers.setIntField((Object)info, "mRssi", 200); // MAX_RSSI

        try {
            // Kitkat
            Class clazz = XposedHelpers.findClass("android.net.wifi.WifiSsid", lpparam.classLoader);
            Object wifiSsid = XposedHelpers.callStaticMethod(clazz, "createFromAsciiEncoded", "FakeWifi");

            XposedHelpers.setObjectField((Object)info, "mWifiSsid", wifiSsid);
        } catch (Error e) {
            // Jellybean
            XposedHelpers.setObjectField((Object)info, "mSSID", "FakeWifi");
        }

        return info;
    }

    private static ConnectionInfo getConnectionInfo() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface networkInterface : interfaces) {
                List<InetAddress> addrs = Collections.list(networkInterface.getInetAddresses());
                for (InetAddress inetAddress : addrs) {
                    if (!inetAddress.isLoopbackAddress()) {
                        String ipStr = inetAddress.getHostAddress().toUpperCase();
                        if (IPV4_PATTERN.matcher(ipStr).matches()) {
                            ConnectionInfo info = new ConnectionInfo();
                            info.setNetworkInterface(networkInterface);
                            info.setInetAddress(inetAddress);
                            info.setIpStr(ipStr);

                            int ip = 0;
                            byte inetAddrBytes[] = inetAddress.getAddress();
                            for (int i = 0; i < 4; i++)
                                ip |= (inetAddrBytes[i] & 0xff) << (8 * i);

                            info.setIp(ip);

                            int netmaskSlash = networkInterface.getInterfaceAddresses().get(0).getNetworkPrefixLength();
                            int netmask = 0;
                            int b = 1;
                            for (int i = 0; i < netmaskSlash; i++, b = b << 1)
                                netmask |= b;

                            info.setNetmask(netmask);
                            return info;
                        }
                    }
                }
            }
        } catch (Exception ex) { }
        return null;
    }

    private DhcpInfo createDhcpInfo(ConnectionInfo connectionInfo) {
        DhcpInfo dhcpInfo = new DhcpInfo();
        dhcpInfo.ipAddress = connectionInfo.getIp();
        dhcpInfo.netmask = connectionInfo.getNetmask();
        dhcpInfo.dns1 = 0x08080808;
        dhcpInfo.dns2 = 0x04040404;

        return dhcpInfo;
    }
}
