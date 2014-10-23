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
package org.primaresearch.web.gwt.shared.user;

import java.io.Serializable;

/**
 * Data type for exchanging session data between server and client.<br/>
 * <br/>
 * Contains:<br/>
 * <ul>
 * <li>Document page image source</li>
 * <li>User permissions</li>
 * </ul> 
 * @author Christian Clausner
 *
 */
public class SessionData implements Serializable{

	private static final long serialVersionUID = 1L;
	
	/** User permissions */
	public Permissions permissions;
	/** URL for retrieving the current document page image */
	public String getDocumentImageUrl;
	
	public SessionData() {
		permissions = null;
		getDocumentImageUrl = null;
	}
}
