package org.redoubt.protocol.as2.mdn;

import java.io.IOException;
import java.util.Enumeration;

import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import org.redoubt.api.protocol.IProtocolSettings;
import org.redoubt.protocol.as2.As2Message;

public class As2MdnMessage extends As2Message {
	// report multipart content type
    public static final String REPORT_SUBTYPE = "report; report-type=disposition-notification";
    public static final String REPORT_TYPE = "multipart/" + REPORT_SUBTYPE;
    // text part content header
    public static final String TEXT_TYPE = "text/plain";
    public static final String TEXT_CHARSET = "us-ascii";
    public static final String TEXT_ENCODING = "7bit";
    // disposition content header
    public static final String DISPOSITION_TYPE = "message/disposition-notification";
    public static final String DISPOSITION_CHARSET = "us-ascii";
    public static final String DISPOSITION_ENCODING = "7bit";
    
    private String originalMessageId;
    
    public As2MdnMessage(As2Message message) {
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
		try {
            MimeMultipart multipart = createReportPart();

            MimeBodyPart dispositionPart = createDispositionPart();
            multipart.addBodyPart(dispositionPart);

            MimeBodyPart textPart = createTextPart();
            multipart.addBodyPart(textPart);

            getData().setContent(multipart);
            getData().setHeader("Content-Type", multipart.getContentType());
            
            setSignCertAlias(getLocalParty().getSignCertAlias());
    		setSignCertKeyPassword(getLocalParty().getSignCertKeyPassword());
    		
    		if(isRequestSignedMdn()) {
    			setSign(true);
    			setEncrypt(false);
    			setCompress(false);
    			setSignDigestAlgorithm(getMdnSigningAlgorithm());
    			secure();
    		}
        } catch (IOException ioe) {
            throw new MessagingException("Error creating data: " + ioe.getMessage());
        }
	}

	@Override
	public void unpackageMessage(IProtocolSettings settings) throws Exception {
		// TODO Auto-generated method stub
		super.unpackageMessage(settings);
	}

	protected MimeBodyPart createDispositionPart() throws IOException, MessagingException {
        MimeBodyPart dispositionPart = new MimeBodyPart();

        InternetHeaders dispValues = new InternetHeaders();
        dispValues.setHeader("Original-Recipient", "rfc822; " + getFromAddress());
        dispValues.setHeader("Final-Recipient", "rfc822; " + getFromAddress());
        dispValues.setHeader("Original-Message-ID", getOriginalMessageId());
        dispValues.setHeader("Disposition", getDisposition().getStatus());
        dispValues.setHeader("Received-Content-MIC", getMic());

        Enumeration dispEnum = dispValues.getAllHeaderLines();
        StringBuffer dispData = new StringBuffer();

        while (dispEnum.hasMoreElements()) {
            dispData.append((String) dispEnum.nextElement()).append("\r\n");
        }

        dispData.append("\r\n");

        String dispText = dispData.toString();
        dispositionPart.setContent(dispText, DISPOSITION_TYPE);
        dispositionPart.setHeader("Content-Type", DISPOSITION_TYPE);

        return dispositionPart;
    }

    protected MimeMultipart createReportPart() throws MessagingException {
        MimeMultipart reportParts = new MimeMultipart();
        reportParts.setSubType(REPORT_SUBTYPE);

        return reportParts;
    }
	
	protected MimeBodyPart createTextPart() throws IOException, MessagingException {
        MimeBodyPart textPart = new MimeBodyPart();        
        
        String text = "The message sent to Recipient [" + getFromAddress() + "] on [" + getMessageDate() + "]\r\n" + 
        "with Subject [" + getSubject() + "] and Id [" + getOriginalMessageId() + "] has been received.\r\n" +
        "In addition, the sender of the message, [" + getToAddress() + "] was authenticated\r\n" + 
        "as the originator of the message.\r\n" +
        "This is not a guarantee that the message has been completely processed or\r\n" +
        "understood by the receiving party.\r\n";
        
        textPart.setContent(text, TEXT_TYPE);
        textPart.setHeader("Content-Type", TEXT_TYPE);        

        return textPart;
    }

	public String getOriginalMessageId() {
		return originalMessageId;
	}

	public void setOriginalMessageId(String originalMessageId) {
		this.originalMessageId = originalMessageId;
	}
}
