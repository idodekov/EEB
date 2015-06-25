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

}
