package org.redoubt.api.protocol;

import org.redoubt.protocol.ProtocolException;

public interface IProtocol {
	void init(IProtocolSettings settings) throws ProtocolException;
	IProtocolSettings getSettings();
	void setSettings(IProtocolSettings name);
	void process(TransferContext context) throws ProtocolException;
	void receive(TransferContext context) throws ProtocolException;
	void send(TransferContext context) throws ProtocolException;
}
