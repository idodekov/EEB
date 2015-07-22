package org.redoubt.protocol.as2;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.apache.log4j.Logger;
import org.bouncycastle.cms.jcajce.ZlibCompressor;
import org.bouncycastle.mail.smime.SMIMECompressedGenerator;
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
            body.writeTo(new FileOutputStream("E:\\git\\Redoubt\\test.file"));
        }  catch (Exception e) {
            sLogger.error("An error has occured while packaging As2 message. " + e.getMessage(), e);
            throw new ProtocolException(e.getMessage(), e);
        }
        
    }
	
}
