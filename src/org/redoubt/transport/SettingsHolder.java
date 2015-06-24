package org.redoubt.transport;

import java.util.HashMap;

public class SettingsHolder extends HashMap<String, Object> {
	private static final long serialVersionUID = -3212552571518174427L;
	
	public SettingsHolder getProtocolSettings() {
		return (SettingsHolder) get(SettingsKeyring.PRTOCOL_SETTINGS);
	}

	public void setProtocolSettings(SettingsHolder protocolSettings) {
		put(SettingsKeyring.PRTOCOL_SETTINGS, protocolSettings);
	}
	
	public String getType() {
		return (String) get(SettingsKeyring.TYPE);
	}

	public void setType(String type) {
		put(SettingsKeyring.TYPE, type);
	}
	
	public class SettingsKeyring {
		public static final String PRTOCOL_SETTINGS = "protocolSettings";
		public static final String TYPE = "type";
	}
}

