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
package org.primaresearch.web.gwt.server;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.primaresearch.web.gwt.client.log.LoggingService;
import org.primaresearch.web.gwt.shared.RemoteException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Service for sending log messages from the client (browser) to the server.
 *
 * @author Christian Clausner
 *
 */
public class LoggingServiceImpl extends RemoteServiceServlet implements LoggingService {

	private static final long serialVersionUID = 1L;
	Logger logger = null;
	
	public LoggingServiceImpl() {
		try {
			logger = Logger.getGlobal();
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	@Override
	public Boolean log(int level, int messageId, String message) throws RemoteException {
		
		doLog(level, messageId, message);
		
		return true;
	}
	
	/**
	 * Calls the Java logger
	 */
	private void doLog(int level, int messageId, String message) {
		try {
			String text = "" + messageId + " - " + message;
			if (logger != null) {
				if (level == LoggingService.LEVEL_INFO)
					logger.info(text);
				else if (level == LoggingService.LEVEL_WARNING)
					logger.warning(text);
				else //if (level == LoggingService.LEVEL_ERROR)
					logger.log(Level.SEVERE, text);
			} else {
				System.out.println(text);
			}
		} catch (Exception exc) {
		}
	}
}
