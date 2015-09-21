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
    static final String COMPRESS_ZLIB = "zlib";
    
    void init();
    
    void deinit();

    boolean isEncrypted(MimeBodyPart part) throws Exception;

    boolean isSigned(MimeBodyPart part) throws Exception;

    String calculateMIC(MimeBodyPart part, String digest, boolean includeHeaders) throws Exception;

    MimeBodyPart decrypt(MimeBodyPart part, X509Certificate cert, PrivateKey key) throws Exception;

    MimeBodyPart encrypt(MimeBodyPart part, X509Certificate cert, String algorithm) throws Exception;
    
    MimeBodyPart sign(MimeBodyPart part, X509Certificate cert, PrivateKey key, String digest) throws Exception;

    MimeBodyPart verify(MimeBodyPart part) throws Exception;
    
    MimeBodyPart compress(MimeBodyPart part, String alg) throws Exception;
    
    MimeBodyPart decompress(MimeBodyPart part) throws Exception;
}