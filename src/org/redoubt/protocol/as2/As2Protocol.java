package org.redoubt.protocol.as2;

import java.io.File;
import java.io.FileOutputStream;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.cms.jcajce.ZlibCompressor;
import org.bouncycastle.mail.smime.SMIMECompressedGenerator;
import org.openas2.cert.CertificateFactory;
import org.openas2.message.DataHistoryItem;
import org.openas2.partner.Partnership;
import org.openas2.partner.SecurePartnership;
import org.openas2.util.AS2UtilOld;
import org.redoubt.api.configuration.ICertificateManager;
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
        // TODO Auto-generated method stub
        
    }

    @Override
    public void send(TransferContext context) throws ProtocolException {
        As2ProtocolSettings settings = (As2ProtocolSettings) getSettings();
        try {
            SMIMECompressedGenerator  gen = new SMIMECompressedGenerator();
              
            MimeBodyPart msg = new MimeBodyPart();
    
            context.get(TransportConstants.CONTEXT_FULL_TARGET);
            msg.setDataHandler(new DataHandler(new FileDataSource(new File((String) context.get(TransportConstants.CONTEXT_FULL_TARGET)))));
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
//            body.setSubject("example compressed message");
            body.setContent(mp.getContent(), mp.getContentType());
            body.saveChanges();
            //body.writeTo(new FileOutputStream("E:\\git\\Redoubt\\test.file"));
            
            HttpClientUtils.sendPostRequest(settings);
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

            // Sign the data if requested
            if (sign) {
                X509Certificate signingCert = certificateManager.getX509Certificate(settings.getSignCertAlias());
                PrivateKey senderKey = certificateManager.getPrivateKey(settings.getSignCertAlias(), settings.getSignCertKeyPassword().toCharArray());
                String digest = settings.getSignDigestAlgorithm();

                dataBP = AS2UtilOld.getCryptoHelper().sign(dataBP, signingCert, senderKey, digest);
                
                //Asynch MDN 2007-03-12
                DataHistoryItem historyItem = new DataHistoryItem(dataBP.getContentType());
                // *** add one more item to msg history
                msg.getHistory().getItems().add(historyItem);

                sLogger.debug("signed data"+msg.getLoggingText());
            }

            // Encrypt the data if requested
            if (encrypt) {
                String algorithm = partnership.getAttribute(SecurePartnership.PA_ENCRYPT);

                X509Certificate receiverCert = certFx.getCertificate(msg, Partnership.PTYPE_RECEIVER);
                dataBP = AS2UtilOld.getCryptoHelper().encrypt(dataBP, receiverCert, algorithm);

                //Asynch MDN 2007-03-12
                DataHistoryItem historyItem = new DataHistoryItem(dataBP.getContentType());
                // *** add one more item to msg history
                msg.getHistory().getItems().add(historyItem);

                sLogger.debug("encrypted data"+msg.getLoggingText());
            }
        }

        return dataBP;
    }
	
}
