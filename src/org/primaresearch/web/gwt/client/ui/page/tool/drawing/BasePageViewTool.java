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
package org.primaresearch.web.gwt.client.ui.page.tool.drawing;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Base class for tools that can be used with the page view class.
 * 
 * @author Christian Clausner
 *
 */
public abstract class BasePageViewTool implements PageViewTool {

	private Set<PageViewToolListener> listeners = new HashSet<PageViewToolListener>();

	private boolean enabled = true;
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void enable(boolean enable) {
		this.enabled = enable;
	}

	@Override
	public void addListener(PageViewToolListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(PageViewToolListener listener) {
		listeners.remove(listener);
	}

	protected void notifyListenersToolFinished(boolean success) {
		for (Iterator<PageViewToolListener> it = listeners.iterator(); it.hasNext(); ){
			PageViewToolListener l = it.next();
			l.onToolFinished(this, success);
		}
	}
	
	@Override
	public void cancel() {
		notifyListenersToolFinished(false);
	}

}
