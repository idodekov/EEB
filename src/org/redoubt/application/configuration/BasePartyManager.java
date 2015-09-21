package org.redoubt.application.configuration;

import java.util.HashMap;
import java.util.Map;

import org.redoubt.api.configuration.IParty;
import org.redoubt.api.configuration.IPartyManager;

public abstract class BasePartyManager implements IPartyManager {
	Map<String, IParty> parties;
	
	public BasePartyManager() {
		parties = new HashMap<String, IParty>();
	}
	
	@Override
	public abstract void loadParties();

	public Party getPartyById(String partyId) {
		return (Party) parties.get(partyId);
	}
	
	public void addParty(IParty party) {
		parties.put(party.getPartyId(), party);
	}
	
	public void deleteParty(String id) {
		parties.remove(id);
	}
}
