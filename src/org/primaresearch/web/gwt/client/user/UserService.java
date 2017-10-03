/*
 * Copyright 2015 PRImA Research Lab, University of Salford, United Kingdom
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

import org.primaresearch.web.gwt.shared.RemoteException;
import org.primaresearch.web.gwt.shared.user.SessionData;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Exchange of user related data
 *
 * @author Christian Clausner
 *
 */
@RemoteServiceRelativePath("user")
public interface UserService extends RemoteService  {

	/**
	 * User log on
	 * @param applicationId Web application ID
	 * @param documentId Document ID
	 * @param attachmentId ID of document attachment (e.g. PAGE XML file)
	 * @param userData Encrypted user data
	 * @return Session data
	 * @throws RemoteException
	 */
	public SessionData logOn(String applicationId, String documentId, String attachmentId, String userData) throws RemoteException;

}
