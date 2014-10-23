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
package org.primaresearch.web.gwt.client.ui.keyboard;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.primaresearch.shared.variable.StringVariable;
import org.primaresearch.shared.variable.VariableMap;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Virtual keyboard widget<br>
 * <br>
 * Following CSS style names apply:<br>
 * <ul>
 *  <li>Main panel: 'virtualKeyboard'</li>
 *  <li>Layout selection box: 'virtualKeyboardSelector'</li>
 *  <li>Key panel: 'virtualKeyPanel'</li>
 *  <li>Key: 'virtualKey'</li>
 * </ul>
 * 
 * 
 * @author Christian Clausner
 *
 */
public class VirtualKeyboard implements IsWidget {

	private DockLayoutPanel mainPanel;
	private ListBox listBox = null;
	private FlowPanel keyPanel;
	private VariableMap activeLayout = null;
		
	private List<VariableMap> layouts = new ArrayList<VariableMap>();
	
	private Set<VirtualKeyPressListener> listeners = new HashSet<VirtualKeyPressListener>();
	
	/**
	 * Constructor
	 * @param addKeyboardSelector Set to <code>true</code> to add a drop-down selection box for keyboard layouts
	 */
	public VirtualKeyboard(boolean addKeyboardSelector) {
		mainPanel = new DockLayoutPanel(Style.Unit.PX);
		mainPanel.addStyleName("virtualKeyboard");
		
		if (addKeyboardSelector) {
			//Layout selection drop-down box
			listBox = new ListBox();
			listBox.setVisibleItemCount(1);
			listBox.addStyleName("virtualKeyboardSelector");
			mainPanel.addNorth(listBox, 30);
			listBox.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					try {
						int index = listBox.getSelectedIndex();
						activeLayout = layouts.get(index);
						refreshKeyPanel();
					} catch(Exception exc) {
						exc.printStackTrace();
					}
				}
			});
		}
		
		//Key panel
		keyPanel = new FlowPanel();
		keyPanel.addStyleName("virtualKeyPanel");
		mainPanel.add(keyPanel);
	}
	
	@Override
	public Widget asWidget() {
		return mainPanel;
	}
	
	public void addListener(VirtualKeyPressListener listener) {
		listeners.add(listener);
	}

	public void removeListener(VirtualKeyPressListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Add a keyboard layout
	 * @param layout List of variables defining the keys
	 */
	public void addLayout(VariableMap layout) {
		layouts.add(layout);
		String name = layout.getName() != null ? layout.getName() : "Default Layout";
		if (listBox != null)
			listBox.addItem(name);
		if (activeLayout == null)
			activateLayout(layout);
	}
	
	private void activateLayout(VariableMap layout) {
		activeLayout = layout;

		String name = layout.getName() != null ? layout.getName() : "Default Layout";
		if (listBox != null) {
			for (int i=0; i<listBox.getItemCount(); i++) {
				if (listBox.getItemText(i).equals(name)) {
					listBox.setSelectedIndex(i);
					break;
				}
			}
		}
		
		refreshKeyPanel();
	}
	
	private void refreshKeyPanel() {
		keyPanel.clear();
		
		if (activeLayout == null)
			return;
		
		//Add keys
		for (int i=0; i<activeLayout.getSize(); i++) {
			StringVariable var = (StringVariable)activeLayout.get(i);
			final Button key = new Button();
			key.getElement().getStyle().setProperty("fontFamily", "aletheiaSans,sans-serif");
			key.addStyleName("virtualKey");
			
			//Convert hex string to Unicode
			int hexInt = Integer.parseInt(var.getValue().toString(), 16);
			String stringRepresentation = new String(Character.toChars(hexInt));
			key.setText(stringRepresentation);
			
			key.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					notifyListeners(key.getText());
				}
			});
			
			key.setTitle(var.getDescription());
			
			keyPanel.add(key);
		}
	}
	
	public int getLayoutCount() {
		return layouts.size();
	}
	
	private void notifyListeners(String character) {
		for (Iterator<VirtualKeyPressListener> it = listeners.iterator(); it.hasNext(); ) {
			it.next().virtualKeyPressed(character);
		}
	}
	
	/**
	 * Interface for key press listeners for a virtual keyboard
	 * 
	 * @author Christian Clausner
	 *
	 */
	public static interface VirtualKeyPressListener {
		
		public void virtualKeyPressed(String character);
	}

}
