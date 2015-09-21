package org.redoubt.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.redoubt.application.VersionInformation;

public class Utils {
	private static final Logger sLogger = Logger.getLogger(Utils.class);
	
	private Utils() {}
	
	public static String generateMessageSender(String sender) {
		String hostname;
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			sLogger.warn("Unable to determine hostname of local machine. " + e.getMessage(), e);
			hostname = "localhost";
		}
		
		StringBuilder senderBuilder = new StringBuilder();
		senderBuilder.append(sender);
		senderBuilder.append("@");
		senderBuilder.append(hostname);
		
		return senderBuilder.toString();
	}
	
	public static String generateMessageID(String sender) {
		SimpleDateFormat format = new SimpleDateFormat("ddMMyyyyHHmmssSSS", Locale.ENGLISH);
		String timestampParameter = format.format(new Date());
		
		/* Between 100000 and 999999 */
		int random = RandomUtils.nextInt(100000, 999999);
		
		String hostname;
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			sLogger.warn("Unable to determine hostname of local machine. " + e.getMessage(), e);
			hostname = "localhost";
		}

		StringBuilder messageId = new StringBuilder();
		messageId.append("<");
		messageId.append(VersionInformation.APP_NAME);
		messageId.append(".");
		messageId.append(timestampParameter);
		messageId.append(".");
		messageId.append(random);
		messageId.append("@");
		messageId.append(sender);
		messageId.append(".");
		messageId.append(hostname);
		messageId.append(">");
		
		String id = messageId.toString();
		
		sLogger.debug("A new message id was succesfully generated [" + id + "].");
		
        return id;
    }
	
	public static String createTimestamp() {
		SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
		format.setTimeZone(TimeZone.getTimeZone("GMT"));
		return format.format(new Date());
	}
	
	public static String normalizeContentType(String contentType) {
		contentType = contentType.replace("micalg=sha-", "micalg=sha");
		contentType = contentType.replaceAll("(\r\n)|(\t)", "");
		return contentType;
	}
}
