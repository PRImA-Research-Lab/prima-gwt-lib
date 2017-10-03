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

/**
 * Listener interface for tools that can be plugged in to the document page view.
 * 
 * @author Christian Clausner
 *
 */
public interface PageViewToolListener {

	/**
	 * Notifies the listener that a tool has finished.
	 * @param success <code>true</code> if the tool has finished successful or <code>false</code> if it has been cancelled. 
	 */
	public void onToolFinished(PageViewTool tool, boolean success);
}
