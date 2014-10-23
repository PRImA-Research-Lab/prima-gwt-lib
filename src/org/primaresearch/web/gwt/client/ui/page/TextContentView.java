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
package org.primaresearch.web.gwt.client.ui.page;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.primaresearch.dla.page.layout.physical.shared.LowLevelTextType;
import org.primaresearch.dla.page.layout.physical.shared.RegionType;
import org.primaresearch.web.gwt.client.ui.keyboard.VirtualKeyboard.VirtualKeyPressListener;
import org.primaresearch.web.gwt.client.ui.page.SelectionManager.SelectionListener;
import org.primaresearch.web.gwt.shared.page.ContentObjectC;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

/**
 * Text box view for page content object text content.
 * 
 * CSS style class 'TextContentView'.
 * 
 * @author Christian Clausner
 *
 */
public class TextContentView implements SelectionListener, VirtualKeyPressListener  {

	private TextArea textField = new TextArea();
	
	private String textColor;
	
	private Set<TextContentViewChangeListener> listeners = new HashSet<TextContentViewChangeListener>();

	/**
	 * Constructor for read-only text content view.
	 * @param readOnly
	 */
	public TextContentView() {
		this(true);
	}

	/**
	 * Constructor
	 * @param readOnly Set to <code>true</code> to forbid editing the text.
	 */
	public TextContentView(boolean readOnly) {
		textField.addStyleName("TextContentView");
		textField.setReadOnly(readOnly);
		textField.getElement().getStyle().setProperty("fontFamily", "aletheiaSans,sans-serif");
		textColor = textField.getElement().getStyle().getColor();
		
		if (!readOnly) {
			textField.addKeyUpHandler(new KeyUpHandler() {
				@Override
				public void onKeyUp(KeyUpEvent event) {
					if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					} else if (event.getNativeKeyCode() == KeyCodes.KEY_TAB) {
					} else {
						notifyChangeListenersTextChanged();
					}
				}
			});
		}
		
		clear();
	}
	
	public Widget getWidget() {
		return textField;
	}

	@Override
	public void selectionChanged(SelectionManager manager) {
		
		notifyChangeListenersPreSelectionHandling(manager);
		
		if (manager.getSelection() != null && manager.getSelection().size() == 1) {
			update(manager.getSelection().iterator().next());
		}
		else
			update(null);
	}

	public void clear() {
		update(null);
	}
	
	private void update(ContentObjectC object) {
		if (object == null) {
			textField.getElement().getStyle().setColor("gray");
			textField.setText("Text Content");
			textField.setReadOnly(true);
			return;
		}

		textField.setText("");
		textField.getElement().getStyle().setColor(textColor);
		
		if (object.getType() instanceof LowLevelTextType || RegionType.TextRegion.equals(object.getType())) {
			textField.setReadOnly(object.isReadOnly());
			String text = object.getText();
			if (text != null)
				textField.setText(text);
		} else {
			textField.setReadOnly(true);
		}
	}
	
	public void addChangeListener(TextContentViewChangeListener listener) {
		listeners.add(listener);
	}
	
	public void removeChangeListener(TextContentViewChangeListener listener) {
		listeners.remove(listener);
	}
	
	private void notifyChangeListenersTextChanged() {
		for (Iterator<TextContentViewChangeListener> it = listeners.iterator(); it.hasNext(); ) {
			it.next().textChanged();
		}
	}
	
	private void notifyChangeListenersPreSelectionHandling(SelectionManager manager) {
		for (Iterator<TextContentViewChangeListener> it = listeners.iterator(); it.hasNext(); ) {
			it.next().preSelectionHandlingOfTextContentView(manager);
		}
	}
	
	public String getText() {
		return textField.getText();
	}
	
	
	/**
	 * Listener for text change events (by user).
	 * @author Christian Clausner
	 *
	 */
	public static interface TextContentViewChangeListener {
		/** Called when the text of the text content view has changed */
		void textChanged();
		/** Called before a selection change is handled by the text content view */
		void preSelectionHandlingOfTextContentView(SelectionManager manager);
	}


	@Override
	public void virtualKeyPressed(String character) {
		if (textField.isReadOnly())
			return;
		
		String text = textField.getText();
		
		int selStart = textField.getCursorPos();
		int selEnd = selStart + textField.getSelectionLength();
		
		String left = text.substring(0, selStart);
		String right = text.substring(selEnd);
		
		textField.setText(left + character + right);
		textField.setCursorPos(left.length()+1);
		textField.setFocus(true);
	}
}
