package org.redoubt.protocol.as2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
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
import org.redoubt.protocol.BaseProtocol;
import org.redoubt.protocol.ProtocolException;
import org.redoubt.transport.TransportConstants;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
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
            SMIMECompressedGenerator  gen = new SMIMECompressedGenerator();
              
            MimeBodyPart msg = new MimeBodyPart();
    
            String fullTarget = (String) context.get(TransportConstants.CONTEXT_FULL_TARGET);
            File workFile = new File(fullTarget);
            
            msg.setDataHandler(new DataHandler(new FileDataSource(workFile)));
            msg.setHeader("Content-Type", "application/octet-stream");
            msg.setHeader("Content-Transfer-Encoding", "binary");
    
            MimeBodyPart mp = gen.generate(msg, new ZlibCompressor());
    
            Properties props = System.getProperties();
            Session session = Session.getDefaultInstance(props, null);
    
            Address fromUser = new InternetAddress(settings.getFrom());
            Address toUser = new InternetAddress(settings.getTo());
    
            MimeMessage body = new MimeMessage(session);
            body.setFrom(fromUser);
            body.setRecipient(Message.RecipientType.TO, toUser);
            body.setContent(mp.getContent(), mp.getContentType());
            body.saveChanges();
            
            body.writeTo(new FileOutputStream(workFile));
            
            HttpClientUtils.sendPostRequest(settings, workFile.toPath());
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

        // Encrypt and/or sign the data if requested
        if (encrypt || sign) {
        	ICertificateManager certificateManager = Factory.getInstance().getCertificateManager();
        	ICryptoHelper cryptoHelper = Factory.getInstance().getCryptoHelper();

            // Sign the data if requested
            if (sign) {
                X509Certificate signingCert = certificateManager.getX509Certificate(settings.getSignCertAlias());
                PrivateKey senderKey = certificateManager.getPrivateKey(settings.getSignCertAlias(), settings.getSignCertKeyPassword().toCharArray());
                String digest = settings.getSignDigestAlgorithm();

                dataBP = cryptoHelper.sign(dataBP, signingCert, senderKey, digest);

                //sLogger.debug("signed data" + msg.getLoggingText());
            }

            // Encrypt the data if requested
            if (encrypt) {
                String algorithm = settings.getEncryptAlgorithm();
                X509Certificate receiverCert = certificateManager.getX509Certificate(settings.getEncryptCertAlias());
                dataBP = cryptoHelper.encrypt(dataBP, receiverCert, algorithm);

                //sLogger.debug("encrypted data" + msg.getLoggingText());
            }
        }

        return dataBP;
    }
	
}
