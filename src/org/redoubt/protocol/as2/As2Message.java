package org.redoubt.protocol.as2;

import java.io.File;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;

import org.apache.log4j.Logger;
import org.bouncycastle.cms.jcajce.ZlibCompressor;
import org.bouncycastle.mail.smime.SMIMECompressedGenerator;
import org.bouncycastle.mail.smime.SMIMEException;

public class As2Message {
	private static final Logger sLogger = Logger.getLogger(As2Message.class);
	
	private MimeBodyPart data;
	private Address toAddress;
	private Address fromAddress;
	
	public As2Message() {
		data = new MimeBodyPart();
	}
	
	public void generateMimeData(String payload) throws MessagingException, SMIMEException {
		MimeBodyPart msg = new MimeBodyPart();
		SMIMECompressedGenerator gen = new SMIMECompressedGenerator();
		msg.setDataHandler(new DataHandler(new FileDataSource(new File(payload))));
		
		data = gen.generate(msg, new ZlibCompressor());
	}
	
	public Address getToAddress() {
		return toAddress;
	}
	
	public void setToAddres(String to) {
		try {
			toAddress = new InternetAddress(to);
		} catch (AddressException e) {
			sLogger.error("Error while setting TO address. " + e.getMessage(), e);
		}
	}
	
	public Address getFromAddress() {
		return fromAddress;
	}
	
	public void setFromAddres(String from) {
		try {
			fromAddress = new InternetAddress(from);
		} catch (AddressException e) {
			sLogger.error("Error while setting FROM address. " + e.getMessage(), e);
		}
	}
}
