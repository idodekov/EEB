package org.redoubt.protocol;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.redoubt.api.protocol.IProtocolManager;
import org.redoubt.api.transport.ITransport;
import org.redoubt.transport.TransportException;

public abstract class BaseProtocolManager implements IProtocolManager {
    private static final Logger sLogger = Logger.getLogger(BaseProtocolManager.class);
    private List<ITransport> transports;
    
    public BaseProtocolManager() {
        transports = new ArrayList<ITransport>();
    }
    
    @Override
    public abstract void loadTransports();
    
    protected void addTransport(ITransport transport) {
        transports.add(transport);
    }

    @Override
    public void startTransports() {
        Iterator<ITransport> it = transports.iterator();
        while(it.hasNext()) {
            ITransport transport = it.next();
            
            try {
                transport.start();
            } catch (TransportException e) {
                sLogger.error("An error has occurred while starting transport. " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void stopTransports() {
        Iterator<ITransport> it = transports.iterator();
        while(it.hasNext()) {
            ITransport transport = it.next();
            
            try {
                transport.stop();
            } catch (TransportException e) {
                sLogger.error("An error has occurred while stopping transport. " + e.getMessage(), e);
            }
        }
    }

}
