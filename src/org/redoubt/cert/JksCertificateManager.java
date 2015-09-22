package org.redoubt.cert;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;

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
    
    public X509Certificate getX509Certificate(String alias) {
    	try {
			X509Certificate cert = (X509Certificate) keystore.getCertificate(alias);
			
			if (cert == null) {
                throw new KeyStoreException(alias);
            }
			
			checkCertificateValidity(cert);
			
			return cert;
		} catch (Exception e) {
			sLogger.error("An error has occured while fetching certificate with alias [" + alias + "] in keystore! " + e.getMessage(), e);
			return null;
		}
    }
    
    public PrivateKey getPrivateKey(String alias, char[] password) {
    	PrivateKey key;
		try {
			key = (PrivateKey) keystore.getKey(alias, password);
			
			if (key == null) {
	            throw new KeyStoreException(alias);
	        }

	        return key;
		} catch (Exception e) {
			sLogger.error("An error has occured while fetching key with alias [" + alias + "] in keystore! " + e.getMessage(), e);
			return null;
		}
    }
    
    @Override
    public boolean isSelfSigned(X509Certificate cert) throws Exception {
        try {
            PublicKey key = cert.getPublicKey();
            cert.verify(key);
            return true;
        } catch (SignatureException sigEx) {
            // Invalid signature --> not self-signed
            return false;
        } catch (InvalidKeyException keyEx) {
            // Invalid key --> not self-signed
            return false;
        }
    }

	@Override
	public boolean checkCertificateValidity(X509Certificate cert) throws Exception {
		Date today = new Date();
		Date notAfter = cert.getNotAfter(); 
		Date notBefore = cert.getNotBefore();
		
		if(today.before(notBefore)) {
			throw new CertificateNotYetValidException("Certificate is not yet valid!");
		}
		
		if(today.after(notAfter)) {
			throw new CertificateExpiredException("Certificate has expired!");
		}
		
		return true;
	}
}
