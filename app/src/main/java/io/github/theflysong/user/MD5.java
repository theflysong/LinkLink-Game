package io.github.theflysong.user;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * 
 *
 * @author norbe
 * @date 2026年5月07日
 */

public class MD5 {
    private static final int A = 0x67452301;
    private static final int B = 0xEFCDAB89;
    private static final int C = 0x98BADCFE;
    private static final int D = 0x10325476;

    private static final int[] S = {
        7, 12, 17, 22, 7, 12, 17, 22, 7, 12, 17, 22, 7, 12, 17, 22,
        5, 9,  14, 20, 5, 9,  14, 20, 5, 9,  14, 20, 5, 9,  14, 20,
        4, 11, 16, 23, 4, 11, 16, 23, 4, 11, 16, 23, 4, 11, 16, 23,
        6, 10, 15, 21, 6 ,10 ,15 ,21 ,6 ,10 ,15 ,21 ,6 ,10 ,15 ,21
    };

    private static final int[] K = new int[64];
    static {
        for (int i = 0; i < 64; i++) {
            K[i] = (int) ((long) (Math.abs(Math.sin(i + 1)) * (1L << 32)));
        }
    }
    
    private static int[] toIntArray(byte[] bytes) {
        int[] result = new int[16];
        for (int i = 0; i < 16; i++) {
            result[i] = ((bytes[i * 4] & 0xFF)) |
                        ((bytes[i * 4 + 1] & 0xFF) << 8) |
                        ((bytes[i * 4 + 2] & 0xFF) << 16) |
                        ((bytes[i * 4 + 3] & 0xFF) << 24);
        }
        return result;
    }
    private static byte[] PadMessage(byte[] message) {
        int originalLength = message.length;
        int paddingLength = (56 - (originalLength + 1) % 64 + 64) % 64;
        byte[] paddedMessage = new byte[originalLength + 1 + paddingLength + 8];
        System.arraycopy(message, 0, paddedMessage, 0, originalLength);
        paddedMessage[originalLength] = (byte) 0x80; // Append '1' bit
        long bitLength = (long) originalLength * 8;
        for (int i = 0; i < 8; i++) {
            paddedMessage[paddedMessage.length - 8 + i] = (byte) ((bitLength >>> (i * 8)) & 0xFF);
        }
        return paddedMessage;
    }

    public static String MD5hash(String input) {
        byte[] message = input.getBytes();
        byte[] paddedMessage = PadMessage(message);

        int a = A, b = B, c = C, d = D;

        for (int i = 0; i < paddedMessage.length / 64; i++) {
            byte[] block = Arrays.copyOfRange(paddedMessage, i * 64, (i + 1) * 64);
            // Process each 512-bit block

            int originalA = a;
            int originalB = b;
            int originalC = c;
            int originalD = d;

            int[] M = toIntArray(block);

            for(int j = 0; j < 64; j++) {
                int F, g;
                if (j < 16) {
                    F = (b & c) | (~b & d);
                    g = j;
                } else if (j < 32) {
                    F = (d & b) | (~d & c);
                    g = (5 * j + 1) % 16;
                } else if (j < 48) {
                    F = b ^ c ^ d;
                    g = (3 * j + 5) % 16;
                } else {
                    F = c ^ (b | ~d);
                    g = (7 * j) % 16;
                }
                int temp = d;
                d = c;
                c = b;
                b = b + Integer.rotateLeft(a + F + K[j] + M[g], S[j]);
                a = temp;
            }

            a += originalA;
            b += originalB;
            c += originalC;
            d += originalD;
        }

        byte[] hash = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN)
            .putInt(a).putInt(b).putInt(c).putInt(d).array();

        StringBuilder sb = new StringBuilder();
        for (byte bVal : hash) {
            String hex = Integer.toHexString(bVal & 0xFF);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }

        return sb.toString();
    }
}
