package org.redoubt.transport;

import org.redoubt.api.protocol.IProtocol;
import org.redoubt.api.transport.ITransport;
import org.redoubt.api.transport.ITransportSettings;

public abstract class BaseTransport implements ITransport {
	private IProtocol protocol;
	private boolean isRunning;
	private ITransportSettings settings;

	@Override
	public abstract void init(ITransportSettings settings) throws TransportException;

	@Override
	public abstract void start() throws TransportException;

	@Override
	public abstract void stop() throws TransportException;
	
	@Override
	public void restart() throws TransportException {
		if(isRunning) {
			stop();
			start();
		}
	}

	@Override
	public IProtocol getProtocol() {
		return protocol;
	}

	@Override
	public void setProtocol(IProtocol protocol) {
		this.protocol = protocol;
	}
	
	@Override
	public boolean isRunning() {
		return isRunning;
	}
	
	protected void setRunning(boolean flag) {
		isRunning = flag;
	}
	
	@Override
	public ITransportSettings getSettings() {
		return settings;
	}

	@Override
	public void setSettings(ITransportSettings settings) {
		this.settings = settings;
	}

}

