package org.redoubt.protocol.as2;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.redoubt.api.protocol.IMdnMonitor;
import org.redoubt.api.protocol.IMessage;
import org.redoubt.util.Utils;

public class As2InMemoryMdnMonitor implements IMdnMonitor {
	private static final Logger sLogger = Logger.getLogger(As2InMemoryMdnMonitor.class);
	private Map<String, As2Message> unconfirmedMessages;
	
	public As2InMemoryMdnMonitor() {
		unconfirmedMessages = new HashMap<String, As2Message>();
	}

	@Override
	public void registerMessage(String uniqueId, IMessage message) {
		As2Message as2Message = (As2Message) message;
		
		String mic = as2Message.getMic();
		String fromEmail = as2Message.getFromEmail();
		String messageId = as2Message.getMessageId();
		String messageDate = as2Message.getMessageDate();

		if(Utils.isNullOrEmptyTrimmed(mic)) {
			sLogger.error("Can't register message for confirmation as it doesn't have a MIC assigned.");
		}
		
		if(Utils.isNullOrEmptyTrimmed(fromEmail)) {
			sLogger.error("Can't register message for confirmation as it doesn't have a from address assigned.");
		}
		
		if(Utils.isNullOrEmptyTrimmed(messageId)) {
			sLogger.error("Can't register message for confirmation as it doesn't have a message id assigned.");
		}
		
		if(Utils.isNullOrEmptyTrimmed(messageDate)) {
			sLogger.error("Can't register message for confirmation as it doesn't have a message date assigned.");
		}
		
		sLogger.info("Message with MIC [" + uniqueId + "] has been registered and is awaiting confirmation from a receipt.");
		unconfirmedMessages.put(uniqueId, as2Message);
	}

	@Override
	public boolean isMessageRegistered(String uniqueId) {
		return unconfirmedMessages.containsKey(uniqueId);
	}

	@Override
	public void confirmAndDeregisterMessage(String uniqueId) {
		As2Message message = unconfirmedMessages.get(uniqueId);
		if(message == null) {
			sLogger.error("Attempting to confirm message with MIC [" + uniqueId + "], however such message doesn't exist.");
			return;
		}
		
		sLogger.info("Message with MIC [" + uniqueId + "] and MessageId [" + message.getMessageId() + "] has been confirmed with a receipt.");
		unconfirmedMessages.remove(uniqueId);
	}
}
