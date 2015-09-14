package org.redoubt.protocol.as2;

import org.redoubt.api.protocol.IProtocolSettings;
import org.redoubt.transport.SettingsHolder;

public class As2ProtocolSettings extends SettingsHolder implements IProtocolSettings {
	private static final long serialVersionUID = -3261720417924355073L;
	public static final String PROTOCOL_NAME = "as2";
	
	public As2ProtocolSettings() {
		//Set default values
		put(As2ProtocolSettingsKeyring.USERNAME, "");
		put(As2ProtocolSettingsKeyring.PASSWORD, "");
		put(As2ProtocolSettingsKeyring.SIGN, Boolean.toString(false));
		put(As2ProtocolSettingsKeyring.ENCRYPT, Boolean.toString(false));
		put(As2ProtocolSettingsKeyring.SIGN_CERT_KEY_PASSWORD, "");
		put(As2ProtocolSettingsKeyring.SIGN_CERT_ALIAS, "");
		put(As2ProtocolSettingsKeyring.ENCRYPT_CERT_ALIAS, "");
		put(As2ProtocolSettingsKeyring.SIGN_DIGEST_ALGORITHM, "sha1");
		put(As2ProtocolSettingsKeyring.ENCRYPT_ALGORITHM, "3des");
	}

	@Override
	public String getProtocolName() {
		return PROTOCOL_NAME;
	}
	
	@Override
    public String getDirection() {
        return (String) get(As2ProtocolSettingsKeyring.DIRECTION);
    }
    
    public void setDirection(String direction) {
        put(As2ProtocolSettingsKeyring.DIRECTION, direction);
    }
    
    public String getFrom() {
        return (String) get(As2ProtocolSettingsKeyring.FROM);
    }
    
    public void setFrom(String from) {
        put(As2ProtocolSettingsKeyring.FROM, from);
    }
    
    public String getTo() {
        return (String) get(As2ProtocolSettingsKeyring.TO);
    }
    
    public void setTo(String to) {
        put(As2ProtocolSettingsKeyring.TO, to);
    }
    
    public String getUrl() {
        return (String) get(As2ProtocolSettingsKeyring.URL);
    }
    
    public void setUrl(String url) {
        put(As2ProtocolSettingsKeyring.URL, url);
    }
    
    public String getUsername() {
        return (String) get(As2ProtocolSettingsKeyring.USERNAME);
    }
    
    public void setUsername(String username) {
        put(As2ProtocolSettingsKeyring.USERNAME, username);
    }
    
    public String getPassword() {
        return (String) get(As2ProtocolSettingsKeyring.PASSWORD);
    }
    
    public void setPassword(String password) {
        put(As2ProtocolSettingsKeyring.PASSWORD, password);
    }
    
    public boolean isEncryptionEnabled() {
    	return Boolean.parseBoolean((String) get(As2ProtocolSettingsKeyring.ENCRYPT));
    }
    
    public void setEncryptionEnabled(boolean encrypt) {
    	put(As2ProtocolSettingsKeyring.ENCRYPT, Boolean.toString(encrypt));
    }
    
    public String getEncryptCertAlias() {
        return (String) get(As2ProtocolSettingsKeyring.ENCRYPT_CERT_ALIAS);
    }
    
    public void setEncryptCertAlias(String encryptCertAlias) {
        put(As2ProtocolSettingsKeyring.ENCRYPT_CERT_ALIAS, encryptCertAlias);
    }
    
    public String getEncryptDigestAlgorithm() {
        return (String) get(As2ProtocolSettingsKeyring.ENCRYPT_ALGORITHM);
    }
    
    public void setEncryptDigestAlgorithm(String alg) {
        put(As2ProtocolSettingsKeyring.ENCRYPT_ALGORITHM, alg);
    }
    
    public boolean isSigningEnabled() {
    	return Boolean.parseBoolean((String) get(As2ProtocolSettingsKeyring.SIGN));
    }
    
    public void setSigningEnabled(boolean sign) {
    	put(As2ProtocolSettingsKeyring.SIGN, Boolean.toString(sign));
    }
    
    public String getSignCertAlias() {
        return (String) get(As2ProtocolSettingsKeyring.SIGN_CERT_ALIAS);
    }
    
    public void setSignCertAlias(String signCertAlias) {
        put(As2ProtocolSettingsKeyring.SIGN_CERT_ALIAS, signCertAlias);
    }
    
    public String getSignCertKeyPassword() {
        return (String) get(As2ProtocolSettingsKeyring.SIGN_CERT_KEY_PASSWORD);
    }
    
    public void setSignCertKeyPassword(String signCertKeyPassword) {
        put(As2ProtocolSettingsKeyring.SIGN_CERT_KEY_PASSWORD, signCertKeyPassword);
    }
    
    public String getSignDigestAlgorithm() {
        return (String) get(As2ProtocolSettingsKeyring.SIGN_DIGEST_ALGORITHM);
    }
    
    public void setSignDigestAlgorithm(String alg) {
        put(As2ProtocolSettingsKeyring.SIGN_DIGEST_ALGORITHM, alg);
    }
    
    public class As2ProtocolSettingsKeyring {
        public static final String DIRECTION = "direction";
        public static final String FROM = "from";
        public static final String TO = "to";
        public static final String URL = "url";
        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";
        public static final String SIGN = "sign";
        public static final String SIGN_CERT_ALIAS = "signCertAlias";
        public static final String SIGN_CERT_KEY_PASSWORD = "signCertKeyPassword";
        public static final String SIGN_DIGEST_ALGORITHM = "signDigestAlgorithm";
        public static final String ENCRYPT = "encrypt";
        public static final String ENCRYPT_CERT_ALIAS = "encryptCertAlias";
        public static final String ENCRYPT_ALGORITHM = "encryptAlgorithm";
    }

}
