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

/**
 * Contains all default permission IDs as static class members.
 * 
 * @author Christian Clausner
 *
 */
public class DefaultPermissionNames implements Serializable{

	private static final long serialVersionUID = 1L;
	
	public static final String Edit = "edit";
	public static final String Download = "download";
	public static final String Save = "save";
	
	public DefaultPermissionNames() {
	}

	/**
	 * Removes all current permissions and gives default demo permissions.
	 */
	public static void giveDemoPermissions(Permissions permissions) {
		permissions.clear();
		permissions.addPermissionEntry(DefaultPermissionNames.Edit, true);
		permissions.addPermissionEntry(DefaultPermissionNames.Download, true);
	}
}
