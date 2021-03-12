package com.wavemaker.runtime.util;

import java.security.SecureRandom;

public class WMRandomUtils {

    private static final char ALPHA_NUMERIC_CHARACTERS[] = "0123456789BCDFGHJKLMNPQRSTVWXYZbcdfghjklmnpqrstvwxyz".toCharArray();

    private static final SecureRandom secureRandom = new SecureRandom();

    public static String getRandomString(int length) {

        StringBuilder sb = new StringBuilder();

        for (int loop = 0; loop < length; ++loop) {
            int index = secureRandom.nextInt(ALPHA_NUMERIC_CHARACTERS.length);
            sb.append(ALPHA_NUMERIC_CHARACTERS[index]);
        }

        return sb.toString();
    }


}
