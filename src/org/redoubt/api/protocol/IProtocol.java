package org.redoubt.api.protocol;

import org.redoubt.protocol.ProtocolException;

public interface IProtocol {
	void init(IProtocolSettings settings) throws ProtocolException;
	IProtocolSettings getSettings();
	void setSettings(IProtocolSettings name);
	void process(TransferContext context);
	void receive(TransferContext context);
	void send(TransferContext context);
}
