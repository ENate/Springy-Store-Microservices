package com.siriusxi.cloud.infra.auth.server.jose;

import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.RSAKey;
// import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpoint

import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.OctetSequenceKey;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

/**
 * Legacy Authorization Server (spring-security-oauth2) does not support any <a
 * href target="_blank"
 * href="https://tools.ietf.org/html/rfc7517#section-5">JWK Set</a> endpoint.
 *
 * <p>
 * This class adds ad-hoc support in order to better support the other samples
 * in the repo.
 */
public class JwkSetEndpoint {
    public JwkSetEndpoint() {
    }

    public static RSAKey generateRsa() {
        KeyPair keyPair = KeyGeneratorUtils.generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        return new RSAKey.Builder(publicKey).privateKey(privateKey).keyID(UUID.randomUUID().toString()).build();
    }

    public static ECKey generateEc() {
        KeyPair keyPair = KeyGeneratorUtils.generateEcKey();
        ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();
        ECPrivateKey privateKey = (ECPrivateKey) keyPair.getPrivate();
        Curve curve = Curve.forECParameterSpec(publicKey.getParams());
        return new ECKey.Builder(curve, publicKey)
                .privateKey(privateKey).keyID(UUID.randomUUID().toString()).build();
    }

    public static OctetSequenceKey generateSecret() {
        SecretKey secretKey = KeyGeneratorUtils.generateSecretKey();
        return new OctetSequenceKey.Builder(secretKey).keyID(UUID.randomUUID().toString()).build();
    }
}
