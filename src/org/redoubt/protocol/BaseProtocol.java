package org.redoubt.protocol;

import org.apache.log4j.Logger;
import org.redoubt.api.protocol.IProtocol;
import org.redoubt.api.protocol.IProtocolSettings;
import org.redoubt.api.protocol.TransferContext;
import org.redoubt.application.configuration.ConfigurationConstants;

public abstract class BaseProtocol implements IProtocol {
    private static final Logger sLogger = Logger.getLogger(BaseProtocol.class);
    private IProtocolSettings protocolSettings;

    @Override
    public void init(IProtocolSettings settings) throws ProtocolException {
        protocolSettings = settings;
    }
    
    @Override
    public void process(TransferContext context) {
        String direction = getSettings().getDirection();
        
        if(ConfigurationConstants.DIRECTION_INBOUND.equalsIgnoreCase(direction)) {
            receive(context);
        } else if(ConfigurationConstants.DIRECTION_OUTBOUND.equalsIgnoreCase(direction)) {
            send(context);
        } else {
            sLogger.error("Direction is not set for protocol of type [" + getSettings().getProtocolName() + "].");
        }
        
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
