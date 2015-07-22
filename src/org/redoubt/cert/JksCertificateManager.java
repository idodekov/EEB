package org.redoubt.cert;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.KeyStore;

import org.apache.log4j.Logger;
import org.redoubt.api.configuration.ICertificateManager;
import org.redoubt.api.configuration.IServerConfigurationManager;
import org.redoubt.api.factory.Factory;
import org.redoubt.api.factory.FactoryConstants;


public class JksCertificateManager implements ICertificateManager {
    private static final Logger sLogger = Logger.getLogger(JksCertificateManager.class);
    private KeyStore keystore;

    @Override
    public void init() {
        IServerConfigurationManager configurationManager = Factory.getInstance().getServerConfigurationManager();
        Path keystoreFile = configurationManager.getKeystoreFile();
        char[] password = configurationManager.getKeystorePassword().toCharArray();
        
        try(InputStream fIn = new FileInputStream(keystoreFile.toFile());) {
            keystore = KeyStore.getInstance(getKeystoreType());
            keystore.load(fIn, password);
        } catch (Exception e) {
            sLogger.error("An error has occured while initializing CertificateManager! " + e.getMessage(), e);
        }
    }

    @Override
    public String getKeystoreType() {
        return FactoryConstants.CERTIFICATE_MANAGER_JKS;
    }
}
