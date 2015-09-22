package org.redoubt.protocol.as2;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.activation.DataHandler;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.log4j.Logger;
import org.redoubt.api.configuration.ICertificateManager;
import org.redoubt.api.configuration.ICryptoHelper;
import org.redoubt.api.configuration.IPartyManager;
import org.redoubt.api.factory.Factory;
import org.redoubt.api.protocol.IMdnMonitor;
import org.redoubt.api.protocol.IMessage;
import org.redoubt.api.protocol.IProtocolSettings;
import org.redoubt.application.VersionInformation;
import org.redoubt.application.configuration.ConfigurationConstants;
import org.redoubt.application.configuration.Party;
import org.redoubt.protocol.ProtocolException;
import org.redoubt.util.FileSystemUtils;
import org.redoubt.util.Utils;

public class As2Message implements IMessage {
	private static final Logger sLogger = Logger.getLogger(As2Message.class);
	
	private MimeBodyPart data;
	private String toAddress;
	private String fromAddress;
	private boolean encrypt;
	private boolean sign;
	private boolean compress;
	private String signCertAlias;
	private String signCertKeyPassword;
	private String signDigestAlgorithm;
	private String encryptAlgorithm;
	private String encryptCertKeyPassword;
	private String encryptCertAlias;
	private String compressionAlgorithm;
	private boolean mdn;
	private String mdnType;
	private String asynchronousMdnUrl;
	private boolean requestSignedMdn;
	private String mdnSigningAlgorithm;
	
	private Map<String, String> headers;
	
	private String messageId;
	private String messageDate;
	private String subject;
	private String fromEmail;
	private String mic;
	
	private Party localParty;
	private Party remoteParty;
	
	public As2Message() {
		data = new MimeBodyPart();
		headers = new HashMap<String, String>();
	}
	
	public As2Message(Path payload, InternetHeaders internetHeaders) throws MessagingException, IOException {
		data = new MimeBodyPart();
		headers = new HashMap<String, String>();
		
		// TODO: add support for large files
		data.setDataHandler(new DataHandler(new ByteArrayDataSource(Files.readAllBytes(payload), 
				As2HeaderDictionary.MIME_TYPE_APPLICATION_OCTET_STREAM)));
		
		populateHeaders(internetHeaders);
	}
	
	public As2Message(byte[] content, InternetHeaders internetHeaders) throws MessagingException {
		data = new MimeBodyPart(internetHeaders, content);
		headers = new HashMap<String, String>();
		
		populateHeaders(internetHeaders);
	}
	
	private void populateHeaders(InternetHeaders internetHeaders) {
		if(internetHeaders != null) {
			@SuppressWarnings("unchecked")
			Enumeration<Header> allHeaders = internetHeaders.getAllHeaders();
			while(allHeaders.hasMoreElements()) {
				Header header = allHeaders.nextElement();
				headers.put(header.getName(), header.getValue());
			}
		}
	}
	
	public void packageMessage(IProtocolSettings settings) throws Exception {
		As2ProtocolSettings as2Settings = (As2ProtocolSettings) settings;
		sLogger.debug("Packaging As2 message...");
		
		fromAddress= as2Settings.getFrom();
		toAddress = as2Settings.getTo();
		
		resolveParties(toAddress, fromAddress);
		
		encrypt = localParty.isEncryptionEnabled();
        sign = localParty.isSigningEnabled();
        compress = localParty.isCompressionEnabled();
        
        if(as2Settings.isEncryptionEnforced() && !encrypt) {
        	throw new ProtocolException("Encryption for transport is enforced, however party with id [" + 
        			localParty.getPartyId() + "] doesn't have encryption enabled. Message will be rejected.");
        }
        
        if(as2Settings.isSigningEnforced() && !sign) {
        	throw new ProtocolException("Signing for transport is enforced, however party with id [" + 
        			localParty.getPartyId() + "] doesn't have signing enabled. Message will be rejected.");
        }
        
        signCertAlias = localParty.getSignCertAlias();
        signCertKeyPassword = localParty.getSignCertKeyPassword();
        signDigestAlgorithm = localParty.getSignDigestAlgorithm();
        encryptAlgorithm = localParty.getEncryptAlgorithm();
        encryptCertAlias = remoteParty.getEncryptCertAlias();
        compressionAlgorithm = localParty.getCompressionAlgorithm();
        mdn = localParty.isRequestMdn();
        mdnType = localParty.getMdnType();
    	asynchronousMdnUrl = localParty.getAsynchronousMdnUrl();
    	requestSignedMdn = localParty.isRequestSignedMdn();
    	mdnSigningAlgorithm = localParty.getMdnSigningAlgorithm();
        
		messageId = Utils.generateMessageID(fromAddress);
        
		data.setHeader(As2HeaderDictionary.CONTENT_TYPE, As2HeaderDictionary.MIME_TYPE_APPLICATION_OCTET_STREAM);
		data.setHeader(As2HeaderDictionary.CONTENT_TRANSFER_ENCODING, As2HeaderDictionary.TRANSFER_ENCODING_BINARY);

		if(mdn) {
			calculateMIC(mdnSigningAlgorithm);
		}
		
		secure();
		
		messageDate = Utils.createTimestamp();
		fromEmail = Utils.generateMessageSender(fromAddress);
		subject = "This is an AS2 message generated by " + VersionInformation.APP_NAME + ".";
        
		String contentType = Utils.normalizeContentType(data.getContentType());
        headers.put(As2HeaderDictionary.CONTENT_TYPE, contentType);
        headers.put(As2HeaderDictionary.AS2_FROM, fromAddress);
        headers.put(As2HeaderDictionary.AS2_TO, toAddress);
        headers.put(As2HeaderDictionary.AS2_VERSION, As2HeaderDictionary.AS2_VERSION_1_1);
        headers.put(As2HeaderDictionary.CONNECTION, "close, TE");
        headers.put(As2HeaderDictionary.USER_AGENT, VersionInformation.APP_NAME + " " + VersionInformation.APP_VERSION);
        headers.put(As2HeaderDictionary.ACCEPT_ENCODING, "gzip,deflate");
        headers.put(As2HeaderDictionary.MIME_VERSION, As2HeaderDictionary.MIME_VERSION_1_0);
        headers.put(As2HeaderDictionary.DATE, messageDate);
        headers.put(As2HeaderDictionary.MESSAGE_ID, "<" + messageId + ">");
        headers.put(As2HeaderDictionary.FROM, fromEmail);
        headers.put(As2HeaderDictionary.SUBJECT, subject);
        
        if(mdn) {
        	sLogger.debug("MDN is requested - adding appropriate headers.");
        	headers.put(As2HeaderDictionary.DISPOSITION_NOTIFICATION_TO, fromEmail);
        	
        	if(ConfigurationConstants.MDN_TYPE_ASYNCHRONOUS.equals(mdnType)) {
        		headers.put(As2HeaderDictionary.RECEIPT_DELIVERY_OPTIONS, asynchronousMdnUrl);
        	}
        	
        	if(requestSignedMdn) {
        		headers.put(As2HeaderDictionary.DISPOSITION_NOTIFICATION_OPTIONS, 
        				"signed-receipt-protocol=optional, pkcs7-signature; signed-receipt-micalg=optional, " + mdnSigningAlgorithm);
        	}
        	
        	IMdnMonitor mdnMonitor = Factory.getInstance().getMdnMonitor();
        	mdnMonitor.registerMessage(mic, this);
        }
        
        sLogger.debug("As2 message successfully packaged.");
	}
	
	protected void secure() throws Exception {
        // Encrypt and/or sign the data if requested
        if (encrypt || sign || compress) {
        	ICertificateManager certificateManager = Factory.getInstance().getCertificateManager();
        	ICryptoHelper cryptoHelper = Factory.getInstance().getCryptoHelper();

        	// Compress the data if requested
            if(compress) {
            	sLogger.debug("Compression is enabled - will attempt to compress the message.");
            	data = cryptoHelper.compress(data, compressionAlgorithm);
            }
        	
            // Sign the data if requested
            if (sign) {
                sLogger.debug("Signing is enabled - will attempt to sign the message.");
                X509Certificate signingCert = certificateManager.getX509Certificate(signCertAlias);
                PrivateKey senderKey = certificateManager.getPrivateKey(signCertAlias, signCertKeyPassword.toCharArray());

                data = cryptoHelper.sign(data, signingCert, senderKey, signDigestAlgorithm);
            }
            
            // Encrypt the data if requested
            if (encrypt) {
                sLogger.debug("Encryption is enabled - will attempt to encrypt the message.");
                X509Certificate receiverCert = certificateManager.getX509Certificate(encryptCertAlias);
                data = cryptoHelper.encrypt(data, receiverCert, encryptAlgorithm);
            }
        }
    }
	
	public void unpackageMessage(IProtocolSettings settings) throws Exception {
		As2ProtocolSettings as2Settings = (As2ProtocolSettings) settings;
		sLogger.debug("Unpackaging As2 message...");
		
		fromAddress= headers.get(As2HeaderDictionary.AS2_FROM);
		if(Utils.isNullOrEmptyTrimmed(fromAddress)) {
			throw new ProtocolException(As2HeaderDictionary.AS2_FROM + " header is empty. Unknown sender - rejecting the message.");
		}
		toAddress = headers.get(As2HeaderDictionary.AS2_TO);
		if(Utils.isNullOrEmptyTrimmed(toAddress)) {
			throw new ProtocolException(As2HeaderDictionary.AS2_TO + " header is empty. Unknown sender - rejecting the message.");
		}
		
		if(!as2Settings.getTo().equals(toAddress)) {
			throw new ProtocolException("Expected to receive a message for party with Id [" + as2Settings.getTo() + 
					"], but received [" + toAddress+ "]. Message will be rejected.");
		}
		
		if(fromAddress.equalsIgnoreCase(toAddress)) {
			throw new ProtocolException(As2HeaderDictionary.AS2_TO + " header can't be equal to [" + As2HeaderDictionary.AS2_FROM + "].");
		}
		
		messageId = Utils.parseMessageID(headers.get(As2HeaderDictionary.MESSAGE_ID));
		if(Utils.isNullOrEmptyTrimmed(messageId)) {
			throw new ProtocolException(As2HeaderDictionary.MESSAGE_ID + " header can't be empty. Unknown message id - rejecting the message.");
		}
		
		fromEmail = headers.get(As2HeaderDictionary.FROM);
		messageDate = headers.get(As2HeaderDictionary.DATE);
		subject = headers.get(As2HeaderDictionary.SUBJECT);
		
		resolveParties(fromAddress, toAddress);
		
        signCertAlias = remoteParty.getSignCertAlias();
        encryptCertAlias = localParty.getEncryptCertAlias();
        encryptCertKeyPassword = localParty.getEncryptCertKeyPassword();
        
        decryptAndVerify(as2Settings.isEncryptionEnforced(), as2Settings.isSigningEnforced());
        
        calculateMIC("sha1");
        
		sLogger.debug("As2 message successfully unpackaged.");
		
	}
	
	protected void decryptAndVerify(boolean encryptionEnforced, boolean signingEnforced) throws Exception {
    	ICertificateManager certificateManager = Factory.getInstance().getCertificateManager();
    	ICryptoHelper cryptoHelper = Factory.getInstance().getCryptoHelper();
    	
	    if (cryptoHelper.isEncrypted(data)) {
	       	sLogger.debug("Message is encrypted - will attempt to decrypt it.");
	
            X509Certificate receiverCert = certificateManager.getX509Certificate(encryptCertAlias);
            PrivateKey receiverKey = certificateManager.getPrivateKey(encryptCertAlias, encryptCertKeyPassword.toCharArray());
            data = cryptoHelper.decrypt(data, receiverCert, receiverKey);
            sLogger.debug("Message is decrypted.");
        } else {
        	if(encryptionEnforced) {
        		//Encryption is enforced
        		throw new ProtocolException("Encryption is enabled, however the message doesn't appear to be encrypted. Will reject it.");
        	}
        }
	
    		
   		if (cryptoHelper.isSigned(data)) {
   			sLogger.debug("Message is signed - will attempt to verify the signature.");
	
   			X509Certificate senderCert = certificateManager.getX509Certificate(signCertAlias);
   			data = cryptoHelper.verify(data, senderCert);
   			sLogger.debug("Signature verified.");
        } else {
        	if(signingEnforced) {
        		//Signing is enforced
        		throw new ProtocolException("Signing is enabled, however the message doesn't appear to be signed. Will reject it.");
        	}
        }
   		
   		if (cryptoHelper.isCompressed(data)) {
   			sLogger.debug("Message is compressed - will attempt to decompress it.");
   			data = cryptoHelper.decompress(data);
   			sLogger.debug("Message is decompressed.");
   		}
    }
	
	protected void resolveParties(String remotePartyId, String localPartyId) throws ProtocolException {
		if(remotePartyId == null || localPartyId == null) {
			throw new ProtocolException("Local or Remote party is unknown.");
		}
		
		IPartyManager partyManager = Factory.getInstance().getPartyManager();
		localParty = partyManager.getPartyById(localPartyId);
		if(localParty == null) {
			throw new ProtocolException("There's no local party for party id [" + localPartyId + "].");
		}
		
		remoteParty = partyManager.getPartyById(remotePartyId);
		if(remoteParty == null) {
			throw new ProtocolException("There's no remote party for party id [" + remotePartyId + "].");
		}
	}
	
	protected String calculateMIC(String digestAlg) throws Exception {
		ICryptoHelper cryptoHelper = Factory.getInstance().getCryptoHelper();
		
		Path workFile = FileSystemUtils.createWorkFile();
		writeMimeDataToFile(workFile);
		mic = cryptoHelper.calculateMIC(workFile, digestAlg);
		
		sLogger.debug("MIC for message with Id [" + messageId + "] is [" + mic + "].");
		FileSystemUtils.removeWorkFile(workFile);
		
		return mic;
	}
	
	public void writeMimeDataToFile(Path file) throws IOException, MessagingException {
		try (InputStream in = data.getInputStream()) {
			Files.copy(in, file, StandardCopyOption.REPLACE_EXISTING);
		}
    }
	
	public String getMessageDate() {
		return messageDate;
	}

	public void setMessageDate(String date) {
		this.messageDate = date;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getFromEmail() {
		return fromEmail;
	}

	public void setFromEmail(String fromEmail) {
		this.fromEmail = fromEmail;
	}

	public String getMic() {
		return mic;
	}

	public void setMic(String mic) {
		this.mic = mic;
	}

	public String getEncryptCertKeyPassword() {
		return encryptCertKeyPassword;
	}

	public void setEncryptCertKeyPassword(String encryptCertKeyPassword) {
		this.encryptCertKeyPassword = encryptCertKeyPassword;
	}

	public String getMessageId() {
		return messageId;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public String getToAddress() {
		return toAddress;
	}
	
	public void setToAddres(String to) {
		toAddress = to;
	}
	
	public String getFromAddress() {
		return fromAddress;
	}
	
	public void setFromAddres(String from) {
		fromAddress = from;
	}

	public MimeBodyPart getData() {
		return data;
	}

	public void setData(MimeBodyPart data) {
		this.data = data;
	}

	public boolean isEncrypt() {
		return encrypt;
	}

	public void setEncrypt(boolean encrypt) {
		this.encrypt = encrypt;
	}

	public boolean isSign() {
		return sign;
	}

	public void setSign(boolean sign) {
		this.sign = sign;
	}

	public boolean isCompress() {
		return compress;
	}

	public void setCompress(boolean compress) {
		this.compress = compress;
	}

	public String getSignCertAlias() {
		return signCertAlias;
	}

	public void setSignCertAlias(String signCertAlias) {
		this.signCertAlias = signCertAlias;
	}

	public String getSignCertKeyPassword() {
		return signCertKeyPassword;
	}

	public void setSignCertKeyPassword(String signCertKeyPassword) {
		this.signCertKeyPassword = signCertKeyPassword;
	}

	public String getSignDigestAlgorithm() {
		return signDigestAlgorithm;
	}

	public void setSignDigestAlgorithm(String signDigestAlgorithm) {
		this.signDigestAlgorithm = signDigestAlgorithm;
	}

	public String getEncryptAlgorithm() {
		return encryptAlgorithm;
	}

	public void setEncryptAlgorithm(String encryptAlgorithm) {
		this.encryptAlgorithm = encryptAlgorithm;
	}

	public String getEncryptCertAlias() {
		return encryptCertAlias;
	}

	public void setEncryptCertAlias(String encryptCertAlias) {
		this.encryptCertAlias = encryptCertAlias;
	}
}
