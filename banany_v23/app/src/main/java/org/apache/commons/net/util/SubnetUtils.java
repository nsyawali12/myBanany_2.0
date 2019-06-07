package org.apache.commons.net.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubnetUtils {
    private static final String IP_ADDRESS = "(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})";
    private static final int NBITS = 32;
    private static final String SLASH_FORMAT = "(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})/(\\d{1,3})";
    private static final Pattern addressPattern = Pattern.compile(IP_ADDRESS);
    private static final Pattern cidrPattern = Pattern.compile(SLASH_FORMAT);
    private int address = 0;
    private int broadcast = 0;
    private boolean inclusiveHostCount = false;
    private int netmask = 0;
    private int network = 0;

    public final class SubnetInfo {
        private static final long UNSIGNED_INT_MASK = 4294967295L;

        private SubnetInfo() {
        }

        private int netmask() {
            return SubnetUtils.this.netmask;
        }

        private int network() {
            return SubnetUtils.this.network;
        }

        private int address() {
            return SubnetUtils.this.address;
        }

        private int broadcast() {
            return SubnetUtils.this.broadcast;
        }

        private long networkLong() {
            return ((long) SubnetUtils.this.network) & UNSIGNED_INT_MASK;
        }

        private long broadcastLong() {
            return ((long) SubnetUtils.this.broadcast) & UNSIGNED_INT_MASK;
        }

        private int low() {
            if (SubnetUtils.this.isInclusiveHostCount()) {
                return network();
            }
            return broadcastLong() - networkLong() > 1 ? network() + 1 : 0;
        }

        private int high() {
            if (SubnetUtils.this.isInclusiveHostCount()) {
                return broadcast();
            }
            return broadcastLong() - networkLong() > 1 ? broadcast() - 1 : 0;
        }

        public boolean isInRange(String address) {
            return isInRange(SubnetUtils.this.toInteger(address));
        }

        public boolean isInRange(int address) {
            long addLong = ((long) address) & UNSIGNED_INT_MASK;
            return addLong >= (((long) low()) & UNSIGNED_INT_MASK) && addLong <= (((long) high()) & UNSIGNED_INT_MASK);
        }

        public String getBroadcastAddress() {
            return SubnetUtils.this.format(SubnetUtils.this.toArray(broadcast()));
        }

        public String getNetworkAddress() {
            return SubnetUtils.this.format(SubnetUtils.this.toArray(network()));
        }

        public String getNetmask() {
            return SubnetUtils.this.format(SubnetUtils.this.toArray(netmask()));
        }

        public String getAddress() {
            return SubnetUtils.this.format(SubnetUtils.this.toArray(address()));
        }

        public String getLowAddress() {
            return SubnetUtils.this.format(SubnetUtils.this.toArray(low()));
        }

        public String getHighAddress() {
            return SubnetUtils.this.format(SubnetUtils.this.toArray(high()));
        }

        @Deprecated
        public int getAddressCount() {
            long countLong = getAddressCountLong();
            if (countLong <= 2147483647L) {
                return (int) countLong;
            }
            throw new RuntimeException("Count is larger than an integer: " + countLong);
        }

        public long getAddressCountLong() {
            long count = (broadcastLong() - networkLong()) + ((long) (SubnetUtils.this.isInclusiveHostCount() ? 1 : -1));
            return count < 0 ? 0 : count;
        }

        public int asInteger(String address) {
            return SubnetUtils.this.toInteger(address);
        }

        public String getCidrSignature() {
            return SubnetUtils.this.toCidrNotation(SubnetUtils.this.format(SubnetUtils.this.toArray(address())), SubnetUtils.this.format(SubnetUtils.this.toArray(netmask())));
        }

        public String[] getAllAddresses() {
            int ct = getAddressCount();
            String[] addresses = new String[ct];
            if (ct != 0) {
                int add = low();
                int j = 0;
                while (add <= high()) {
                    addresses[j] = SubnetUtils.this.format(SubnetUtils.this.toArray(add));
                    add++;
                    j++;
                }
            }
            return addresses;
        }

        public String toString() {
            StringBuilder buf = new StringBuilder();
            buf.append("CIDR Signature:\t[").append(getCidrSignature()).append("]").append(" Netmask: [").append(getNetmask()).append("]\n").append("Network:\t[").append(getNetworkAddress()).append("]\n").append("Broadcast:\t[").append(getBroadcastAddress()).append("]\n").append("First Address:\t[").append(getLowAddress()).append("]\n").append("Last Address:\t[").append(getHighAddress()).append("]\n").append("# Addresses:\t[").append(getAddressCount()).append("]\n");
            return buf.toString();
        }
    }

    public SubnetUtils(String cidrNotation) {
        calculate(cidrNotation);
    }

    public SubnetUtils(String address, String mask) {
        calculate(toCidrNotation(address, mask));
    }

    public boolean isInclusiveHostCount() {
        return this.inclusiveHostCount;
    }

    public void setInclusiveHostCount(boolean inclusiveHostCount) {
        this.inclusiveHostCount = inclusiveHostCount;
    }

    public final SubnetInfo getInfo() {
        return new SubnetInfo();
    }

    private void calculate(String mask) {
        Matcher matcher = cidrPattern.matcher(mask);
        if (matcher.matches()) {
            this.address = matchAddress(matcher);
            int cidrPart = rangeCheck(Integer.parseInt(matcher.group(5)), 0, 32);
            for (int j = 0; j < cidrPart; j++) {
                this.netmask |= 1 << (31 - j);
            }
            this.network = this.address & this.netmask;
            this.broadcast = this.network | (this.netmask ^ -1);
            return;
        }
        throw new IllegalArgumentException("Could not parse [" + mask + "]");
    }

    private int toInteger(String address) {
        Matcher matcher = addressPattern.matcher(address);
        if (matcher.matches()) {
            return matchAddress(matcher);
        }
        throw new IllegalArgumentException("Could not parse [" + address + "]");
    }

    private int matchAddress(Matcher matcher) {
        int addr = 0;
        for (int i = 1; i <= 4; i++) {
            addr |= (rangeCheck(Integer.parseInt(matcher.group(i)), 0, 255) & 255) << ((4 - i) * 8);
        }
        return addr;
    }

    private int[] toArray(int val) {
        int[] ret = new int[4];
        for (int j = 3; j >= 0; j--) {
            ret[j] = ret[j] | ((val >>> ((3 - j) * 8)) & 255);
        }
        return ret;
    }

    private String format(int[] octets) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < octets.length; i++) {
            str.append(octets[i]);
            if (i != octets.length - 1) {
                str.append(".");
            }
        }
        return str.toString();
    }

    private int rangeCheck(int value, int begin, int end) {
        if (value >= begin && value <= end) {
            return value;
        }
        throw new IllegalArgumentException("Value [" + value + "] not in range [" + begin + "," + end + "]");
    }

    int pop(int x) {
        x -= (x >>> 1) & 1431655765;
        x = (x & 858993459) + ((x >>> 2) & 858993459);
        x = ((x >>> 4) + x) & 252645135;
        x += x >>> 8;
        return (x + (x >>> 16)) & 63;
    }

    private String toCidrNotation(String addr, String mask) {
        return addr + "/" + pop(toInteger(mask));
    }
}
