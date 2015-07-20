package org.redoubt.protocol.none;

import org.redoubt.api.protocol.TransferContext;
import org.redoubt.protocol.BaseProtocol;

public class NoneProtocol extends BaseProtocol {

    @Override
    public void receive(TransferContext context) {
        // Does nothing
    }

    @Override
    public void send(TransferContext context) {
        // Does nothing
    }

}
