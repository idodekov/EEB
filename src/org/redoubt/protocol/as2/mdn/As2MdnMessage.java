package org.redoubt.protocol.as2.mdn;

import java.io.IOException;
import java.util.Enumeration;

import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import org.redoubt.api.protocol.IProtocolSettings;
import org.redoubt.application.VersionInformation;
import org.redoubt.protocol.as2.As2HeaderDictionary;
import org.redoubt.protocol.as2.As2Message;
import org.redoubt.util.Utils;

public class As2MdnMessage extends As2Message {
    private String originalMessageId;
    private String text;
    
    public As2MdnMessage(As2Message message) {
    	super();
    	mic = message.getMic();
		fromAddress = message.getToAddress();
		toAddress = message.getFromAddress();
		asynchronousMdnUrl = message.getAsynchronousMdnUrl();
		mdnType = message.getMdnType();
		mdnSigningAlgorithm = message.getMdnSigningAlgorithm();
		requestSignedMdn = message.isRequestSignedMdn();
		localParty = message.getLocalParty();
		remoteParty = message.getRemoteParty();
		messageDate = message.getMessageDate();
		subject = message.getSubject();
		disposition = message.getDisposition();
		originalMessageId = message.getMessageId();
	}
	
	@Override
	public void packageMessage(IProtocolSettings settings) throws Exception {
        MimeMultipart multipart = new MimeMultipart();
        multipart.setSubType(As2HeaderDictionary.MIME_SUBTYPE_REPORT);
        
        MimeBodyPart textPart = createTextPart();
        multipart.addBodyPart(textPart);

        MimeBodyPart dispositionPart = createDispositionPart();
        multipart.addBodyPart(dispositionPart);

        data.setContent(multipart);
        data.setHeader(As2HeaderDictionary.CONTENT_TYPE, multipart.getContentType());
            
        signCertAlias = getLocalParty().getSignCertAlias();
    	signCertKeyPassword = getLocalParty().getSignCertKeyPassword();
    	
    	String contentType = Utils.normalizeContentType(data.getContentType());
        headers.put(As2HeaderDictionary.CONTENT_TYPE, contentType);
        headers.put(As2HeaderDictionary.AS2_FROM, fromAddress);
        headers.put(As2HeaderDictionary.AS2_TO, toAddress);
        headers.put(As2HeaderDictionary.AS2_VERSION, As2HeaderDictionary.AS2_VERSION_1_1);
        headers.put(As2HeaderDictionary.CONNECTION, "close");
        headers.put(As2HeaderDictionary.USER_AGENT, VersionInformation.APP_NAME + " " + VersionInformation.APP_VERSION);
        headers.put(As2HeaderDictionary.ACCEPT_ENCODING, "gzip,deflate");
        headers.put(As2HeaderDictionary.MIME_VERSION, As2HeaderDictionary.MIME_VERSION_1_0);
        headers.put(As2HeaderDictionary.DATE, messageDate);
        headers.put(As2HeaderDictionary.MESSAGE_ID, "<" + messageId + ">");
        headers.put(As2HeaderDictionary.FROM, fromEmail);
        headers.put(As2HeaderDictionary.SUBJECT, subject);
    		
    	if(isRequestSignedMdn()) {
    		setSign(true);
    		setEncrypt(false);
    		setCompress(false);
    		setSignDigestAlgorithm(getMdnSigningAlgorithm());
    		secure();
    	}
	}

	@Override
	public void unpackageMessage(IProtocolSettings settings) throws Exception {
		// TODO Auto-generated method stub
		//super.unpackageMessage(settings);
	}

	protected MimeBodyPart createDispositionPart() throws IOException, MessagingException {
        MimeBodyPart dispositionPart = new MimeBodyPart();

        InternetHeaders dispValues = new InternetHeaders();
        dispValues.setHeader(As2HeaderDictionary.ORIGINAL_RECIPIENT, "rfc822; " + fromAddress);
        dispValues.setHeader(As2HeaderDictionary.FINAL_RECIPIENT, "rfc822; " + fromAddress);
        dispValues.setHeader(As2HeaderDictionary.ORIGINAL_MESSAGE_ID, originalMessageId);
        dispValues.setHeader(As2HeaderDictionary.DISPOSITION, disposition.getStatus());
        dispValues.setHeader(As2HeaderDictionary.RECEIVED_CONTENT_MIC, mic);

        @SuppressWarnings("rawtypes")
        Enumeration dispEnum = dispValues.getAllHeaderLines();
        StringBuffer dispData = new StringBuffer();

        while (dispEnum.hasMoreElements()) {
            dispData.append((String) dispEnum.nextElement()).append("\r\n");
        }

        dispData.append("\r\n");

        String dispText = dispData.toString();
        dispositionPart.setContent(dispText, As2HeaderDictionary.MIME_TYPE_DISPOSITION_NOTIFICATION);
        dispositionPart.setHeader(As2HeaderDictionary.CONTENT_TYPE, As2HeaderDictionary.MIME_TYPE_DISPOSITION_NOTIFICATION);

        return dispositionPart;
    }
	
	protected MimeBodyPart createTextPart() throws IOException, MessagingException {
        MimeBodyPart textPart = new MimeBodyPart();        
        
        text = "The message sent to Recipient [" + fromAddress + "] on [" + messageDate + "]\r\n" + 
        "with Subject [" + subject + "] and Id [" + originalMessageId + "] has been received.\r\n" +
        "In addition, the sender of the message, [" + toAddress + "] was authenticated\r\n" + 
        "as the originator of the message.\r\n" +
        "This is not a guarantee that the message has been completely processed or\r\n" +
        "understood by the receiving party.\r\n";
        
        textPart.setContent(text, As2HeaderDictionary.MIME_TYPE_TEXT_PLAIN_US_ASCII);
        textPart.setHeader(As2HeaderDictionary.CONTENT_TYPE, As2HeaderDictionary.MIME_TYPE_TEXT_PLAIN_US_ASCII);

        return textPart;
    }

	public String getOriginalMessageId() {
		return originalMessageId;
	}

	public void setOriginalMessageId(String originalMessageId) {
		this.originalMessageId = originalMessageId;
	}
}
