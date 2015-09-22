package org.redoubt.application.configuration;

import java.util.HashMap;

import org.redoubt.api.configuration.IParty;

public class Party extends HashMap<String, Object> implements IParty {
	private static final long serialVersionUID = -3347048359486242282L;

	public Party() {
		//Set default values
		put(PartySettingsKeyring.PARTY_ID, "");
		put(PartySettingsKeyring.SIGN_CERT_ALIAS, "");
		put(PartySettingsKeyring.ENCRYPT_CERT_ALIAS, "");
		put(PartySettingsKeyring.SIGN, Boolean.toString(false));
		put(PartySettingsKeyring.ENCRYPT, Boolean.toString(false));
		put(PartySettingsKeyring.SIGN_CERT_KEY_PASSWORD, "");
		put(PartySettingsKeyring.SIGN_DIGEST_ALGORITHM, "sha1");
		put(PartySettingsKeyring.ENCRYPT_CERT_KEY_PASSWORD, "");
		put(PartySettingsKeyring.ENCRYPT_ALGORITHM, "3des");
		put(PartySettingsKeyring.COMPRESS, Boolean.toString(false));
		put(PartySettingsKeyring.COMPRESS_ALGORITHM, "zlib");
		put(PartySettingsKeyring.REQUEST_MDN, "false");
		put(PartySettingsKeyring.MDN_TYPE, "synhronous");
		put(PartySettingsKeyring.REQUEST_SIGNED_MDN, "false");
		put(PartySettingsKeyring.MDN_SIGNING_ALGORITHM, "sha1");
	}
	
	@Override
	public String getPartyId() {
		return (String) get(PartySettingsKeyring.PARTY_ID);
	}

	@Override
	public void setPartyId(String partyId) {
		put(PartySettingsKeyring.PARTY_ID, partyId);
	}

	public String getSignCertAlias() {
		return (String) get(PartySettingsKeyring.SIGN_CERT_ALIAS);
	}

	public void setSignCertAlias(String signCertAlias) {
		put(PartySettingsKeyring.SIGN_CERT_ALIAS, signCertAlias);
	}

	public String getEncryptCertAlias() {
		return (String) get(PartySettingsKeyring.ENCRYPT_CERT_ALIAS);
	}

	public void setEncryptCertAlias(String encryptCertAlias) {
		put(PartySettingsKeyring.ENCRYPT_CERT_ALIAS, encryptCertAlias);
	}
	
	public boolean isEncryptionEnabled() {
    	return Boolean.parseBoolean((String) get(PartySettingsKeyring.ENCRYPT));
    }
    
    public void setEncryptionEnabled(boolean encrypt) {
    	put(PartySettingsKeyring.ENCRYPT, Boolean.toString(encrypt));
    }
    
    public String getEncryptCertKeyPassword() {
        return (String) get(PartySettingsKeyring.ENCRYPT_CERT_KEY_PASSWORD);
    }
    
    public void setEncryptCertKeyPassword(String encryptCertKeyPassword) {
        put(PartySettingsKeyring.ENCRYPT_CERT_KEY_PASSWORD, encryptCertKeyPassword);
    }
    
    public String getEncryptAlgorithm() {
        return (String) get(PartySettingsKeyring.ENCRYPT_ALGORITHM);
    }
    
    public void setEncryptAlgorithm(String alg) {
        put(PartySettingsKeyring.ENCRYPT_ALGORITHM, alg);
    }
    
    public boolean isSigningEnabled() {
    	return Boolean.parseBoolean((String) get(PartySettingsKeyring.SIGN));
    }
    
    public void setSigningEnabled(boolean sign) {
    	put(PartySettingsKeyring.SIGN, Boolean.toString(sign));
    }
    
    public String getSignCertKeyPassword() {
        return (String) get(PartySettingsKeyring.SIGN_CERT_KEY_PASSWORD);
    }
    
    public void setSignCertKeyPassword(String signCertKeyPassword) {
        put(PartySettingsKeyring.SIGN_CERT_KEY_PASSWORD, signCertKeyPassword);
    }
    
    public String getSignDigestAlgorithm() {
        return (String) get(PartySettingsKeyring.SIGN_DIGEST_ALGORITHM);
    }
    
    public void setSignDigestAlgorithm(String alg) {
        put(PartySettingsKeyring.SIGN_DIGEST_ALGORITHM, alg);
    }
    
    public boolean isCompressionEnabled() {
    	return Boolean.parseBoolean((String) get(PartySettingsKeyring.COMPRESS));
    }
    
    public void setCompressionEnabled(boolean compress) {
    	put(PartySettingsKeyring.COMPRESS, Boolean.toString(compress));
    }
    
    public String getCompressionAlgorithm() {
        return (String) get(PartySettingsKeyring.COMPRESS_ALGORITHM);
    }
    
    public void setCompressionAlgorithm(String alg) {
        put(PartySettingsKeyring.COMPRESS_ALGORITHM, alg);
    }
    
    public boolean isRequestMdn() {
    	return Boolean.parseBoolean((String) get(PartySettingsKeyring.REQUEST_MDN));
    }
    
    public void setRequestMdn(boolean requestMdn) {
    	put(PartySettingsKeyring.REQUEST_MDN, Boolean.toString(requestMdn));
    }
    
    public String getMdnType() {
        return (String) get(PartySettingsKeyring.MDN_TYPE);
    }
    
    public void setMdnType(String mdnType) {
        put(PartySettingsKeyring.MDN_TYPE, mdnType);
    }
    
    public String getAsynchronousMdnUrl() {
        return (String) get(PartySettingsKeyring.ASYNCHRONOUS_MDN_URL);
    }
    
    public void setAsynchronousMdnUrl(String asynchronousMdnUrl) {
        put(PartySettingsKeyring.ASYNCHRONOUS_MDN_URL, asynchronousMdnUrl);
    }
    
    public boolean isRequestSignedMdn() {
    	return Boolean.parseBoolean((String) get(PartySettingsKeyring.REQUEST_SIGNED_MDN));
    }
    
    public void setRequestSignedMdn(boolean requestSignedMdn) {
    	put(PartySettingsKeyring.REQUEST_SIGNED_MDN, Boolean.toString(requestSignedMdn));
    }
    
    public String getMdnSigningAlgorithm() {
        return (String) get(PartySettingsKeyring.MDN_SIGNING_ALGORITHM);
    }
    
    public void setMdnSigningAlgorithm(String mdnSigningAlgorithm) {
        put(PartySettingsKeyring.MDN_SIGNING_ALGORITHM, mdnSigningAlgorithm);
    }

	public class PartySettingsKeyring {
        public static final String SIGN_CERT_ALIAS = "signCertAlias";
        public static final String ENCRYPT_CERT_ALIAS = "encryptCertAlias";
        public static final String PARTY_ID = "partyId";
        public static final String SIGN = "sign";
	    public static final String SIGN_CERT_KEY_PASSWORD = "signCertKeyPassword";
	    public static final String SIGN_DIGEST_ALGORITHM = "signDigestAlgorithm";
	    public static final String ENCRYPT = "encrypt";
	    public static final String ENCRYPT_CERT_KEY_PASSWORD = "encryptCertKeyPassword";
	    public static final String ENCRYPT_ALGORITHM = "encryptAlgorithm";
	    public static final String COMPRESS = "compress";
	    public static final String COMPRESS_ALGORITHM = "compressAlgorithm";
	    public static final String REQUEST_MDN = "requestMdn";
	    public static final String MDN_TYPE = "mdnType";
	    public static final String ASYNCHRONOUS_MDN_URL = "asynchronousMdnUrl";
	    public static final String REQUEST_SIGNED_MDN = "requestSignedMdn";
	    public static final String MDN_SIGNING_ALGORITHM = "mdnSigningAlgorithm";
    }
	
}
