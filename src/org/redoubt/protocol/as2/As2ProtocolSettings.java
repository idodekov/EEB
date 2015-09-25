package org.redoubt.protocol.as2;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.redoubt.api.protocol.IProtocolSettings;
import org.redoubt.transport.SettingsHolder;

public class As2ProtocolSettings extends SettingsHolder implements IProtocolSettings {
	private static final long serialVersionUID = -3261720417924355073L;
	public static final String PROTOCOL_NAME = "as2";
	
	public As2ProtocolSettings() {
		//Set default values
		put(As2ProtocolSettingsKeyring.ENFORCE_SIGNING, Boolean.toString(false));
		put(As2ProtocolSettingsKeyring.ENFORCE_ENCRYPTION, Boolean.toString(false));
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
    
    public boolean isEncryptionEnforced() {
    	return Boolean.parseBoolean((String) get(As2ProtocolSettingsKeyring.ENFORCE_ENCRYPTION));
    }
    
    public void setEncryptionEnforced(boolean enforce) {
    	put(As2ProtocolSettingsKeyring.ENFORCE_ENCRYPTION, Boolean.toString(enforce));
    }
    
    public boolean isSigningEnforced() {
    	return Boolean.parseBoolean((String) get(As2ProtocolSettingsKeyring.ENFORCE_SIGNING));
    }
    
    public void setSigningEnforced(boolean enforce) {
    	put(As2ProtocolSettingsKeyring.ENFORCE_SIGNING, Boolean.toString(enforce));
    }
    
    public Path getProductionFolder() {
        return Paths.get((String) get(As2ProtocolSettingsKeyring.PRODUCTION_FOLDER));
    }
    
    public void setProductionFolder(Path folder) {
        put(As2ProtocolSettingsKeyring.PRODUCTION_FOLDER, folder);
    }
    
    public class As2ProtocolSettingsKeyring {
        public static final String DIRECTION = "direction";
        public static final String FROM = "from";
        public static final String TO = "to";
        public static final String URL = "url";
        public static final String PRODUCTION_FOLDER = "productionFolder";
        public static final String ENFORCE_SIGNING = "enforceSigning";
        public static final String ENFORCE_ENCRYPTION = "enforceEncryption";
    }

}
