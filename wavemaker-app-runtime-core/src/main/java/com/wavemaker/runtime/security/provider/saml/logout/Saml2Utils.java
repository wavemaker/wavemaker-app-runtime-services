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

package com.wavemaker.runtime.security.provider.saml.logout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

import org.springframework.security.saml2.Saml2Exception;

public class Saml2Utils {

    private Saml2Utils() {
    }

    static String samlEncode(byte[] b) {
        return Base64.getEncoder().encodeToString(b);
    }

    static byte[] samlDecode(String s) {
        return Base64.getDecoder().decode(s);
    }

    static byte[] samlDeflate(String s) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DeflaterOutputStream deflater = new DeflaterOutputStream(b, new Deflater(Deflater.DEFLATED, true));
            deflater.write(s.getBytes(StandardCharsets.UTF_8));
            deflater.finish();
            return b.toByteArray();
        }
        catch (IOException ex) {
            throw new Saml2Exception("Unable to deflate string", ex);
        }
    }

    static String samlInflate(byte[] b) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InflaterOutputStream iout = new InflaterOutputStream(out, new Inflater(true));
            iout.write(b);
            iout.finish();
            return out.toString(StandardCharsets.UTF_8);
        }
        catch (IOException ex) {
            throw new Saml2Exception("Unable to inflate string", ex);
        }
    }
}
