/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.wavemaker.runtime.commons.util;

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
