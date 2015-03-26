/*
 *  Copyright (C) 2012-2013 CloudJee, Inc. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.wavemaker.tools.cloudfoundry.spinup.authentication;

import java.util.Arrays;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;

import com.wavemaker.tools.cloudfoundry.spinup.util.HexString;

/**
 * A security token that can be stored and transported between systems. Transport tokens are {@link AuthenticationToken
 * AuthenticationTokens} encrypted using a {@link SharedSecret}.
 * 
 * @see AuthenticationToken
 */
public final class TransportToken {

    private final byte[] digest;

    private final byte[] encryptedToken;

    public TransportToken(byte[] digest, byte[] encryptedToken) {
        Assert.notNull(digest, "Digest must not be null");
        Assert.notNull(encryptedToken, "Encrypted Token must not be null");
        this.digest = digest;
        this.encryptedToken = encryptedToken;
    }

    public byte[] getEncryptedToken() {
        return this.encryptedToken;
    }

    public boolean isDigestMatch(byte[] digest) {
        if (digest == null) {
            return false;
        }
        return Arrays.equals(this.digest, digest);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.digest) + Arrays.hashCode(this.encryptedToken);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TransportToken other = (TransportToken) obj;
        return Arrays.equals(this.digest, other.digest) && Arrays.equals(this.encryptedToken, other.encryptedToken);
    }

    public String encode() {
        return HexString.toString(this.digest) + "." + HexString.toString(this.encryptedToken);
    }

    @Override
    public String toString() {
        return new ToStringCreator(this).append("digest", HexString.toString(this.digest)).append("encryptedToken",
            HexString.toString(this.encryptedToken)).toString();
    }

    public static TransportToken decode(String encodedTransportToken) {
        Assert.notNull(encodedTransportToken, "EncodedTransportToken must not be null");
        String[] tokenParts = encodedTransportToken.split("\\.");
        try {
            Assert.state(tokenParts.length == 2, "Expected two parts in encoded transport token");
            byte[] digest = HexString.toBytes(tokenParts[0]);
            byte[] encryptedToken = HexString.toBytes(tokenParts[1]);
            return new TransportToken(digest, encryptedToken);
        } catch (Exception e) {
            throw new IllegalStateException("Malformed EncodedTransportToken", e);
        }
    }

}
