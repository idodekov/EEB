package org.redoubt.protocol;

import org.redoubt.api.protocol.IProtocol;
import org.redoubt.api.protocol.IProtocolSettings;

public abstract class BaseProtocol implements IProtocol {
    private IProtocolSettings protocolSettings;

    @Override
    public void init(IProtocolSettings settings) throws ProtocolException {
        protocolSettings = settings;
    }

    @Override
    public IProtocolSettings getSettings() {
        return protocolSettings;
    }

    @Override
    public void setSettings(IProtocolSettings protocolSettings) {
        this.protocolSettings = protocolSettings;
    }

}
