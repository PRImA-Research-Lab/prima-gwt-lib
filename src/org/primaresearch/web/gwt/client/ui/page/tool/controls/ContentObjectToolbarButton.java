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
package org.primaresearch.web.gwt.client.ui.page.tool.controls;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;

/**
 * Icon widget that is part of the toolbar of a selected content object in the page view. 
 * 
 * @author Christian Clausner
 *
 */
public class ContentObjectToolbarButton implements HasClickHandlers, MouseDownHandler, MouseUpHandler, IsWidget {

	private Widget button; //Push button or toggle button 

	/**
	 * Constructor for a standard push button.
	 * 
	 * @param imageUrl Icon resource
	 * @param tooltip Tooltip text
	 */
	public ContentObjectToolbarButton(String imageUrl, String tooltip) {
		this(imageUrl, tooltip, false);
	}
	
	/**
	 * Constructor with selection for push or toggle button.
	 * 
	 * @param imageUrl Icon resource
	 * @param tooltip Tooltip text
	 * @param toggleButton If <code>true</code> a toggle button is created, otherwise a push button.
	 */
	public ContentObjectToolbarButton(String imageUrl, String tooltip, boolean toggleButton) {
		if (toggleButton) {
			button = new ToggleButton(new Image(imageUrl));
			((ToggleButton)button).addMouseDownHandler(this);
			((ToggleButton)button).addMouseUpHandler(this);
		}
		else { //Push button
			button = new PushButton(new Image(imageUrl));
			((PushButton)button).addMouseDownHandler(this);
			((PushButton)button).addMouseUpHandler(this);
		}
		button.getElement().setTitle(tooltip);
	}
	
	public void setEnabled(boolean enable) {
		if (button instanceof PushButton)
			((PushButton)button).setEnabled(enable);
		else if (button instanceof ToggleButton)
			((ToggleButton)button).setEnabled(enable);
	}
	
	/**
	 * Sets the state of the toggle button.<br>
	 * If the control is a push button, this method has no effect.
	 * @param down Use <code>true</code> for active/down and <code>false</code> for inactive/up
	 */
	public void setDown(boolean down) {
		if (button instanceof ToggleButton)
			((ToggleButton)button).setDown(down);
	}

	/**
	 * Returns the state of the toggle button.<br>
	 * If the control is a push button, <code>false</code> is returned.
	 * return <code>true</code> for active/down or <code>false</code> for inactive/up
	 */
	public boolean isDown() {
		if (button instanceof ToggleButton)
			((ToggleButton)button).isDown();
		return false;
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		button.fireEvent(event);
	}

	@Override
	public HandlerRegistration addClickHandler(ClickHandler handler) {
		return ((HasClickHandlers)button).addClickHandler(handler);
	}

	@Override
	public void onMouseUp(MouseUpEvent event) {
		event.stopPropagation();
	}

	@Override
	public void onMouseDown(MouseDownEvent event) {
		event.stopPropagation();
	}

	@Override
	public Widget asWidget() {
		return button;
	}
	

}
