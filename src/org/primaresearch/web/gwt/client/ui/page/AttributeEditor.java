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
package org.primaresearch.web.gwt.client.ui.page;

import java.util.Iterator;

import org.primaresearch.shared.variable.BooleanValue;
import org.primaresearch.shared.variable.BooleanVariable;
import org.primaresearch.shared.variable.DoubleValue;
import org.primaresearch.shared.variable.DoubleVariable;
import org.primaresearch.shared.variable.IntegerValue;
import org.primaresearch.shared.variable.IntegerVariable;
import org.primaresearch.shared.variable.StringValue;
import org.primaresearch.shared.variable.StringVariable;
import org.primaresearch.shared.variable.Variable;
import org.primaresearch.shared.variable.VariableValue;
import org.primaresearch.shared.variable.constraints.ValidStringValues;

import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Editor for a single page content object attribute
 * 
 * @author Christian Clausner
 *
 */
public class AttributeEditor {

	private VerticalPanel mainPanel = new VerticalPanel();
	private Variable attribute = null;
	private TextArea textField = null;
	//private TextBox textBox = null;
	private ListBox listBox = null;
	private IntegerBox integerBox = null;
	private DoubleBox doubleBox = null;
	private KeyPressHandler keyPressHandler = null;
	
	public AttributeEditor() {
	}
	
	public Widget getWidget() {
		return mainPanel;
	}

	public void setKeyPressHandler(KeyPressHandler keyPressHandler) {
		this.keyPressHandler = keyPressHandler;
	}

	public void update(Variable attribute) {
		this.attribute = attribute;
		textField = null;
		//textBox = null;
		listBox = null;
		integerBox = null;
		doubleBox = null;
		mainPanel.clear();
		
		if (attribute == null)
			return;
		
		try {
			if (attribute instanceof StringVariable)
				createStringValueControl();
			else if (attribute instanceof BooleanVariable)
				createBooleanValueControl();
			else if (attribute instanceof IntegerVariable)
				createIntegerValueControl();
			else if (attribute instanceof DoubleVariable)
				createDoubleValueControl();
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		
		if (keyPressHandler != null) {
			if (textField != null)
				textField.addKeyPressHandler(keyPressHandler);
			//else if (textBox != null)
			//	textBox.addKeyPressHandler(keyPressHandler);
			else if (listBox != null)
				listBox.addKeyPressHandler(keyPressHandler);
			else if (integerBox != null)
				integerBox.addKeyPressHandler(keyPressHandler);
			else if (doubleBox != null)
				doubleBox.addKeyPressHandler(keyPressHandler);
		}
	}
	
	private void createStringValueControl() {
		StringValue value = (StringValue)attribute.getValue();
		
		//No constraint? - Text field 
		if (attribute.getConstraint() == null) {
			textField = new TextArea();
			textField.setWidth("90%");
			mainPanel.add(textField);
			

			if (value != null)
				textField.setText(value.val);
		}
		//List of valid values? - List control
		else if (attribute.getConstraint() instanceof ValidStringValues) {
			listBox = new ListBox(false);
			listBox.setWidth("90%");
			ValidStringValues validValues = (ValidStringValues)attribute.getConstraint();
			int selectedIndex = 0;
			listBox.addItem("[not set]");
			if (validValues.getValidValues() != null) {
				int i=1;
				for (Iterator<String> it = validValues.getValidValues().iterator(); it.hasNext(); ) {
					String val = it.next();
					if (val != null) {
						listBox.addItem(val);
						if (value != null && value.val != null && val.equals(value.val))
							selectedIndex = i;
					}
					i++;
				}
			}
			listBox.setVisibleItemCount(5);
			mainPanel.add(listBox);
			listBox.setSelectedIndex(selectedIndex);
		}
	}
	
	private void createIntegerValueControl() {
		IntegerValue value = (IntegerValue)attribute.getValue();

		integerBox = new IntegerBox();
		integerBox.setWidth("90%");
		mainPanel.add(integerBox);

		if (value != null)
			integerBox.setValue(value.val);
		
		integerBox.addKeyPressHandler(new KeyPressHandler(){  
			   public void onKeyPress(KeyPressEvent event) {  
			      char keyCode = event.getCharCode();  
			      if(!(keyCode >= '0' && keyCode <= '9') && keyCode != '-' ) {                        
			    	  integerBox.cancelKey();  
			      }    
			   }  
			});   
	}
	
	private void createDoubleValueControl() {
		DoubleValue value = (DoubleValue)attribute.getValue();
		
		doubleBox = new DoubleBox();
		doubleBox.setWidth("90%");
		mainPanel.add(doubleBox); 

		if (value != null)
			doubleBox.setValue(value.val);
		
		doubleBox.addKeyPressHandler(new KeyPressHandler(){  
			   public void onKeyPress(KeyPressEvent event) {  
			      char keyCode = event.getCharCode();  
			      if(!(keyCode >= '0' && keyCode <= '9') && keyCode != '-' && keyCode != '.' ) {                        
			    	  doubleBox.cancelKey();  
			      }    
			   }  
			});   
	}
	
	public void focus() {
		if (textField != null)
			textField.setFocus(true);
		else if (doubleBox != null)
			doubleBox.setFocus(true);
		else if (listBox != null)
			listBox.setFocus(true);
		else if (integerBox != null)
			integerBox.setFocus(true);
	}
	
	private void createBooleanValueControl() {
		BooleanValue value = (BooleanValue)attribute.getValue();
		
		listBox = new ListBox(false);
		listBox.setWidth("90%");
		listBox.addItem("[not set]");
		listBox.addItem("True");
		listBox.addItem("False");
		if (value == null)
			listBox.setSelectedIndex(0);
		else if (value.val)
			listBox.setSelectedIndex(1);
		else
			listBox.setSelectedIndex(2);
		listBox.setVisibleItemCount(3);
		mainPanel.add(listBox);
	}
	
	public VariableValue getNewValue() {
		if (attribute == null)
			return null;
		
		if (attribute instanceof StringVariable)
			return getStringValue();
		else if (attribute instanceof BooleanVariable)
			return getBooleanValue();
		else if (attribute instanceof IntegerVariable)
			return getIntegerValue();
		else if (attribute instanceof DoubleVariable)
			return getDoubleValue();
		return null;
	}
	
	public Variable getAttribute() {
		return attribute;
	}

	private VariableValue getStringValue() {
		//No constraint? - Text field 
		if (attribute.getConstraint() == null) {
			String text = textField.getText();
			if (text.isEmpty())
				return null;
			return new StringValue(text);
		}		
		//List of valid values? - List control
		else if (attribute.getConstraint() instanceof ValidStringValues) {
			if (listBox.getSelectedIndex() > 0) {
				String val = listBox.getItemText(listBox.getSelectedIndex());
				return new StringValue(val);
			} 
		}
		return null;
	}

	private VariableValue getBooleanValue() {
		if (listBox.getSelectedIndex() == 1) 
			return new BooleanValue(true);
		else if (listBox.getSelectedIndex() == 2) 
			return new BooleanValue(false);
		return null;
	}
	
	private VariableValue getIntegerValue() {
		if (integerBox.getText().isEmpty())
			return null;
		return new IntegerValue(integerBox.getValue());
	}
	
	private VariableValue getDoubleValue() {
		if (doubleBox.getText().isEmpty())
			return null;
		try {
			return new DoubleValue(doubleBox.getValue());
		} catch (NumberFormatException exc) {
			return null;
		}
	}


	
	
}
