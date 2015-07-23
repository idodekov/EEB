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
    private KeyStore truststore;

    @Override
    public void init() {
        IServerConfigurationManager configurationManager = Factory.getInstance().getServerConfigurationManager();
        Path keystoreFile = configurationManager.getKeystoreFile();
        char[] keystorePassword = configurationManager.getKeystorePassword().toCharArray();
        
        Path truststoreFile = configurationManager.getTruststoreFile();
        char[] truststorePassword = configurationManager.getTruststorePassword().toCharArray();
        
        try(InputStream keystoreStream = new FileInputStream(keystoreFile.toFile());
                InputStream truststoreStream = new FileInputStream(truststoreFile.toFile());) {
            sLogger.info("Loading keystore from [" + keystoreFile.toString() + "].");
            keystore = KeyStore.getInstance(getKeystoreType());
            keystore.load(keystoreStream, keystorePassword);
            
            sLogger.info("Loading truststore from [" + truststoreFile.toString() + "].");
            truststore = KeyStore.getInstance(getKeystoreType());
            truststore.load(truststoreStream, truststorePassword);
        } catch (Exception e) {
            sLogger.error("An error has occured while initializing CertificateManager! " + e.getMessage(), e);
            sLogger.error("APPLICATION WILL BE TERMMINATED.");
            System.exit(1);
        }
    }

    @Override
    public String getKeystoreType() {
        return FactoryConstants.CERTIFICATE_MANAGER_JKS;
    }
}
