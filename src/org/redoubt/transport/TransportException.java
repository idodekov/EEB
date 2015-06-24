package org.redoubt.transport;

public class TransportException extends Exception {
	private static final long serialVersionUID = -5204429169530127093L;

	public TransportException() {
		super();
	}

	public TransportException(String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public TransportException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public TransportException(String arg0) {
		super(arg0);
	}

	public TransportException(Throwable arg0) {
		super(arg0);
	}
	
	

}
