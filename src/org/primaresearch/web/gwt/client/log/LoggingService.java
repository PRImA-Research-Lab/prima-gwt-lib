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
package org.primaresearch.web.gwt.client.log;

import org.primaresearch.web.gwt.shared.RemoteException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Service for sending log messages from the client (browser) to the server.
 *
 * @author Christian Clausner
 *
 */
@RemoteServiceRelativePath("loggingService")
public interface LoggingService  extends RemoteService {

	public static final int LEVEL_INFO 		= 1;
	public static final int LEVEL_WARNING 	= 2;
	public static final int LEVEL_ERROR 	= 3;
	
	/**
	 * Sends a log message to the server.
	 * @param level Info, warning or error (use LEVEL_ constants)
	 * @param messageId Message ID (e.g. error code)
	 * @param message Message text
	 * @throws RemoteException
	 */
	public Boolean log(int level, int messageId, String message) throws RemoteException;
}
