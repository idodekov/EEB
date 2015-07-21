package org.redoubt.protocol.none;

import org.redoubt.api.protocol.TransferContext;
import org.redoubt.protocol.BaseProtocol;
import org.redoubt.protocol.ProtocolException;

public class NoneProtocol extends BaseProtocol {

    @Override
    public void receive(TransferContext context) throws ProtocolException {
        // Does nothing
    }

    @Override
    public void send(TransferContext context) throws ProtocolException {
        // Does nothing
    }

}
