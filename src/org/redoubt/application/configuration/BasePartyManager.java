package org.redoubt.application.configuration;

import java.util.Map;

import org.redoubt.api.configuration.IParty;
import org.redoubt.api.configuration.IPartyManager;

public abstract class BasePartyManager implements IPartyManager {
	private Map<String, IParty> parties;
	
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
}
