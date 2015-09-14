package org.redoubt.api.configuration;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public interface ICertificateManager {
    void init();
    String getKeystoreType();
    X509Certificate getX509Certificate(String alias);
    PrivateKey getPrivateKey(String alias, char[] password);
}
