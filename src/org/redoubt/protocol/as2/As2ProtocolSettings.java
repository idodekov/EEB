package org.redoubt.protocol.as2;

import org.redoubt.api.protocol.IProtocolSettings;
import org.redoubt.transport.SettingsHolder;

public class As2ProtocolSettings extends SettingsHolder implements IProtocolSettings {
	private static final long serialVersionUID = -3261720417924355073L;
	public static final String PROTOCOL_NAME = "as2";

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
    
    public class As2ProtocolSettingsKeyring {
        public static final String DIRECTION = "direction";
        public static final String FROM = "from";
        public static final String TO = "to";
        public static final String URL = "url";
        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";
    }

}
