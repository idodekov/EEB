package org.redoubt.protocol.as2.mdn;

public class Disposition {
	public static final String DISP_AUTOMATIC_ACTION = "automatic-action/MDN-sent-automatically; ";
	public static final String DISP_PROCESSED = DISP_AUTOMATIC_ACTION + "processed";
	public static final String DISP_PROCESSED_ERROR = DISP_PROCESSED + "/Error: ";
	public static final String DISP_UNEXPECTED_ERROR = DISP_PROCESSED_ERROR + "unexpected-processing-error";
	public static final String DISP_AUTHENTICATION_FAILED = DISP_PROCESSED_ERROR + "authentication-failed";
	public static final String DISP_DECRYPTION_FAILED = DISP_PROCESSED_ERROR + "decryption-failed";
	public static final String DISP_SIGNATURE_FAILED = DISP_PROCESSED_ERROR + "integrity-check-failed";
	public static final String DISP_INSUFFICIENT_SECURITY = DISP_PROCESSED_ERROR + "insufficient-message-security";
		
	private String status;
		
	public Disposition() {
		status = DISP_UNEXPECTED_ERROR;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
		
	public boolean isSuccessful() {
		if(status.equals(DISP_PROCESSED)) {
			return true;
		}
		
		return false;
	}
}
