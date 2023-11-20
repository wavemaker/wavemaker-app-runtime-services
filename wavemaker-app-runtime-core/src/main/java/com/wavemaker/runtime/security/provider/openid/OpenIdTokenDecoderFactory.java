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

package com.wavemaker.runtime.security.provider.openid;

import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.UrlResource;
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenDecoderFactory;
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenValidator;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.converter.ClaimTypeConverter;
import org.springframework.security.oauth2.jose.jws.JwsAlgorithm;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import com.wavemaker.commons.WMRuntimeException;

import static org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenDecoderFactory.createDefaultClaimTypeConverters;

/**
 * OpenIdTokenDecoderFactory creates a NimbusJwtDecoder using either JwkSetUrl or public key from jwkSetUri property.
 * If the property has only JwkSetUrl it uses the default OidcIdTokenDecoderFactory.
 * If the property has both JwkSetUrl and file path then file path is used and falls back to url in case of any exceptions.
 */
public class OpenIdTokenDecoderFactory implements JwtDecoderFactory<ClientRegistration> {

    private static final Logger logger = LoggerFactory.getLogger(OpenIdTokenDecoderFactory.class);
    private static final String MISSING_SIGNATURE_VERIFIER_ERROR_CODE = "missing_signature_verifier";
    private static final String JWK_URI_SEPARATOR = ";";
    private Function<ClientRegistration, JwsAlgorithm> jwsAlgorithmResolver = clientRegistration -> SignatureAlgorithm.RS256;
    private Function<ClientRegistration, OAuth2TokenValidator<Jwt>> jwtValidatorFactory = clientRegistration ->
        new DelegatingOAuth2TokenValidator<>(new JwtTimestampValidator(),
            new OidcIdTokenValidator(clientRegistration));
    private static final ClaimTypeConverter DEFAULT_CLAIM_TYPE_CONVERTER = new ClaimTypeConverter(
        createDefaultClaimTypeConverters());
    private Function<ClientRegistration, Converter<Map<String, Object>, Map<String, Object>>> claimTypeConverterFactory =
        clientRegistration -> DEFAULT_CLAIM_TYPE_CONVERTER;
    private final Map<String, JwtDecoder> jwtDecoders = new ConcurrentHashMap<>();
    private JwtDecoderFactory<ClientRegistration> jwtDecoderFactory = new OidcIdTokenDecoderFactory();

    @Override
    public JwtDecoder createDecoder(ClientRegistration clientRegistration) {
        String jwkSetUri = clientRegistration.getProviderDetails().getJwkSetUri();
        if (StringUtils.isBlank(jwkSetUri)) {
            throw new WMRuntimeException("jwkSet url or public key file path not specified");
        }
        if (!isPublicKeyFilePathSpecified(jwkSetUri)) {
            logger.debug("Using default OidcIdTokenDecoderFactory with JwkSetUri");
            return jwtDecoderFactory.createDecoder(clientRegistration);
        } else {
            return this.jwtDecoders.computeIfAbsent(clientRegistration.getRegistrationId(), key -> {
                NimbusJwtDecoder jwtDecoder = buildDecoder(clientRegistration);
                jwtDecoder.setJwtValidator(this.jwtValidatorFactory.apply(clientRegistration));
                Converter<Map<String, Object>, Map<String, Object>> claimTypeConverter = this.claimTypeConverterFactory
                    .apply(clientRegistration);
                if (claimTypeConverter != null) {
                    jwtDecoder.setClaimSetConverter(claimTypeConverter);
                }
                return jwtDecoder;
            });
        }
    }

    private NimbusJwtDecoder buildDecoder(ClientRegistration clientRegistration) {
        JwsAlgorithm jwsAlgorithm = this.jwsAlgorithmResolver.apply(clientRegistration);
        if (jwsAlgorithm != null && SignatureAlgorithm.class.isAssignableFrom(jwsAlgorithm.getClass())) {
            String jwkSetUri = clientRegistration.getProviderDetails().getJwkSetUri();
            try {
                RSAPublicKey rsaPublicKey = (RSAPublicKey) readPublicKey(jwkSetUri);
                return NimbusJwtDecoder.withPublicKey(rsaPublicKey).signatureAlgorithm((SignatureAlgorithm) jwsAlgorithm).build();
            } catch (IOException e) {
                logger.warn("Failed to read certificate from the file path. Falling back to jwkSetUrl", e);
            } catch (CertificateException e) {
                logger.warn("Failed to read jwkSet public key from the certificate. Falling back to jwkSetUrl", e);
            } catch (IllegalStateException e) {
                logger.warn("The public key in the certificate is not a RSAPublicKey. Falling back to jwkSetUrl", e);
            }
            String jwkSetUrl = extractJwkSetUrl(jwkSetUri);
            return NimbusJwtDecoder.withJwkSetUri(jwkSetUrl).jwsAlgorithm((SignatureAlgorithm) jwsAlgorithm).build();
        }
        OAuth2Error oauth2Error = new OAuth2Error(MISSING_SIGNATURE_VERIFIER_ERROR_CODE,
            "Failed to find a Signature Verifier for Client Registration: '"
                + clientRegistration.getRegistrationId()
                + "'. Check to ensure you have configured a valid JWS Algorithm: '" + jwsAlgorithm + "'",
            null);
        throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
    }

    private PublicKey readPublicKey(String jwkSetPath) throws IOException, CertificateException {
        String jwkSetCertificatePath = extractJwkSetCertificatePath(jwkSetPath);
        UrlResource resource = new UrlResource(jwkSetCertificatePath);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        Certificate certificate = certificateFactory.generateCertificate(resource.getInputStream());
        PublicKey publicKey = certificate.getPublicKey();
        if (!(publicKey instanceof RSAPublicKey)) {
            throw new IllegalStateException("Expected Public key format is RSAPublicKey");
        }
        return publicKey;
    }

    private boolean isPublicKeyFilePathSpecified(String jwkSetUri) {
        return jwkSetUri.contains("classpath:") || jwkSetUri.contains("file:");
    }

    private String extractJwkSetUrl(String jwkSetUri) {
        return Arrays.stream(jwkSetUri.split(JWK_URI_SEPARATOR)).filter(s -> s.startsWith("http")).findFirst()
            .orElseThrow(() -> new IllegalStateException("jwkSetUri does not contain url"));
    }

    private String extractJwkSetCertificatePath(String jwkSetUri) {
        return Arrays.stream(jwkSetUri.split(JWK_URI_SEPARATOR)).filter(s -> s.startsWith("classpath:") || s.startsWith("file:")).findFirst()
            .orElseThrow(() -> new IllegalStateException("jwkSetUri does not contain file path"));
    }
}
