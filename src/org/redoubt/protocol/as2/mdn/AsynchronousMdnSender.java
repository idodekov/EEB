package org.redoubt.protocol.as2.mdn;

import java.nio.file.Path;
import java.util.Map;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.redoubt.protocol.as2.HttpClientUtils;
import org.redoubt.util.FileSystemUtils;

public class AsynchronousMdnSender extends TimerTask {
	private static final Logger sLogger = Logger.getLogger(AsynchronousMdnSender.class);
	
	private Path mdnFile;
	private Map<String, String> mdnHeaders;
	private String url;
	
	public AsynchronousMdnSender(Path mdnFile, Map<String, String> mdnHeaders, String url) {
		this.mdnFile = mdnFile;
		this.mdnHeaders = mdnHeaders;
		this.url = url;
	}

	@Override
	public void run() {
		new Thread() {
			@Override
			public void run() {
				try {
					HttpClientUtils.sendPostRequest(null, mdnFile, mdnHeaders, url);
					FileSystemUtils.removeWorkFile(mdnFile);
				} catch (Exception e) {
					sLogger.error("An error has occured while sending asynchronous MDN. " + e.getMessage(), e);
				}
			}
		}.start();
	}
	
	
}
