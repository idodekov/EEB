package org.redoubt.protocol.as2;

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
    public static final String FROM = "From";
    public static final String SUBJECT = "Subject";
    
    public static final String MIME_TYPE_APPLICATION_OCTET_STREAM = "application/octet-stream";
    public static final String TRANSFER_ENCODING_BINARY = "binary";
    
    public static final String AS2_VERSION_1_0 = "1.0";
    public static final String AS2_VERSION_1_1 = "1.1";
    public static final String AS2_VERSION_1_2 = "1.2";
    
    public static final String MIME_VERSION_1_0 = "1.0";
}
