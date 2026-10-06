package com.weng.weng;

import java.security.MessageDigest;

/** 本地敏感字段（API Key 等）的轻量加密：密文落盘，读取时解密。
 *  方案与内嵌人设同源：SHA-256(固定种子) 派生密钥 + CTR 流 XOR + base64。
 *  这不是防逆向的强加密（密钥在包内），目标是让明文不直接躺在 SharedPreferences 里。
 *  兼容旧数据：遇到没有 "enc:" 前缀的值（历史明文）原样返回，不丢数据、不误判。 */
public class Crypto {

    private static final String SEED = "weng.weng.apiKey.v1#7F3a9C1bE2d4";

    private static byte[] key() throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(SEED.getBytes("UTF-8"));
    }

    /** CTR 流 XOR：data 与 keyBytes 派生的密钥流按字节异或（加密/解密同一步骤） */
    private static byte[] xor(byte[] data, byte[] keyBytes, long counter) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] out = new byte[data.length];
        int made = 0;
        long c = counter;
        while (made < data.length) {
            md.reset();
            md.update(keyBytes);
            md.update((byte) (c >> 24));
            md.update((byte) (c >> 16));
            md.update((byte) (c >> 8));
            md.update((byte) c);
            byte[] block = md.digest();
            int n = Math.min(32, data.length - made);
            for (int i = 0; i < n; i++) out[made + i] = (byte) (data[made + i] ^ block[i]);
            made += n;
            c++;
        }
        return out;
    }

    /** 明文 → "enc:<16进制 nonce>:<base64 密文>"；失败回退明文（不阻断主流程） */
    public static String encrypt(String plain) {
        if (plain == null || plain.length() == 0) return "";
        try {
            java.security.SecureRandom sr = new java.security.SecureRandom();
            long nonce = sr.nextLong() & 0x7FFFFFFFFFFFFFFFL;
            byte[] ct = xor(plain.getBytes("UTF-8"), key(), nonce);
            return "enc:" + Long.toHexString(nonce) + ":"
                    + android.util.Base64.encodeToString(ct, android.util.Base64.NO_WRAP);
        } catch (Exception e) {
            return plain;
        }
    }

    /** 密文 → 明文；无 "enc:" 前缀（旧明文）或解析失败 → 原样返回 */
    public static String decrypt(String enc) {
        if (enc == null || enc.length() == 0) return "";
        if (!enc.startsWith("enc:")) return enc;
        try {
            String[] parts = enc.split(":", 3);
            if (parts.length != 3) return enc;
            long nonce = Long.parseLong(parts[1], 16);
            byte[] ct = android.util.Base64.decode(parts[2], android.util.Base64.NO_WRAP);
            return new String(xor(ct, key(), nonce), "UTF-8");
        } catch (Exception e) {
            return enc;
        }
    }
}
