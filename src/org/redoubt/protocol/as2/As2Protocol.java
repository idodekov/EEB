package org.redoubt.protocol.as2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.apache.log4j.Logger;
import org.bouncycastle.cms.jcajce.ZlibCompressor;
import org.bouncycastle.mail.smime.SMIMECompressedGenerator;
import org.redoubt.api.configuration.ICertificateManager;
import org.redoubt.api.configuration.ICryptoHelper;
import org.redoubt.api.factory.Factory;
import org.redoubt.api.protocol.TransferContext;
import org.redoubt.fs.util.FileSystemUtils;
import org.redoubt.protocol.BaseProtocol;
import org.redoubt.protocol.ProtocolException;
import org.redoubt.transport.TransportConstants;

import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;

public class As2Protocol extends BaseProtocol {
    private static final Logger sLogger = Logger.getLogger(As2Protocol.class);

    @Override
    public void receive(TransferContext context) throws ProtocolException {
        As2ProtocolSettings settings = (As2ProtocolSettings) getSettings();
        
        Path productionFolder = settings.getProductionFolder();
        String fullTarget = (String) context.get(TransportConstants.CONTEXT_FULL_TARGET);
        Path destination = Paths.get(fullTarget);
        
        try {
            Files.copy(destination, Paths.get(productionFolder.toString(), destination.getFileName().toString()));
        } catch (IOException e) {
            sLogger.error("An error has occured while copying destination file [" + destination.toString() + "].", e);
        }
    }

    @Override
    public void send(TransferContext context) throws ProtocolException {
        As2ProtocolSettings settings = (As2ProtocolSettings) getSettings();
        try {
            MimeBodyPart msg = new MimeBodyPart();
    
            String fullTarget = (String) context.get(TransportConstants.CONTEXT_FULL_TARGET);
            File workFile = new File(fullTarget);
            
            msg.setDataHandler(new DataHandler(new FileDataSource(workFile)));
            msg.setHeader("Content-Type", "application/octet-stream");
            msg.setHeader("Content-Transfer-Encoding", "binary");
    
            msg = secure(msg);
            
            Properties props = System.getProperties();
            Session session = Session.getDefaultInstance(props, null);
    
            MimeMessage body = new MimeMessage(session);
            body.setHeader(As2HeaderDictionary.AS2_FROM, settings.getFrom());
            body.setHeader(As2HeaderDictionary.AS2_TO, settings.getTo());
            body.setHeader(As2HeaderDictionary.AS2_VERSION, As2HeaderDictionary.AS2_VERSION_1_1);
            
            body.setSentDate(new Date());
            body.setContent(msg.getContent(), msg.getContentType());
            body.saveChanges();
            Path tempStorage = FileSystemUtils.createWorkFile();
            
            FileOutputStream fos = new FileOutputStream(tempStorage.toFile());
            body.writeTo(fos);
            fos.close();
            
            HttpClientUtils.sendPostRequest(settings, tempStorage);
            
            FileSystemUtils.moveFile(tempStorage, workFile.toPath(), true);
        }  catch (Exception e) {
            sLogger.error("An error has occured while packaging As2 message. " + e.getMessage(), e);
            throw new ProtocolException(e.getMessage(), e);
        }
        
    }
    
    protected MimeBodyPart secure(MimeBodyPart dataBP) throws Exception {
    	As2ProtocolSettings settings = (As2ProtocolSettings) getSettings();
        // Set up encrypt/sign variables
        boolean encrypt = settings.isEncryptionEnabled();
        boolean sign = settings.isSigningEnabled();
        boolean compress = settings.isCompressionEnabled();

        // Encrypt and/or sign the data if requested
        if (encrypt || sign || compress) {
        	ICertificateManager certificateManager = Factory.getInstance().getCertificateManager();
        	ICryptoHelper cryptoHelper = Factory.getInstance().getCryptoHelper();

            // Sign the data if requested
            if (sign) {
                sLogger.debug("Signing is enabled - will attempt to sign the message.");
                X509Certificate signingCert = certificateManager.getX509Certificate(settings.getSignCertAlias());
                PrivateKey senderKey = certificateManager.getPrivateKey(settings.getSignCertAlias(), settings.getSignCertKeyPassword().toCharArray());
                String digest = settings.getSignDigestAlgorithm();

                dataBP = cryptoHelper.sign(dataBP, signingCert, senderKey, digest);
            }
            
            // Compress the data if requested
            if(compress) {
            	sLogger.debug("Compression is enabled - will attempt to compress the message.");
            	SMIMECompressedGenerator  gen = new SMIMECompressedGenerator();
            	dataBP = gen.generate(dataBP, new ZlibCompressor());
            }

            // Encrypt the data if requested
            if (encrypt) {
                sLogger.debug("Encryption is enabled - will attempt to encrypt the message.");
                String algorithm = settings.getEncryptAlgorithm();
                X509Certificate receiverCert = certificateManager.getX509Certificate(settings.getEncryptCertAlias());
                dataBP = cryptoHelper.encrypt(dataBP, receiverCert, algorithm);
            }
        }

        return dataBP;
    }
	
}
