package beidanci.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

public class MD5Utils {
    public MD5Utils() {
    }

    public static String md5(String string) {
        if (string != null && string.trim().length() >= 1) {
            try {
                return getMD5(string.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException var2) {
                throw new RuntimeException(var2.getMessage(), var2);
            }
        } else {
            return null;
        }
    }

    public static String md52(String string) {
        if (string != null && string.trim().length() >= 1) {
            try {
                return getMD52(string.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException var2) {
                throw new RuntimeException(var2.getMessage(), var2);
            }
        } else {
            return null;
        }
    }

    private static String getMD5(byte[] source) {
        String s = null;
        char[] hexDigits = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(source);
            byte[] tmp = md.digest();
            char[] str = new char[32];
            int k = 0;

            for(int i = 0; i < 16; ++i) {
                byte byte0 = tmp[i];
                str[k++] = hexDigits[byte0 >>> 4 & 15];
                str[k++] = hexDigits[byte0 & 15];
            }

            s = new String(str);
        } catch (Exception var9) {
            var9.printStackTrace();
        }

        return s;
    }

    private static String getMD52(byte[] source) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            StringBuffer result = new StringBuffer();
            byte[] var6;
            int var5 = (var6 = md5.digest(source)).length;

            for(int var4 = 0; var4 < var5; ++var4) {
                byte b = var6[var4];
                result.append(Integer.toHexString((b & 240) >>> 4));
                result.append(Integer.toHexString(b & 15));
            }

            return result.toString();
        } catch (Exception var7) {
            var7.printStackTrace();
            return "";
        }
    }
}
