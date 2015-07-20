package org.redoubt.protocol.none;

import org.redoubt.api.protocol.IProtocolSettings;
import org.redoubt.transport.SettingsHolder;

public class NoneProtocolSettings extends SettingsHolder implements IProtocolSettings {
    private static final long serialVersionUID = 982012325455327384L;
    public static final String PROTOCOL_NAME = "none";

    @Override
    public String getProtocolName() {
        return PROTOCOL_NAME;
    }

    @Override
    public String getDirection() {
        return (String) get(NoneProtocolSettingsKeyring.DIRECTION);
    }
    
    public void setDirection(String direction) {
        put(NoneProtocolSettingsKeyring.DIRECTION, direction);
    }
    
    public class NoneProtocolSettingsKeyring {
        public static final String DIRECTION = "direction";
    }
}
