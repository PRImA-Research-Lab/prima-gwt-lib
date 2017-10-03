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
package org.primaresearch.web.gwt.client.ui.page.tool;

import org.primaresearch.dla.page.layout.physical.shared.LowLevelTextType;
import org.primaresearch.dla.page.layout.physical.shared.RegionType;
import org.primaresearch.web.gwt.client.page.PageLayoutC;
import org.primaresearch.web.gwt.client.page.PageSyncManager;
import org.primaresearch.web.gwt.client.ui.page.SelectionManager;
import org.primaresearch.web.gwt.shared.page.ContentObjectC;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.HasDirection.Direction;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Tool for deleting a page content object.
 * 
 * @author Christian Clausner
 *
 */
public class DeleteContentObjectTool {

	/**
	 * Static method to run the tool (requests confirmation from the user).
	 * 
	 * @param panel Parent panel
	 * @param showRelativeTo Widget to which the confirmation dialogue is aligned to.
	 * @param layout Page layout
	 * @param object Content object to delete
	 * @param syncManager Synchronisation manager to send the delete request to the server
	 * @param selectionManager Content object selection manager for adding a listener
	 */
	public static void run(Panel panel, UIObject showRelativeTo, PageLayoutC layout, ContentObjectC object,
							PageSyncManager syncManager, SelectionManager selectionManager) {
		if (layout == null || object == null)
			return;
		
		showConfiramtionDialog(panel, showRelativeTo, layout, object, syncManager, selectionManager);
	}
	
	private static void showConfiramtionDialog(Panel parent, UIObject showRelativeTo, final PageLayoutC layout, 
												final ContentObjectC object, 
												final PageSyncManager syncManager,
												final SelectionManager selectionManager) {
		final DialogBox confirmationDialog = new DialogBox();
			
		final VerticalPanel vertPanel = new VerticalPanel();
		confirmationDialog.add(vertPanel);
		
		Label confirmLabel = new Label();
		vertPanel.add(confirmLabel);
		
		final HorizontalPanel horPanel = new HorizontalPanel();
		horPanel.setWidth("100%");
		horPanel.setSpacing(5);
		horPanel.setHorizontalAlignment(HorizontalAlignmentConstant.endOf(Direction.LTR));
		vertPanel.add(horPanel);
		
		Button buttonCancel = new Button("Cancel");
		horPanel.add(buttonCancel);
		buttonCancel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				confirmationDialog.hide();
			}
		});
		
		Button buttonDelete = new Button("Delete");
		horPanel.add(buttonDelete);
		buttonDelete.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				syncManager.deleteContentObject(object);
				layout.remove(object);
				selectionManager.clearSelection();
				confirmationDialog.hide();
			}
		});

		String text = "Delete selected ";
		if (object.getType() instanceof RegionType)
			text += "region";
		else if (LowLevelTextType.TextLine.equals(object.getType()))
			text += "text line";
		else if (LowLevelTextType.Word.equals(object.getType()))
			text += "word";
		else if (LowLevelTextType.Glyph.equals(object.getType()))
			text += "glyph";
		text += "?";
		confirmLabel.setText(text);

		parent.add(confirmationDialog);
		if (showRelativeTo != null)
			confirmationDialog.showRelativeTo(showRelativeTo);
		else
			confirmationDialog.show();
		
	}
}
