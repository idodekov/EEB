package org.redoubt.api.configuration;

import org.redoubt.application.configuration.Party;

public interface IPartyManager {
	void loadParties();
	Party getPartyById(String partyId);
	void addParty(IParty party);
	void deleteParty(String id);
}
