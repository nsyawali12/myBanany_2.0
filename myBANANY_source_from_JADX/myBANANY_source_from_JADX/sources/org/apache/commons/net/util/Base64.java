package org.apache.commons.net.util;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

public class Base64 {
    private static final byte[] CHUNK_SEPARATOR = new byte[]{(byte) 13, (byte) 10};
    static final int CHUNK_SIZE = 76;
    private static final byte[] DECODE_TABLE = new byte[]{(byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) 62, (byte) -1, (byte) 62, (byte) -1, (byte) 63, (byte) 52, (byte) 53, (byte) 54, (byte) 55, (byte) 56, (byte) 57, (byte) 58, (byte) 59, (byte) 60, PAD, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) 0, (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5, (byte) 6, (byte) 7, (byte) 8, (byte) 9, (byte) 10, (byte) 11, (byte) 12, (byte) 13, (byte) 14, (byte) 15, (byte) 16, (byte) 17, (byte) 18, (byte) 19, (byte) 20, (byte) 21, (byte) 22, (byte) 23, (byte) 24, (byte) 25, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) 63, (byte) -1, (byte) 26, (byte) 27, (byte) 28, (byte) 29, (byte) 30, (byte) 31, (byte) 32, (byte) 33, (byte) 34, (byte) 35, (byte) 36, (byte) 37, (byte) 38, (byte) 39, (byte) 40, (byte) 41, (byte) 42, (byte) 43, (byte) 44, (byte) 45, (byte) 46, (byte) 47, (byte) 48, (byte) 49, (byte) 50, (byte) 51};
    private static final int DEFAULT_BUFFER_RESIZE_FACTOR = 2;
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    private static final int MASK_6BITS = 63;
    private static final int MASK_8BITS = 255;
    private static final byte PAD = (byte) 61;
    private static final byte[] STANDARD_ENCODE_TABLE = new byte[]{(byte) 65, (byte) 66, (byte) 67, (byte) 68, (byte) 69, (byte) 70, (byte) 71, (byte) 72, (byte) 73, (byte) 74, (byte) 75, (byte) 76, (byte) 77, (byte) 78, (byte) 79, (byte) 80, (byte) 81, (byte) 82, (byte) 83, (byte) 84, (byte) 85, (byte) 86, (byte) 87, (byte) 88, (byte) 89, (byte) 90, (byte) 97, (byte) 98, (byte) 99, (byte) 100, (byte) 101, (byte) 102, (byte) 103, (byte) 104, (byte) 105, (byte) 106, (byte) 107, (byte) 108, (byte) 109, (byte) 110, (byte) 111, (byte) 112, (byte) 113, (byte) 114, (byte) 115, (byte) 116, (byte) 117, (byte) 118, (byte) 119, (byte) 120, (byte) 121, (byte) 122, (byte) 48, (byte) 49, (byte) 50, (byte) 51, (byte) 52, (byte) 53, (byte) 54, (byte) 55, (byte) 56, (byte) 57, (byte) 43, (byte) 47};
    private static final byte[] URL_SAFE_ENCODE_TABLE = new byte[]{(byte) 65, (byte) 66, (byte) 67, (byte) 68, (byte) 69, (byte) 70, (byte) 71, (byte) 72, (byte) 73, (byte) 74, (byte) 75, (byte) 76, (byte) 77, (byte) 78, (byte) 79, (byte) 80, (byte) 81, (byte) 82, (byte) 83, (byte) 84, (byte) 85, (byte) 86, (byte) 87, (byte) 88, (byte) 89, (byte) 90, (byte) 97, (byte) 98, (byte) 99, (byte) 100, (byte) 101, (byte) 102, (byte) 103, (byte) 104, (byte) 105, (byte) 106, (byte) 107, (byte) 108, (byte) 109, (byte) 110, (byte) 111, (byte) 112, (byte) 113, (byte) 114, (byte) 115, (byte) 116, (byte) 117, (byte) 118, (byte) 119, (byte) 120, (byte) 121, (byte) 122, (byte) 48, (byte) 49, (byte) 50, (byte) 51, (byte) 52, (byte) 53, (byte) 54, (byte) 55, (byte) 56, (byte) 57, (byte) 45, (byte) 95};
    private byte[] buffer;
    private int currentLinePos;
    private final int decodeSize;
    private final int encodeSize;
    private final byte[] encodeTable;
    private boolean eof;
    private final int lineLength;
    private final byte[] lineSeparator;
    private int modulus;
    private int pos;
    private int readPos;
    /* renamed from: x */
    private int f14x;

    public Base64() {
        this(false);
    }

    public Base64(boolean urlSafe) {
        this(76, CHUNK_SEPARATOR, urlSafe);
    }

    public Base64(int lineLength) {
        this(lineLength, CHUNK_SEPARATOR);
    }

    public Base64(int lineLength, byte[] lineSeparator) {
        this(lineLength, lineSeparator, false);
    }

    public Base64(int lineLength, byte[] lineSeparator, boolean urlSafe) {
        int i;
        if (lineSeparator == null) {
            lineLength = 0;
            lineSeparator = EMPTY_BYTE_ARRAY;
        }
        if (lineLength > 0) {
            i = (lineLength / 4) * 4;
        } else {
            i = 0;
        }
        this.lineLength = i;
        this.lineSeparator = new byte[lineSeparator.length];
        System.arraycopy(lineSeparator, 0, this.lineSeparator, 0, lineSeparator.length);
        if (lineLength > 0) {
            this.encodeSize = lineSeparator.length + 4;
        } else {
            this.encodeSize = 4;
        }
        this.decodeSize = this.encodeSize - 1;
        if (containsBase64Byte(lineSeparator)) {
            throw new IllegalArgumentException("lineSeperator must not contain base64 characters: [" + newStringUtf8(lineSeparator) + "]");
        }
        this.encodeTable = urlSafe ? URL_SAFE_ENCODE_TABLE : STANDARD_ENCODE_TABLE;
    }

    public boolean isUrlSafe() {
        return this.encodeTable == URL_SAFE_ENCODE_TABLE;
    }

    boolean hasData() {
        return this.buffer != null;
    }

    int avail() {
        return this.buffer != null ? this.pos - this.readPos : 0;
    }

    private void resizeBuffer() {
        if (this.buffer == null) {
            this.buffer = new byte[8192];
            this.pos = 0;
            this.readPos = 0;
            return;
        }
        byte[] b = new byte[(this.buffer.length * 2)];
        System.arraycopy(this.buffer, 0, b, 0, this.buffer.length);
        this.buffer = b;
    }

    int readResults(byte[] b, int bPos, int bAvail) {
        if (this.buffer != null) {
            int len = Math.min(avail(), bAvail);
            if (this.buffer != b) {
                System.arraycopy(this.buffer, this.readPos, b, bPos, len);
                this.readPos += len;
                if (this.readPos < this.pos) {
                    return len;
                }
                this.buffer = null;
                return len;
            }
            this.buffer = null;
            return len;
        }
        return this.eof ? -1 : 0;
    }

    void setInitialBuffer(byte[] out, int outPos, int outAvail) {
        if (out != null && out.length == outAvail) {
            this.buffer = out;
            this.pos = outPos;
            this.readPos = outPos;
        }
    }

    void encode(byte[] in, int inPos, int inAvail) {
        if (!this.eof) {
            byte[] bArr;
            int i;
            if (inAvail < 0) {
                this.eof = true;
                if (this.buffer == null || this.buffer.length - this.pos < this.encodeSize) {
                    resizeBuffer();
                }
                switch (this.modulus) {
                    case 1:
                        bArr = this.buffer;
                        i = this.pos;
                        this.pos = i + 1;
                        bArr[i] = this.encodeTable[(this.f14x >> 2) & 63];
                        bArr = this.buffer;
                        i = this.pos;
                        this.pos = i + 1;
                        bArr[i] = this.encodeTable[(this.f14x << 4) & 63];
                        if (this.encodeTable == STANDARD_ENCODE_TABLE) {
                            bArr = this.buffer;
                            i = this.pos;
                            this.pos = i + 1;
                            bArr[i] = PAD;
                            bArr = this.buffer;
                            i = this.pos;
                            this.pos = i + 1;
                            bArr[i] = PAD;
                            break;
                        }
                        break;
                    case 2:
                        bArr = this.buffer;
                        i = this.pos;
                        this.pos = i + 1;
                        bArr[i] = this.encodeTable[(this.f14x >> 10) & 63];
                        bArr = this.buffer;
                        i = this.pos;
                        this.pos = i + 1;
                        bArr[i] = this.encodeTable[(this.f14x >> 4) & 63];
                        bArr = this.buffer;
                        i = this.pos;
                        this.pos = i + 1;
                        bArr[i] = this.encodeTable[(this.f14x << 2) & 63];
                        if (this.encodeTable == STANDARD_ENCODE_TABLE) {
                            bArr = this.buffer;
                            i = this.pos;
                            this.pos = i + 1;
                            bArr[i] = PAD;
                            break;
                        }
                        break;
                }
                if (this.lineLength > 0 && this.pos > 0) {
                    System.arraycopy(this.lineSeparator, 0, this.buffer, this.pos, this.lineSeparator.length);
                    this.pos += this.lineSeparator.length;
                    return;
                }
                return;
            }
            int i2 = 0;
            int inPos2 = inPos;
            while (i2 < inAvail) {
                if (this.buffer == null || this.buffer.length - this.pos < this.encodeSize) {
                    resizeBuffer();
                }
                int i3 = this.modulus + 1;
                this.modulus = i3;
                this.modulus = i3 % 3;
                inPos = inPos2 + 1;
                int b = in[inPos2];
                if (b < 0) {
                    b += 256;
                }
                this.f14x = (this.f14x << 8) + b;
                if (this.modulus == 0) {
                    bArr = this.buffer;
                    i = this.pos;
                    this.pos = i + 1;
                    bArr[i] = this.encodeTable[(this.f14x >> 18) & 63];
                    bArr = this.buffer;
                    i = this.pos;
                    this.pos = i + 1;
                    bArr[i] = this.encodeTable[(this.f14x >> 12) & 63];
                    bArr = this.buffer;
                    i = this.pos;
                    this.pos = i + 1;
                    bArr[i] = this.encodeTable[(this.f14x >> 6) & 63];
                    bArr = this.buffer;
                    i = this.pos;
                    this.pos = i + 1;
                    bArr[i] = this.encodeTable[this.f14x & 63];
                    this.currentLinePos += 4;
                    if (this.lineLength > 0 && this.lineLength <= this.currentLinePos) {
                        System.arraycopy(this.lineSeparator, 0, this.buffer, this.pos, this.lineSeparator.length);
                        this.pos += this.lineSeparator.length;
                        this.currentLinePos = 0;
                    }
                }
                i2++;
                inPos2 = inPos;
            }
            inPos = inPos2;
        }
    }

    void decode(byte[] in, int inPos, int inAvail) {
        if (!this.eof) {
            byte[] bArr;
            int i;
            if (inAvail < 0) {
                this.eof = true;
            }
            int i2 = 0;
            int inPos2 = inPos;
            while (i2 < inAvail) {
                if (this.buffer == null || this.buffer.length - this.pos < this.decodeSize) {
                    resizeBuffer();
                }
                inPos = inPos2 + 1;
                byte b = in[inPos2];
                if (b == PAD) {
                    this.eof = true;
                    break;
                }
                if (b >= (byte) 0 && b < DECODE_TABLE.length) {
                    int result = DECODE_TABLE[b];
                    if (result >= 0) {
                        int i3 = this.modulus + 1;
                        this.modulus = i3;
                        this.modulus = i3 % 4;
                        this.f14x = (this.f14x << 6) + result;
                        if (this.modulus == 0) {
                            bArr = this.buffer;
                            i = this.pos;
                            this.pos = i + 1;
                            bArr[i] = (byte) ((this.f14x >> 16) & 255);
                            bArr = this.buffer;
                            i = this.pos;
                            this.pos = i + 1;
                            bArr[i] = (byte) ((this.f14x >> 8) & 255);
                            bArr = this.buffer;
                            i = this.pos;
                            this.pos = i + 1;
                            bArr[i] = (byte) (this.f14x & 255);
                        }
                    }
                }
                i2++;
                inPos2 = inPos;
            }
            if (this.eof && this.modulus != 0) {
                this.f14x <<= 6;
                switch (this.modulus) {
                    case 2:
                        this.f14x <<= 6;
                        bArr = this.buffer;
                        i = this.pos;
                        this.pos = i + 1;
                        bArr[i] = (byte) ((this.f14x >> 16) & 255);
                        return;
                    case 3:
                        bArr = this.buffer;
                        i = this.pos;
                        this.pos = i + 1;
                        bArr[i] = (byte) ((this.f14x >> 16) & 255);
                        bArr = this.buffer;
                        i = this.pos;
                        this.pos = i + 1;
                        bArr[i] = (byte) ((this.f14x >> 8) & 255);
                        return;
                    default:
                        return;
                }
            }
        }
    }

    public static boolean isBase64(byte octet) {
        return octet == PAD || (octet >= (byte) 0 && octet < DECODE_TABLE.length && DECODE_TABLE[octet] != (byte) -1);
    }

    public static boolean isArrayByteBase64(byte[] arrayOctet) {
        int i = 0;
        while (i < arrayOctet.length) {
            if (!isBase64(arrayOctet[i]) && !isWhiteSpace(arrayOctet[i])) {
                return false;
            }
            i++;
        }
        return true;
    }

    private static boolean containsBase64Byte(byte[] arrayOctet) {
        for (byte element : arrayOctet) {
            if (isBase64(element)) {
                return true;
            }
        }
        return false;
    }

    public static byte[] encodeBase64(byte[] binaryData) {
        return encodeBase64(binaryData, false);
    }

    public static String encodeBase64String(byte[] binaryData) {
        return newStringUtf8(encodeBase64(binaryData, true));
    }

    public static String encodeBase64StringUnChunked(byte[] binaryData) {
        return newStringUtf8(encodeBase64(binaryData, false));
    }

    public static String encodeBase64String(byte[] binaryData, boolean useChunking) {
        return newStringUtf8(encodeBase64(binaryData, useChunking));
    }

    public static byte[] encodeBase64URLSafe(byte[] binaryData) {
        return encodeBase64(binaryData, false, true);
    }

    public static String encodeBase64URLSafeString(byte[] binaryData) {
        return newStringUtf8(encodeBase64(binaryData, false, true));
    }

    public static byte[] encodeBase64Chunked(byte[] binaryData) {
        return encodeBase64(binaryData, true);
    }

    public byte[] decode(String pArray) {
        return decode(getBytesUtf8(pArray));
    }

    private byte[] getBytesUtf8(String pArray) {
        try {
            return pArray.getBytes("UTF8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] decode(byte[] pArray) {
        reset();
        if (pArray == null || pArray.length == 0) {
            return pArray;
        }
        byte[] buf = new byte[((int) ((long) ((pArray.length * 3) / 4)))];
        setInitialBuffer(buf, 0, buf.length);
        decode(pArray, 0, pArray.length);
        decode(pArray, 0, -1);
        byte[] result = new byte[this.pos];
        readResults(result, 0, result.length);
        return result;
    }

    public static byte[] encodeBase64(byte[] binaryData, boolean isChunked) {
        return encodeBase64(binaryData, isChunked, false);
    }

    public static byte[] encodeBase64(byte[] binaryData, boolean isChunked, boolean urlSafe) {
        return encodeBase64(binaryData, isChunked, urlSafe, Integer.MAX_VALUE);
    }

    public static byte[] encodeBase64(byte[] binaryData, boolean isChunked, boolean urlSafe, int maxResultSize) {
        if (binaryData == null || binaryData.length == 0) {
            return binaryData;
        }
        int i;
        if (isChunked) {
            i = 76;
        } else {
            i = 0;
        }
        long len = getEncodeLength(binaryData, i, isChunked ? CHUNK_SEPARATOR : EMPTY_BYTE_ARRAY);
        if (len > ((long) maxResultSize)) {
            throw new IllegalArgumentException("Input array too big, the output array would be bigger (" + len + ") than the specified maxium size of " + maxResultSize);
        }
        return (isChunked ? new Base64(urlSafe) : new Base64(0, CHUNK_SEPARATOR, urlSafe)).encode(binaryData);
    }

    public static byte[] decodeBase64(String base64String) {
        return new Base64().decode(base64String);
    }

    public static byte[] decodeBase64(byte[] base64Data) {
        return new Base64().decode(base64Data);
    }

    private static boolean isWhiteSpace(byte byteToCheck) {
        switch (byteToCheck) {
            case (byte) 9:
            case (byte) 10:
            case (byte) 13:
            case (byte) 32:
                return true;
            default:
                return false;
        }
    }

    public String encodeToString(byte[] pArray) {
        return newStringUtf8(encode(pArray));
    }

    private static String newStringUtf8(byte[] encode) {
        try {
            return new String(encode, "UTF8");
        } catch (UnsupportedEncodingException ue) {
            throw new RuntimeException(ue);
        }
    }

    public byte[] encode(byte[] pArray) {
        reset();
        if (pArray == null || pArray.length == 0) {
            return pArray;
        }
        byte[] buf = new byte[((int) getEncodeLength(pArray, this.lineLength, this.lineSeparator))];
        setInitialBuffer(buf, 0, buf.length);
        encode(pArray, 0, pArray.length);
        encode(pArray, 0, -1);
        if (this.buffer != buf) {
            readResults(buf, 0, buf.length);
        }
        if (!isUrlSafe() || this.pos >= buf.length) {
            return buf;
        }
        byte[] smallerBuf = new byte[this.pos];
        System.arraycopy(buf, 0, smallerBuf, 0, this.pos);
        return smallerBuf;
    }

    private static long getEncodeLength(byte[] pArray, int chunkSize, byte[] chunkSeparator) {
        chunkSize = (chunkSize / 4) * 4;
        long len = (long) ((pArray.length * 4) / 3);
        long mod = len % 4;
        if (mod != 0) {
            len += 4 - mod;
        }
        if (chunkSize <= 0) {
            return len;
        }
        boolean lenChunksPerfectly = len % ((long) chunkSize) == 0;
        len += (len / ((long) chunkSize)) * ((long) chunkSeparator.length);
        if (lenChunksPerfectly) {
            return len;
        }
        return len + ((long) chunkSeparator.length);
    }

    public static BigInteger decodeInteger(byte[] pArray) {
        return new BigInteger(1, decodeBase64(pArray));
    }

    public static byte[] encodeInteger(BigInteger bigInt) {
        if (bigInt != null) {
            return encodeBase64(toIntegerBytes(bigInt), false);
        }
        throw new NullPointerException("encodeInteger called with null parameter");
    }

    static byte[] toIntegerBytes(BigInteger bigInt) {
        int bitlen = ((bigInt.bitLength() + 7) >> 3) << 3;
        byte[] bigBytes = bigInt.toByteArray();
        if (bigInt.bitLength() % 8 != 0 && (bigInt.bitLength() / 8) + 1 == bitlen / 8) {
            return bigBytes;
        }
        int startSrc = 0;
        int len = bigBytes.length;
        if (bigInt.bitLength() % 8 == 0) {
            startSrc = 1;
            len--;
        }
        byte[] resizedBytes = new byte[(bitlen / 8)];
        System.arraycopy(bigBytes, startSrc, resizedBytes, (bitlen / 8) - len, len);
        return resizedBytes;
    }

    private void reset() {
        this.buffer = null;
        this.pos = 0;
        this.readPos = 0;
        this.currentLinePos = 0;
        this.modulus = 0;
        this.eof = false;
    }

    int getLineLength() {
        return this.lineLength;
    }

    byte[] getLineSeparator() {
        return (byte[]) this.lineSeparator.clone();
    }
}
