package org.redoubt.protocol.as2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import org.redoubt.protocol.as2.mdn.As2MdnMessage;
import org.redoubt.protocol.as2.mdn.Disposition;
import org.redoubt.util.FileSystemUtils;
import org.redoubt.util.Utils;

public class As2Message implements IMessage {
	private static final Logger sLogger = Logger.getLogger(As2Message.class);
	
	protected MimeBodyPart data;
	protected String toAddress;
	protected String fromAddress;
	protected boolean encrypt;
	protected boolean sign;
	protected boolean compress;
	protected String signCertAlias;
	protected String signCertKeyPassword;
	protected String signDigestAlgorithm;
	protected String encryptAlgorithm;
	protected String encryptCertKeyPassword;
	protected String encryptCertAlias;
	protected String compressionAlgorithm;
	protected boolean mdnRequested;
	protected String mdnType;
	protected String asynchronousMdnUrl;
	protected boolean requestSignedMdn;
	protected String mdnSigningAlgorithm;
	protected Disposition disposition;
	
	protected Map<String, String> headers;
	
	protected String messageId;
	protected String messageDate;
	protected String subject;
	protected String fromEmail;
	protected String mic;
	
	protected Party localParty;
	protected Party remoteParty;
	
	public As2Message() {
		data = new MimeBodyPart();
		headers = new HashMap<String, String>();
		disposition = new Disposition();
	}
	
	public As2Message(Path payload, InternetHeaders internetHeaders) throws MessagingException, IOException {
		data = new MimeBodyPart();
		headers = new HashMap<String, String>();
		disposition = new Disposition();
		
		// TODO: add support for large files
		data.setDataHandler(new DataHandler(new ByteArrayDataSource(Files.readAllBytes(payload), 
				As2HeaderDictionary.MIME_TYPE_APPLICATION_OCTET_STREAM)));
		
		populateHeaders(internetHeaders);
	}
	
	public As2Message(byte[] content, InternetHeaders internetHeaders) throws MessagingException {
		data = new MimeBodyPart(internetHeaders, content);
		headers = new HashMap<String, String>();
		disposition = new Disposition();
		
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
	
	@Override
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
        mdnRequested = localParty.isRequestMdn();
        mdnType = localParty.getMdnType();
    	asynchronousMdnUrl = localParty.getAsynchronousMdnUrl();
    	requestSignedMdn = localParty.isRequestSignedMdn();
    	mdnSigningAlgorithm = localParty.getMdnSigningAlgorithm();
        
		messageId = Utils.generateMessageID(fromAddress);
        
		data.setHeader(As2HeaderDictionary.CONTENT_TYPE, As2HeaderDictionary.MIME_TYPE_APPLICATION_OCTET_STREAM);
		data.setHeader(As2HeaderDictionary.CONTENT_TRANSFER_ENCODING, As2HeaderDictionary.TRANSFER_ENCODING_BINARY);

		secure();
		
		messageDate = Utils.createTimestamp();
		fromEmail = Utils.generateMessageSender(fromAddress);
		subject = "This is an AS2 message generated by " + VersionInformation.APP_NAME + ".";
        
		String contentType = Utils.normalizeContentType(data.getContentType());
        headers.put(As2HeaderDictionary.CONTENT_TYPE, contentType);
        headers.put(As2HeaderDictionary.AS2_FROM, fromAddress);
        headers.put(As2HeaderDictionary.AS2_TO, toAddress);
        headers.put(As2HeaderDictionary.AS2_VERSION, As2HeaderDictionary.AS2_VERSION_1_1);
        headers.put(As2HeaderDictionary.CONNECTION, "close");
        headers.put(As2HeaderDictionary.USER_AGENT, As2HeaderDictionary.USER_AGENT_REDOUBT);
        headers.put(As2HeaderDictionary.ACCEPT_ENCODING, "gzip,deflate");
        headers.put(As2HeaderDictionary.MIME_VERSION, As2HeaderDictionary.MIME_VERSION_1_0);
        headers.put(As2HeaderDictionary.DATE, messageDate);
        headers.put(As2HeaderDictionary.MESSAGE_ID, messageId);
        headers.put(As2HeaderDictionary.FROM, fromEmail);
        headers.put(As2HeaderDictionary.SUBJECT, subject);
        
        prepreOutboundMdnOptions();
        
        sLogger.debug("As2 message successfully packaged.");
	}
	
	protected void secure() throws Exception {
        ICertificateManager certificateManager = Factory.getInstance().getCertificateManager();
        ICryptoHelper cryptoHelper = Factory.getInstance().getCryptoHelper();
        
        if(!(this instanceof As2MdnMessage)) {
            if(mdnRequested) {
                if(!sign && (compress || encrypt)) {
                    calculateMIC(mdnSigningAlgorithm,true);
                } else if(!sign) {
                    calculateMIC(mdnSigningAlgorithm,false);
                }
            }
        }
        
        if(compress) {
           	sLogger.debug("Compression is enabled - will attempt to compress the message.");
          	data = cryptoHelper.compress(data, compressionAlgorithm);
        }
        
        if(!(this instanceof As2MdnMessage)) {
            if(mdnRequested) {
                if(sign) {
                    calculateMIC(mdnSigningAlgorithm,true);
                }
            }
        }
        
        if (sign) {
            sLogger.debug("Signing is enabled - will attempt to sign the message.");
            X509Certificate signingCert = certificateManager.getX509Certificate(signCertAlias);
            PrivateKey senderKey = certificateManager.getPrivateKey(signCertAlias, signCertKeyPassword.toCharArray());

            data = cryptoHelper.sign(data, signingCert, senderKey, signDigestAlgorithm);
        }
            
        if (encrypt) {
            sLogger.debug("Encryption is enabled - will attempt to encrypt the message.");
            X509Certificate receiverCert = certificateManager.getX509Certificate(encryptCertAlias);
            data = cryptoHelper.encrypt(data, receiverCert, encryptAlgorithm);
        }
    }
	
	protected void prepreOutboundMdnOptions() {
		if(mdnRequested) {
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
	}
	
	@Override
	public void unpackageMessage(IProtocolSettings settings) throws Exception {
		As2ProtocolSettings as2Settings = (As2ProtocolSettings) settings;
		sLogger.debug("Unpackaging As2 message...");
		
		prepreInboundMdnOptions();
		
		fromAddress= headers.get(As2HeaderDictionary.AS2_FROM);
		if(Utils.isNullOrEmptyTrimmed(fromAddress)) {
			disposition.setStatus(Disposition.DISP_AUTHENTICATION_FAILED);
			throw new ProtocolException(As2HeaderDictionary.AS2_FROM + " header is empty. Unknown sender - rejecting the message.");
		}
		toAddress = headers.get(As2HeaderDictionary.AS2_TO);
		if(Utils.isNullOrEmptyTrimmed(toAddress)) {
			disposition.setStatus(Disposition.DISP_AUTHENTICATION_FAILED);
			throw new ProtocolException(As2HeaderDictionary.AS2_TO + " header is empty. Unknown sender - rejecting the message.");
		}
		
		if(!as2Settings.getTo().equals(toAddress)) {
			disposition.setStatus(Disposition.DISP_AUTHENTICATION_FAILED);
			throw new ProtocolException("Expected to receive a message for party with Id [" + as2Settings.getTo() + 
					"], but received [" + toAddress+ "]. Message will be rejected.");
		}
		
		if(fromAddress.equalsIgnoreCase(toAddress)) {
			disposition.setStatus(Disposition.DISP_AUTHENTICATION_FAILED);
			throw new ProtocolException(As2HeaderDictionary.AS2_TO + " header can't be equal to [" + As2HeaderDictionary.AS2_FROM + "].");
		}
		
		messageId = headers.get(As2HeaderDictionary.MESSAGE_ID);
		if(Utils.isNullOrEmptyTrimmed(messageId)) {
			disposition.setStatus(Disposition.DISP_AUTHENTICATION_FAILED);
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
        
        disposition.setStatus(Disposition.DISP_PROCESSED);
		sLogger.debug("As2 message successfully unpackaged.");
		
	}
	
	protected void decryptAndVerify(boolean encryptionEnforced, boolean signingEnforced) throws Exception {
    	ICertificateManager certificateManager = Factory.getInstance().getCertificateManager();
    	ICryptoHelper cryptoHelper = Factory.getInstance().getCryptoHelper();
    	
    	boolean messageIsEncrypted = cryptoHelper.isEncrypted(data);
    	
	    if (messageIsEncrypted) {
	       	sLogger.debug("Message is encrypted - will attempt to decrypt it.");
	
            X509Certificate receiverCert = certificateManager.getX509Certificate(encryptCertAlias);
            PrivateKey receiverKey = certificateManager.getPrivateKey(encryptCertAlias, encryptCertKeyPassword.toCharArray());
            try {
            	data = cryptoHelper.decrypt(data, receiverCert, receiverKey);
		    } catch(Exception e) {
	        	disposition.setStatus(Disposition.DISP_DECRYPTION_FAILED);
	        	sLogger.error("An error has occured while decrypting message. " + e.getMessage());
	        	throw new ProtocolException(e.getMessage(), e);
	        }
            sLogger.debug("Message is decrypted.");
        } else {
        	if(encryptionEnforced) {
        		//Encryption is enforced
        		disposition.setStatus(Disposition.DISP_INSUFFICIENT_SECURITY);
        		throw new ProtocolException("Encryption is enabled, however the message doesn't appear to be encrypted. Will reject it.");
        	}
        }
	
	    boolean messageIsSigned = cryptoHelper.isSigned(data);
    		
   		if (messageIsSigned) {
   			sLogger.debug("Message is signed - will attempt to verify the signature.");
	
   			X509Certificate senderCert = certificateManager.getX509Certificate(signCertAlias);
   			try {
   				data = cryptoHelper.verify(data, senderCert);
   			} catch(Exception e) {
            	disposition.setStatus(Disposition.DISP_SIGNATURE_FAILED);
            	sLogger.error("An error has occured while verifying message signature. " + e.getMessage());
            	throw new ProtocolException(e.getMessage(), e);
            }
   			
   			sLogger.debug("Signature verified.");
        } else {
        	if(signingEnforced) {
        		//Signing is enforced
        		disposition.setStatus(Disposition.DISP_INSUFFICIENT_SECURITY);
        		throw new ProtocolException("Signing is enabled, however the message doesn't appear to be signed. Will reject it.");
        	}
        }
   		
   		boolean isCompressed = cryptoHelper.isCompressed(data);
   		
   		if(!(this instanceof As2MdnMessage)) {
            if(messageIsSigned) {
                calculateMIC(mdnSigningAlgorithm, true);
            }
        }
   		
   		if (isCompressed) {
   			sLogger.debug("Message is compressed - will attempt to decompress it.");
   			data = cryptoHelper.decompress(data);
   			sLogger.debug("Message is decompressed.");
   		}
   		
   		if(!(this instanceof As2MdnMessage)) {
            if(!messageIsSigned && (messageIsEncrypted || isCompressed)) {
                calculateMIC(mdnSigningAlgorithm, true);
            } else if(!messageIsSigned) {
                calculateMIC(mdnSigningAlgorithm, false);
            }
        }
    }
	
	protected void prepreInboundMdnOptions() {
		String dispositionNotificationTo = headers.get(As2HeaderDictionary.DISPOSITION_NOTIFICATION_TO);
        if(!Utils.isNullOrEmptyTrimmed(dispositionNotificationTo)) {
        	mdnRequested = true;
        	
        	String asyncUrl = headers.get(As2HeaderDictionary.RECEIPT_DELIVERY_OPTIONS);
        	if(Utils.isNullOrEmptyTrimmed(asyncUrl)) {
        		mdnType = ConfigurationConstants.MDN_TYPE_SYNCHRONOUS;
        	} else {
        		mdnType = ConfigurationConstants.MDN_TYPE_ASYNCHRONOUS;
        		asynchronousMdnUrl = asyncUrl;
        	}
        	
        	String dispositionNotificationOptions = headers.get(As2HeaderDictionary.DISPOSITION_NOTIFICATION_OPTIONS);
        	if(!Utils.isNullOrEmptyTrimmed(dispositionNotificationOptions)) {
        		requestSignedMdn = true;
        		String micAlg = ICryptoHelper.DIGEST_SHA1;
        		
        		if(dispositionNotificationOptions.startsWith("signed-receipt-protocol=optional, pkcs7-signature; signed-receipt-micalg=optional,")) {
        			int startIndex = dispositionNotificationOptions.lastIndexOf("signed-receipt-protocol=optional, pkcs7-signature; signed-receipt-micalg=optional,");
        			String micAlgList = dispositionNotificationOptions.substring(startIndex).trim();
        			if(micAlgList.contains(",")) {
        				/* Means that more than a single mic alg is set - just get the first one */
        				for(String str : micAlgList.split(",")) {
        					if(str.equals(ICryptoHelper.DIGEST_SHA1) || str.equals(ICryptoHelper.DIGEST_MD5)) {
        						micAlg = str;
        					}
        				}
        			} else {
        				if(micAlgList.equals(ICryptoHelper.DIGEST_SHA1) || micAlgList.equals(ICryptoHelper.DIGEST_MD5)) {
    						micAlg = micAlgList;
    					}
        			}
        			
        		}
        		
        		mdnSigningAlgorithm = micAlg;
        	}
        }
	}
	
	public void resolveParties(String remotePartyId, String localPartyId) throws ProtocolException {
		if(remotePartyId == null || localPartyId == null) {
			disposition.setStatus(Disposition.DISP_AUTHENTICATION_FAILED);
			throw new ProtocolException("Local or Remote party is unknown.");
		}
		
		IPartyManager partyManager = Factory.getInstance().getPartyManager();
		localParty = partyManager.getPartyById(localPartyId);
		if(localParty == null) {
			disposition.setStatus(Disposition.DISP_AUTHENTICATION_FAILED);
			throw new ProtocolException("There's no local party for party id [" + localPartyId + "].");
		}
		
		remoteParty = partyManager.getPartyById(remotePartyId);
		if(remoteParty == null) {
			disposition.setStatus(Disposition.DISP_AUTHENTICATION_FAILED);
			throw new ProtocolException("There's no remote party for party id [" + remotePartyId + "].");
		}
	}
	
	/**
	 *
<pre>
   When a signed receipt request is made, the "Received-content-MIC"
   MUST always be returned to the requester (except when corruption
   prevents computation of the digest in accordance with the following
   specification).  The "Received-content-MIC" MUST be calculated as
   follows:

      o  For any signed messages, the MIC to be returned is calculated
         on the RFC1767/RFC3023 MIME header and content.
         Canonicalization on the MIME headers MUST be performed before
         the MIC is calculated, since the sender requesting the signed
         receipt was also REQUIRED to canonicalize.

      o  For encrypted, unsigned messages, the MIC to be returned is
         calculated on the decrypted RFC 1767/RFC3023 MIME header and
         content.  The content after decryption MUST be canonicalized
         before the MIC is calculated.

      o  For unsigned, unencrypted messages, the MIC MUST be calculated
         over the message contents without the MIME or any other RFC
         2822 headers, since these are sometimes altered or reordered by
         Mail Transport Agents (MTAs).
</pre>
	 * @param digestAlg
	 * @return
	 * @throws Exception
	 */
	protected String calculateMIC(String digestAlg, boolean headers) throws Exception {
		ICryptoHelper cryptoHelper = Factory.getInstance().getCryptoHelper();
		
		if(Utils.isNullOrEmptyTrimmed(mdnSigningAlgorithm)) {
            mdnSigningAlgorithm = ICryptoHelper.DIGEST_SHA1;
        }
		
		Path workFile = FileSystemUtils.createWorkFile();
		
		if(headers) {
			writeMimeDataAndHeadersToFile(workFile);
		} else {
			writeMimeDataToFile(workFile);
		}
		
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
	
	public void writeMimeDataAndHeadersToFile(Path file) throws IOException, MessagingException {
		try (OutputStream out = Files.newOutputStream(file)) {
			data.writeTo(out);
		}
    }
	
	public Disposition getDisposition() {
		return disposition;
	}

	public void setDisposition(Disposition disposition) {
		this.disposition = disposition;
	}

	public Party getLocalParty() {
		return localParty;
	}

	public void setLocalParty(Party localParty) {
		this.localParty = localParty;
	}

	public Party getRemoteParty() {
		return remoteParty;
	}

	public void setRemoteParty(Party remoteParty) {
		this.remoteParty = remoteParty;
	}

	public boolean isMdnReqested() {
		return mdnRequested;
	}

	public void setMdnRequested(boolean mdn) {
		this.mdnRequested = mdn;
	}

	public String getMdnType() {
		return mdnType;
	}

	public void setMdnType(String mdnType) {
		this.mdnType = mdnType;
	}

	public String getAsynchronousMdnUrl() {
		return asynchronousMdnUrl;
	}

	public void setAsynchronousMdnUrl(String asynchronousMdnUrl) {
		this.asynchronousMdnUrl = asynchronousMdnUrl;
	}

	public boolean isRequestSignedMdn() {
		return requestSignedMdn;
	}

	public void setRequestSignedMdn(boolean requestSignedMdn) {
		this.requestSignedMdn = requestSignedMdn;
	}

	public String getMdnSigningAlgorithm() {
		return mdnSigningAlgorithm;
	}

	public void setMdnSigningAlgorithm(String mdnSigningAlgorithm) {
		this.mdnSigningAlgorithm = mdnSigningAlgorithm;
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
