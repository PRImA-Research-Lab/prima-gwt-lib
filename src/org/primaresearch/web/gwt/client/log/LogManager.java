/*
 * Copyright 2014 PRImA Research Lab, University of Salford, United Kingdom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.primaresearch.web.gwt.client.log;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Central class for client-to-server logging.
 * 
 * @author Christian Clausner
 *
 */
public class LogManager {
	
	private String loggerId;

	private LoggingServiceAsync syncService = null;
	
	/**
	 * Constructor
	 * @param loggerId Prefix for log messages
	 */
	public LogManager(String loggerId) {
		this.loggerId = loggerId;
		try {
			syncService = GWT.create(LoggingService.class);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
	
	/**
	 * Logs an info message  
	 * @param id Message ID
	 * @param message Message text
	 */
	public void logInfo(int id, String message) {
	    log(LoggingService.LEVEL_INFO, id, message);
	}
	
	/**
	 * Logs a warning message  
	 * @param id Message ID
	 * @param message Message text
	 */
	public void logWarning(int id, String message) {
	    log(LoggingService.LEVEL_WARNING, id, message);
	}

	/**
	 * Logs a warning message  
	 * @param id Message ID
	 * @param message Message text
	 */
	public void logError(int id, String message) {
	    log(LoggingService.LEVEL_ERROR, id, message);
	}
	
	/**
	 * Sends the given log message to the server (asynchronous)
	 * @param level Severity (info, warning, or error; see LoggingService.LEVEL_ constants)
	 * @param id Message ID
	 * @param message Message text
	 */
	private void log(int level, int id, String message) {
		try {
		    AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
		    	public void onFailure(Throwable caught) {
		    	}
		    	public void onSuccess(Boolean contentObjects) {
		    	}
		    };
		    syncService.log(level, id, loggerId+": "+message, callback);
		} catch (Exception exc) {
		}
	}

}
