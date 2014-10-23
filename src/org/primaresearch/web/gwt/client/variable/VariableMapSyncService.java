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
package org.primaresearch.web.gwt.client.variable;

import org.primaresearch.shared.variable.VariableMap;
import org.primaresearch.web.gwt.shared.RemoteException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Interface for loading variables from the server.
 * 
 * @author Christian Clausner
 *
 */
@RemoteServiceRelativePath("variableMapSync")
public interface VariableMapSyncService extends RemoteService {

	/**
	 * Loads variables from an XML file
	 * @param url File resource
	 * @return The variables
	 */
	public VariableMap loadVariables(String url) throws RemoteException;
}
