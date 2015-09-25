package org.redoubt.protocol.as2;

import org.redoubt.application.VersionInformation;

public class As2HeaderDictionary {
    private As2HeaderDictionary () {}
    
    public static final String AS2_FROM = "AS2-From";
    public static final String AS2_TO = "AS2-To";
    public static final String AS2_VERSION = "AS2-Version";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";
    public static final String CONNECTION = "Connection";
    public static final String USER_AGENT = "User-Agent";
    public static final String ACCEPT_ENCODING = "Accept-Encoding";
    public static final String MIME_VERSION = "MIME-Version";
    public static final String DATE = "Date";
    public static final String MESSAGE_ID = "Message-Id";
    public static final String ORIGINAL_MESSAGE_ID = "Original-Message-ID";
    public static final String FROM = "From";
    public static final String SUBJECT = "Subject";
    public static final String DISPOSITION_NOTIFICATION_TO = "Disposition-Notification-To";
    public static final String DISPOSITION_NOTIFICATION_OPTIONS = "Disposition-Notification-Options";
    public static final String RECEIPT_DELIVERY_OPTIONS = "Receipt-Delivery-Option";
    public static final String ORIGINAL_RECIPIENT = "Original-Recipient";
    public static final String FINAL_RECIPIENT = "Final-Recipient";
    public static final String DISPOSITION = "Disposition";
    public static final String RECEIVED_CONTENT_MIC = "Received-Content-MIC";
    
    public static final String MIME_TYPE_APPLICATION_OCTET_STREAM = "application/octet-stream";
    public static final String MIME_TYPE_DISPOSITION_NOTIFICATION = "message/disposition-notification";
    public static final String MIME_TYPE_TEXT_PLAIN = "text/plain";
    public static final String MIME_TYPE_TEXT_PLAIN_US_ASCII = "text/plain; charset=us-ascii";
    public static final String MIME_SUBTYPE_REPORT = "report; report-type=disposition-notification";
    public static final String TRANSFER_ENCODING_BINARY = "binary";
    public static final String TRANSFER_ENCODING_7BIT = "7bit";
    public static final String USER_AGENT_REDOUBT = VersionInformation.APP_NAME + " " + VersionInformation.APP_VERSION;
    
    public static final String AS2_VERSION_1_0 = "1.0";
    public static final String AS2_VERSION_1_1 = "1.1";
    public static final String AS2_VERSION_1_2 = "1.2";
    
    public static final String MIME_VERSION_1_0 = "1.0";
}
