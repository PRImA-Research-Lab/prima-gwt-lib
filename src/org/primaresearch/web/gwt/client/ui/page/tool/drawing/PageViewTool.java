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
package org.primaresearch.web.gwt.client.ui.page.tool.drawing;

import org.primaresearch.web.gwt.client.ui.page.renderer.RendererPlugin;

import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;

/**
 * A tool for interacting with the document page view.
 * 
 * @author Christian Clausner
 *
 */
public interface PageViewTool extends RendererPlugin {

	void addListener(PageViewToolListener listener);
	
	void removeListener(PageViewToolListener listener);
	
	void cancel();
	
	/**
	 * Mouse has been moved.
	 * @param event The original mouse event
	 * @return <code>true</code> if the event has been handled and the parent should not do any further handling.
	 */
	public boolean onMouseMove(MouseMoveEvent event);
	
	/**
	 * Mouse has left the view.
	 * @param event The original mouse event
	 * @return <code>true</code> if the event has been handled and the parent should not do any further handling.
	 */
	public boolean onMouseOut(MouseOutEvent event);
	
	/**
	 * Mouse button has been released.
	 * @param event The original mouse event
	 * @return <code>true</code> if the event has been handled and the parent should not do any further handling.
	 */
	public boolean onMouseUp(MouseUpEvent event);

	/**
	 * Mouse button has been pressed.
	 * @param event The original mouse event
	 * @return <code>true</code> if the event has been handled and the parent should not do any further handling.
	 */
	public boolean onMouseDown(MouseDownEvent event);
	
}
