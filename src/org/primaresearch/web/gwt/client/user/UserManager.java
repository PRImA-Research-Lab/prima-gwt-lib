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
package org.primaresearch.web.gwt.client.user;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.primaresearch.web.gwt.shared.user.SessionData;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Handles user management related communication with the server.
 *  
 * @author Christian Clausner
 *
 */
public class UserManager {

	private UserServiceAsync syncService = GWT.create(UserService.class);
	
	private SessionData sessionData;
	
	private Set<LogOnListener> logOnListeners = new HashSet<LogOnListener>();

	/**
	 * Adds a "user log on" listener
	 */
	public void addListener(LogOnListener listener) {
		this.logOnListeners.add(listener);
	}
	
	/**
	 * Removes a "user log on" listener
	 */
	public void removeListener(LogOnListener listener) {
		this.logOnListeners.remove(listener);
	}
	
	/**
	 * Returns the URL to retrieve the current document page image. 
	 */
	public String getDocumentImageWebServiceUrl() {
		if (sessionData != null)
			return sessionData.getDocumentImageUrl;
		return null;
	}

	/**
	 * Logs the user on.
	 * @param applicationId Web application ID
	 * @param documentId ID of the current document
	 * @param attachmentId ID of the current attachment for the document (e.g. PAGE XML file)
	 * @param userToken Encrypted token with user information
	 */
	public void logOn(String applicationId, String documentId, String attachmentId, String userToken) {
		
		//Authenticate on server side
	    AsyncCallback<SessionData> callback = new AsyncCallback<SessionData>() {
	    	public void onFailure(Throwable caught) {
    			notifyLogonListeners(false);
	    	}

	    	public void onSuccess(SessionData data) {
	    		if (data != null) {
	    			sessionData = data;
	    			notifyLogonListeners(true);
	    		} else {
	    			notifyLogonListeners(false);
	    		}
	    	}
	    };
	    syncService.logOn(applicationId, documentId, attachmentId, userToken, callback);
	    
	    //Window.alert("GWT HostPageBaseURL: "+GWT.getHostPageBaseURL());
	    //System.out.println("GWT HostPageBaseURL: "+GWT.getHostPageBaseURL());
	}
	
	/**
	 * Notifies all listeners of the result of the log on.
	 */
	private void notifyLogonListeners(boolean success) {
		for (Iterator<LogOnListener> it = logOnListeners.iterator(); it.hasNext(); ) {
			if (success)
				it.next().logOnSuccessful(this);
			else
				it.next().logOnFailed(this);
		}
	}
	

	/**
	 * Interface for "user log on" listeners.
	 * 
	 * @author Christian Clausner
	 *
	 */
	public static interface LogOnListener {
		
		/**
		 * Called if the user has been successfully logged on
		 */
		void logOnSuccessful(UserManager userManager);
		
		/**
		 * Called if the user could not be logged on
		 */
		void logOnFailed(UserManager userManager);
	}
	
}
