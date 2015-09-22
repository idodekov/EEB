package org.redoubt.api.protocol;

public interface IMessage {
	void packageMessage(IProtocolSettings settings) throws Exception;
	void unpackageMessage(IProtocolSettings settings) throws Exception;
}
