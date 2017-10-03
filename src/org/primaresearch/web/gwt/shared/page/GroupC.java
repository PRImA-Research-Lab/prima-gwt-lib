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
package org.primaresearch.web.gwt.shared.page;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * Group for reading order and layers. For use on client side ('C' for client)
 * 
 * @author Christian Clausner
 *
 */
public class GroupC implements Serializable, GroupMemberC {

	private static final long serialVersionUID = 1L;
	public String id = null;
	public String caption = null;
	public boolean ordered = true;
	public List<GroupMemberC> members = null;
	
	public GroupC() {
	}

	/**
	 * Finds and removes the region reference member with the given region ID (recursive).  
	 * @param id ID of referenced region
	 */
	public void removeRegionRef(String id) {
		if (members == null)
			return;
		
		for (Iterator<GroupMemberC> it = members.iterator(); it.hasNext(); ) {
			GroupMemberC member = it.next();
			if (member instanceof GroupC)
				((GroupC)member).removeRegionRef(id);
			else if (id.equals(((RegionRefC)member).regionId)) {
				members.remove(member);
				break;
			}
		}
	}
	
}
