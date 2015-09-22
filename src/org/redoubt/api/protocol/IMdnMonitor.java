package org.redoubt.api.protocol;

public interface IMdnMonitor {
	void registerMessage(String uniqueId, IMessage message);
	boolean isMessageRegistered(String uniqueId);
	void confirmAndDeregisterMessage(String uniqueId);
}
