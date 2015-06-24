package org.redoubt.protocol;

public class ProtocolException extends Exception {
	private static final long serialVersionUID = 4583888893278997252L;

	public ProtocolException() {
		super();
	}

	public ProtocolException(String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public ProtocolException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public ProtocolException(String arg0) {
		super(arg0);
	}

	public ProtocolException(Throwable arg0) {
		super(arg0);
	}

}
