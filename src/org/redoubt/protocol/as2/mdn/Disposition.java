package org.redoubt.protocol.as2.mdn;

public class Disposition {
		// The MDN has been automatically generated
		public static final String DISP_AUTOMATIC_ACTION = "automatic-action/MDN-sent-automatically; ";
		// The message has been processed successfully
		public static final String DISP_PROCESSED = DISP_AUTOMATIC_ACTION + "processed";
		// An error occurred during message processing
		public static final String DISP_PROCESSED_ERROR = DISP_PROCESSED + "/Error: ";
		// An unexpected error occurred during processing
		public static final String DISP_UNEXPECTED_ERROR = DISP_PROCESSED_ERROR + "unexpected-processing-error";
		// The message sender or receiver could not be authenticated
		public static final String DISP_AUTHENTICATION_FAILED = DISP_PROCESSED_ERROR + "authentication-failed";
		// The message could not be decrypted
		public static final String DISP_DECRYPTION_FAILED = DISP_PROCESSED_ERROR + "decryption-failed";
		// The message signature could not be verified
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
		
		
}
