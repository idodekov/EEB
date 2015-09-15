package org.redoubt.api.configuration;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.mail.internet.MimeBodyPart;

public interface ICryptoHelper {
    static final String DIGEST_MD5 = "md5";
    static final String DIGEST_SHA1 = "sha1";
    static final String CRYPT_CAST5 = "cast5";
    static final String CRYPT_3DES = "3des";
    static final String CRYPT_IDEA = "idea";
    static final String CRYPT_RC2 = "rc2";

    boolean isEncrypted(MimeBodyPart part) throws Exception;

    boolean isSigned(MimeBodyPart part) throws Exception;

    String calculateMIC(MimeBodyPart part, String digest, boolean includeHeaders) throws Exception;

    MimeBodyPart decrypt(MimeBodyPart part, X509Certificate cert, PrivateKey key) throws Exception;

    void deinit();

    MimeBodyPart encrypt(MimeBodyPart part, X509Certificate cert, String algorithm) throws Exception;

    void init();

    MimeBodyPart sign(MimeBodyPart part, X509Certificate cert, PrivateKey key, String digest) throws Exception;

    MimeBodyPart verify(MimeBodyPart part, X509Certificate cert) throws Exception;
}