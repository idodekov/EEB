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

	public IParty getPartyById(String partyId) {
		return parties.get(partyId);
	}
	
	public LocalParty getLocalPartyById(String partyId) {
		IParty party = parties.get(partyId);
		
		if(party.isLocal()) {
			return (LocalParty) party;
		}
		
		return null;
	}
	
	public RemoteParty getRemotePartyById(String partyId) {
		IParty party = parties.get(partyId);
		
		if(!party.isLocal()) {
			return (RemoteParty) party;
		}
		
		return null;
	}
	
	public void addParty(IParty party) {
		parties.put(party.getPartyId(), party);
	}
	
	public void deleteParty(String id) {
		parties.remove(id);
	}
}
