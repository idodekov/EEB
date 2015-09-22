package org.redoubt.protocol.as2.mdn;

import java.io.IOException;
import java.util.Enumeration;

import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

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
	
	protected MimeBodyPart createDispositionPart() throws IOException, MessagingException {
        MimeBodyPart dispositionPart = new MimeBodyPart();

        InternetHeaders dispValues = new InternetHeaders();
        dispValues.setHeader("Reporting-UA", getReportingUA());
        dispValues.setHeader("Original-Recipient", getOriginalRecipient());
        dispValues.setHeader("Final-Recipient", getFinalRecipient());
        dispValues.setHeader("Original-Message-ID", getOriginalMessageID());
        dispValues.setHeader("Disposition", getDisposition());
        dispValues.setHeader("Received-Content-MIC", getReceivedContentMIC());

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
        String text = getText() + "\r\n";
        textPart.setContent(text, TEXT_TYPE);
        textPart.setHeader("Content-Type", TEXT_TYPE);        

        return textPart;
    }
}
