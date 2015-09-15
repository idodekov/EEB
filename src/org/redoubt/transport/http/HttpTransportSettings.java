package org.redoubt.transport.http;

import org.redoubt.api.transport.ITransportSettings;
import org.redoubt.transport.SettingsHolder;

public class HttpTransportSettings extends SettingsHolder implements ITransportSettings {
	private static final long serialVersionUID = 5063476342919936607L;
	public static final String TRANSPORT_NAME = "http";

	@Override
	public String getTransportName() {
		return TRANSPORT_NAME;
	}

	public int getPort() {
		return Integer.parseInt((String) get(HttpTransportSettingsKeyring.PORT));
	}

	public void setPort(int port) {
		put(HttpTransportSettingsKeyring.PORT, port);
	}

	public boolean isTlsEnabled() {
		return (boolean) get(HttpTransportSettingsKeyring.TLS_ENABLED);
	}

	public void setTlsEnabled(boolean secure) {
		put(HttpTransportSettingsKeyring.TLS_ENABLED, secure);
	}
	
	public String getContextPath() {
		return (String) get(HttpTransportSettingsKeyring.CONTEXT_PATH);
	}

	public void setContextPath(String contextPath) {
		put(HttpTransportSettingsKeyring.CONTEXT_PATH, contextPath);
	}
	
	public String getName() {
		return (String) get(HttpTransportSettingsKeyring.NAME);
	}

	public void setName(String name) {
		put(HttpTransportSettingsKeyring.NAME, name);
	}
	
	public class HttpTransportSettingsKeyring {
		public static final String PORT = "port";
		public static final String TLS_ENABLED = "tlsEnabled";
		public static final String CONTEXT_PATH = "contextPath";
		public static final String NAME = "name";
	}
}
