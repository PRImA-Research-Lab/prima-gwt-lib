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
package org.primaresearch.web.gwt.shared.user;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Permissions (for current combination of user/document/attachment).
 * 
 * @author Christian Clausner
 *
 */
public class Permissions implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Map<String,Boolean> permissions = new HashMap<String, Boolean>();

	public Permissions() {
	}
	
	/**
	 * Removes all permissions
	 */
	public void clear() {
		permissions.clear();
	}
	
	/**
	 * Adds a single permission entry (permission name and 'granted' flag)
	 * @param name ID of the permission
	 * @param granted <code>true</code> to permit, <code>false</code> to forbid 
	 */
	public void addPermissionEntry(String name, boolean granted) {
		permissions.put(name, granted);
	}
	
	/**
	 * Checks if an action is permitted
	 * @param name Permission ID
	 * @return <code>true</code> if permitted, <code>false</code> if forbidden 
	 */
	public boolean isPermitted(String name) {
		//Is the permission specified directly?
		if (permissions.containsKey(name)) {
			return permissions.get(name);
		}
		
		//Otherwise check parent permission ('.' is separator)
		int pos = name.lastIndexOf('.');
		if (pos > 0) {
			String parent = name.substring(0, pos-1);
			return isPermitted(parent);
		}
		return false;
	}
	
}
