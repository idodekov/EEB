package org.redoubt.application.configuration;

public class LocalParty extends BaseParty {
	private static final long serialVersionUID = -4120613099651523573L;

	public boolean isEncryptionEnabled() {
    	return Boolean.parseBoolean((String) get(LocalPartySettingsKeyring.ENCRYPT));
    }
    
    public void setEncryptionEnabled(boolean encrypt) {
    	put(LocalPartySettingsKeyring.ENCRYPT, Boolean.toString(encrypt));
    }
    
    public String getEncryptCertKeyPassword() {
        return (String) get(LocalPartySettingsKeyring.ENCRYPT_CERT_KEY_PASSWORD);
    }
    
    public void setEncryptCertKeyPassword(String encryptCertKeyPassword) {
        put(LocalPartySettingsKeyring.ENCRYPT_CERT_KEY_PASSWORD, encryptCertKeyPassword);
    }
    
    public String getEncryptAlgorithm() {
        return (String) get(LocalPartySettingsKeyring.ENCRYPT_ALGORITHM);
    }
    
    public void setEncryptAlgorithm(String alg) {
        put(LocalPartySettingsKeyring.ENCRYPT_ALGORITHM, alg);
    }
    
    public boolean isSigningEnabled() {
    	return Boolean.parseBoolean((String) get(LocalPartySettingsKeyring.SIGN));
    }
    
    public void setSigningEnabled(boolean sign) {
    	put(LocalPartySettingsKeyring.SIGN, Boolean.toString(sign));
    }
    
    public String getSignCertKeyPassword() {
        return (String) get(LocalPartySettingsKeyring.SIGN_CERT_KEY_PASSWORD);
    }
    
    public void setSignCertKeyPassword(String signCertKeyPassword) {
        put(LocalPartySettingsKeyring.SIGN_CERT_KEY_PASSWORD, signCertKeyPassword);
    }
    
    public String getSignDigestAlgorithm() {
        return (String) get(LocalPartySettingsKeyring.SIGN_DIGEST_ALGORITHM);
    }
    
    public void setSignDigestAlgorithm(String alg) {
        put(LocalPartySettingsKeyring.SIGN_DIGEST_ALGORITHM, alg);
    }
    
    public boolean isCompressionEnabled() {
    	return Boolean.parseBoolean((String) get(LocalPartySettingsKeyring.COMPRESS));
    }
    
    public void setCompressionEnabled(boolean compress) {
    	put(LocalPartySettingsKeyring.COMPRESS, Boolean.toString(compress));
    }
    
    public String getCompressionAlgorithm() {
        return (String) get(LocalPartySettingsKeyring.COMPRESS_ALGORITHM);
    }
    
    public void setCompressionAlgorithm(String alg) {
        put(LocalPartySettingsKeyring.COMPRESS_ALGORITHM, alg);
    }
	
	public class LocalPartySettingsKeyring {
		public static final String SIGN = "sign";
	    public static final String SIGN_CERT_KEY_PASSWORD = "signCertKeyPassword";
	    public static final String SIGN_DIGEST_ALGORITHM = "signDigestAlgorithm";
	    public static final String ENCRYPT = "encrypt";
	    public static final String ENCRYPT_CERT_KEY_PASSWORD = "encryptCertKeyPassword";
	    public static final String ENCRYPT_ALGORITHM = "encryptAlgorithm";
	    public static final String COMPRESS = "compress";
	    public static final String COMPRESS_ALGORITHM = "compressAlgorithm";
    }
}
