package org.redoubt.application.configuration;

import java.util.HashMap;

import org.redoubt.api.configuration.IParty;

public class BaseParty extends HashMap<String, Object> implements IParty {
	private static final long serialVersionUID = -3347048359486242282L;

	@Override
	public boolean isLocal() {
		return Boolean.parseBoolean((String) get(PartySettingsKeyring.IS_LOCAL));
	}

	@Override
	public void setLocal(boolean isLocal) {
		put(PartySettingsKeyring.IS_LOCAL, Boolean.toString(isLocal));
	}

	@Override
	public String getPartyId() {
		return (String) get(PartySettingsKeyring.PARTY_ID);
	}

	@Override
	public void setPartyId(String partyId) {
		put(PartySettingsKeyring.PARTY_ID, partyId);
	}

	public String getSignCertAlias() {
		return (String) get(PartySettingsKeyring.SIGN_CERT_ALIAS);
	}

	public void setSignCertAlias(String signCertAlias) {
		put(PartySettingsKeyring.SIGN_CERT_ALIAS, signCertAlias);
	}

	public String getEncryptCertAlias() {
		return (String) get(PartySettingsKeyring.ENCRYPT_CERT_ALIAS);
	}

	public void setEncryptCertAlias(String encryptCertAlias) {
		put(PartySettingsKeyring.ENCRYPT_CERT_ALIAS, encryptCertAlias);
	}

	public class PartySettingsKeyring {
        public static final String SIGN_CERT_ALIAS = "signCertAlias";
        public static final String ENCRYPT_CERT_ALIAS = "encryptCertAlias";
        public static final String PARTY_ID = "partyId";
        public static final String IS_LOCAL = "isLocal";
    }
	
}
