package org.redoubt.api.configuration;

public interface IParty {
	boolean isLocal();
	void setLocal(boolean local);
	public String getPartyId();
	public void setPartyId(String partyId);
}
