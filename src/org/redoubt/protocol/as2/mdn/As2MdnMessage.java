package org.redoubt.protocol.as2.mdn;

import java.io.IOException;
import java.util.Enumeration;

import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import org.redoubt.api.protocol.IProtocolSettings;
import org.redoubt.protocol.as2.As2HeaderDictionary;
import org.redoubt.protocol.as2.As2Message;

public class As2MdnMessage extends As2Message {
    private String originalMessageId;
    private String text;
    
    public As2MdnMessage(As2Message message) {
    	super();
    	setMic(message.getMic());
		setFromAddres(message.getToAddress());
		setToAddres(message.getFromAddress());
		setAsynchronousMdnUrl(message.getAsynchronousMdnUrl());
		setMdnType(message.getMdnType());
		setMdnSigningAlgorithm(message.getMdnSigningAlgorithm());
		setRequestSignedMdn(message.isRequestSignedMdn());
		setLocalParty(message.getLocalParty());
		setRemoteParty(message.getRemoteParty());
		setMessageDate(message.getMessageDate());
		setSubject(message.getSubject());
		setDisposition(message.getDisposition());
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

        getData().setContent(multipart);
        getData().setHeader(As2HeaderDictionary.CONTENT_TYPE, multipart.getContentType());
            
        setSignCertAlias(getLocalParty().getSignCertAlias());
    	setSignCertKeyPassword(getLocalParty().getSignCertKeyPassword());
    		
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
        dispValues.setHeader(As2HeaderDictionary.ORIGINAL_RECIPIENT, "rfc822; " + getFromAddress());
        dispValues.setHeader(As2HeaderDictionary.FINAL_RECIPIENT, "rfc822; " + getFromAddress());
        dispValues.setHeader(As2HeaderDictionary.ORIGINAL_MESSAGE_ID, getOriginalMessageId());
        dispValues.setHeader(As2HeaderDictionary.DISPOSITION, getDisposition().getStatus());
        dispValues.setHeader(As2HeaderDictionary.RECEIVED_CONTENT_MIC, getMic());

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
        
        text = "The message sent to Recipient [" + getFromAddress() + "] on [" + getMessageDate() + "]\r\n" + 
        "with Subject [" + getSubject() + "] and Id [" + getOriginalMessageId() + "] has been received.\r\n" +
        "In addition, the sender of the message, [" + getToAddress() + "] was authenticated\r\n" + 
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
