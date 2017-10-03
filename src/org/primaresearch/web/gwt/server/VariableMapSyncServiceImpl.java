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
package org.primaresearch.web.gwt.server;

import java.net.MalformedURLException;
import java.net.URL;

import org.primaresearch.io.xml.variable.XmlVariableFileReader;
import org.primaresearch.shared.variable.VariableMap;
import org.primaresearch.web.gwt.client.variable.VariableMapSyncService;
import org.primaresearch.web.gwt.shared.RemoteException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Loading variables from the server.
 * 
 * @author Christian Clausner
 *
 */
public class VariableMapSyncServiceImpl  extends RemoteServiceServlet implements VariableMapSyncService {

	private static final long serialVersionUID = 1L;

	
	@Override
	public VariableMap loadVariables(String url) throws RemoteException {
		VariableMap variables = null;
		try {
			XmlVariableFileReader reader = new XmlVariableFileReader();
			
			variables = reader.read(new URL(url));
		} catch (MalformedURLException e) {
			e.printStackTrace();
			String errmsg = "Error loading variable XML file: \n";
			errmsg += e.getMessage() + "\n";
			throw new RemoteException(errmsg);
		} catch (Exception e) {
			e.printStackTrace();
			String errmsg = "Error loading variable XML file: \n";
			errmsg += e.getMessage() + "\n";
			throw new RemoteException(errmsg);
		}		
		return variables;
	}

}
