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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Comparator;

import org.primaresearch.shared.variable.Variable;
import org.primaresearch.web.gwt.client.ui.page.SelectionManager.SelectionListener;
import org.primaresearch.web.gwt.shared.page.ContentObjectC;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Widget;

/**
 * Table view for page content object attributes.
 * 
 * @author Christian Clausner
 *
 */
public class RegionPropertiesView implements SelectionListener {

	private FlexTable table = new FlexTable();
	private PropertiesViewVariableComparator comparator = new PropertiesViewVariableComparator();
	
	public RegionPropertiesView() {
		clear();
	}
		
	public Widget getWidget() {
		return table;
	}

	@Override
	public void selectionChanged(SelectionManager manager) {
		update(manager.getSelection());
	}
	
	public void clear() {
		update(null);
	}
	
	private void update(Set<ContentObjectC> objects) {
		table.removeAllRows();
	
		if (objects == null || objects.size() == 0) {
			//Nothing selected -> Show placeholder
			table.setText(0, 0, "Object Properties");
			return;
		}

		if (objects.size() > 1) {
			table.setText(0, 0, "Multiple objects");
		} 
		else { //Single object
			ContentObjectC obj = objects.iterator().next();
			
			//Header (object type)
			table.setText(0, 0, obj.getType().getName());
			table.getFlexCellFormatter().setColSpan(0, 0, 2);
			table.getFlexCellFormatter().addStyleName(0, 0, "propertiesHeading");
			
			//ID
			table.setText(1, 0, "ID");
			table.setText(1, 1, obj.getId());
			
			if (obj.getAttributes() != null) {
				//Collect attributes
				List<Variable> vars = new ArrayList<Variable>();
				for (int i=0; i<obj.getAttributes().getSize(); i++) {
					vars.add(obj.getAttributes().get(i));
				}

				//Sort
				Collections.sort(vars, comparator);
				
				//Add to table
				for (int i=0; i<vars.size(); i++) {
					addAttributeToTable(vars.get(i), i+2);
				}
			}
		}
	}
	
	private void addAttributeToTable(Variable attr, int row) {
		table.setText(row, 0, attr.getName());
		String text = "";
		if (attr.getValue() != null)
			text = attr.getValue().toString();
		table.setText(row, 1, text);
	}
	
	public static class PropertiesViewVariableComparator implements Comparator<Variable>, Serializable {
		
		private static final long serialVersionUID = 1L;
		private static Map<String,Integer> sortIndexes = new HashMap<String,Integer>();
		
		static {
			sortIndexes.put("type",					10);
			sortIndexes.put("production",			20);
			sortIndexes.put("primaryLanguage",		30);
			sortIndexes.put("secondaryLanguage",	40);
			sortIndexes.put("language",				50);
			sortIndexes.put("primaryScript",		60);
			sortIndexes.put("secondaryScript",		70);
			sortIndexes.put("readingOrientation",	80);
			sortIndexes.put("readingDirection",		90);
			sortIndexes.put("orientation",			100);

			sortIndexes.put("embText",				110);

			sortIndexes.put("colour",				120);
			sortIndexes.put("colourDepth",			130);
			sortIndexes.put("penColour",			140);
			sortIndexes.put("numColours",			150);
			sortIndexes.put("textColour",			160);
			sortIndexes.put("bgColour",				170);
			
			sortIndexes.put("fontSize",				180);
			sortIndexes.put("fontFamily",			190);
			sortIndexes.put("kerning",				200);
			sortIndexes.put("reverseVideo",			210);
			sortIndexes.put("leading",				220);
			sortIndexes.put("indented",				230);
			sortIndexes.put("align",				240);
			sortIndexes.put("serif",				250);
			sortIndexes.put("monospace",			260);
			sortIndexes.put("bold",					270);
			sortIndexes.put("italic",				280);
			sortIndexes.put("underlined",			290);
			sortIndexes.put("subscript",			300);
			sortIndexes.put("superscript",			310);
			sortIndexes.put("strikethrough",		320);
			sortIndexes.put("smallCaps",			330);
			sortIndexes.put("letterSpaced",			340);

			sortIndexes.put("ligature",				350);
			sortIndexes.put("symbol",				360);

			sortIndexes.put("rows",					370);
			sortIndexes.put("columns",				380);
			sortIndexes.put("lineColour",			390);
			sortIndexes.put("lineSeparators",		400);

			sortIndexes.put("confidence",			410);
			sortIndexes.put("custom",				420);
			sortIndexes.put("comments",				430);
		}
		
		public PropertiesViewVariableComparator() {
			super();
		}

		@Override
		public int compare(Variable v1, Variable v2) {
			if (v1 == null || v2 == null)
				return 0;
			int sortIndex1 = sortIndexes.containsKey(v1.getName()) ? sortIndexes.get(v1.getName()) : 500000;
			if (v1.getValue() == null)
				sortIndex1 += 1000000; //Empty values at the bottom
			int sortIndex2 = sortIndexes.containsKey(v1.getName()) ? sortIndexes.get(v2.getName()) : 500000;
			if (v2.getValue() == null)
				sortIndex2 += 1000000; //Empty values at the bottom
			return sortIndex1 - sortIndex2;
		}

		
	}
}
