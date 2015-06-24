package org.redoubt.api.transport;

import org.redoubt.api.protocol.IProtocol;
import org.redoubt.transport.TransportException;

public interface ITransport {
	void init(ITransportSettings settings) throws TransportException;
	void start() throws TransportException;
	void stop() throws TransportException;
	void restart() throws TransportException;
	boolean isRunning();
	IProtocol getProtocol();
	void setProtocol(IProtocol protocol);
	ITransportSettings getSettings();
	void setSettings(ITransportSettings settings);
}
