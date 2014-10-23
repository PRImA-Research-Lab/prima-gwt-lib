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
package org.primaresearch.web.gwt.client.ui.page.tool;

import java.util.List;

import org.primaresearch.dla.page.layout.physical.shared.ContentType;
import org.primaresearch.dla.page.layout.physical.shared.LowLevelTextType;
import org.primaresearch.dla.page.layout.physical.shared.RegionType;
import org.primaresearch.maths.geometry.Polygon;
import org.primaresearch.maths.geometry.Rect;
import org.primaresearch.shared.variable.StringValue;
import org.primaresearch.shared.variable.StringVariable;
import org.primaresearch.shared.variable.Variable;
import org.primaresearch.shared.variable.VariableMap;
import org.primaresearch.web.gwt.client.page.PageLayoutC;
import org.primaresearch.web.gwt.client.page.PageSyncManager;
import org.primaresearch.web.gwt.client.ui.page.PageScrollView;
import org.primaresearch.web.gwt.client.ui.page.tool.drawing.PageViewTool;
import org.primaresearch.web.gwt.client.ui.page.tool.drawing.PageViewToolListener;
import org.primaresearch.web.gwt.client.ui.page.tool.drawing.RectangleTool;
import org.primaresearch.web.gwt.shared.page.ContentObjectC;

/**
 * Tool to create new page content objects.
 * 
 * @author Christian Clausner
 *
 */
public class CreatePageObjectTool {

	private static int idCounter = 1;
	
	private PageViewTool drawingTool;
	private PageScrollView pageView;
	private PageSyncManager pageLoader;
	private ContentType pageObjectType; 
	private String regionSubType;
	
	/**
	 * Constructor. Creates and activates the tool.
	 */
	public CreatePageObjectTool(DrawingTool drawingTool, PageScrollView pageView, String pageObjectType, PageSyncManager pageLoader) {
		this(drawingTool, pageView, translateContentType(pageObjectType), pageLoader);
	}
	
	/**
	 * Constructor. Creates and activates the tool.
	 */
	public CreatePageObjectTool(DrawingTool drawingTool, PageScrollView pageView, ContentType pageObjectType, PageSyncManager pageLoader) {
		this.pageView = pageView;
		this.pageLoader = pageLoader;
		this.pageObjectType = pageObjectType;
		if (DrawingTool.Rectangle.equals(drawingTool))
			this.drawingTool = new RectangleTool(pageView);
		
		this.drawingTool.addListener(new PageViewToolListener() {
			@Override
			public void onToolFinished(PageViewTool tool, boolean success) {
				toolFinished(tool, success);
			}
		});
		
		pageView.setTool(this.drawingTool);
	}
	
	/**
	 * Sets the sub-type of the newly created region
	 * @param subType
	 */
	public void setRegionSubType(String subType) {
		this.regionSubType = subType;
	}

	//The region has been drawn by the user
	private void toolFinished(PageViewTool tool, boolean success) {
		if (success) {
			//Create polygon
			Polygon polygon = null;
			if (tool instanceof RectangleTool) {
				Rect rect = ((RectangleTool)tool).getRect();
				if (rect != null) {
					polygon = new Polygon();
					polygon.addPoint(rect.left, rect.top);
					polygon.addPoint(rect.right, rect.top);
					polygon.addPoint(rect.right, rect.bottom);
					polygon.addPoint(rect.left, rect.bottom);
				}
			}
			
			//Create object
			if (polygon != null) {
				PageLayoutC layout = pageView.getPageLayout();
				List<ContentObjectC> content = layout.getContent(pageObjectType);
				
				ContentObjectC newObject = new ContentObjectC(polygon, "todo"+idCounter);
				idCounter++;
				
				newObject.setType(pageObjectType);
				if (regionSubType != null && !"".equals(regionSubType)) {
					VariableMap attrs = newObject.getAttributes();
					if (attrs == null) {
						attrs = new VariableMap();
						newObject.setAttributes(attrs);
					}
					Variable attr = attrs.get("type");
					if (attr == null) {
						attr = new StringVariable("type");
						attrs.add(attr);
					}
					try {
						attr.setValue(new StringValue(regionSubType));
					} catch (Exception e) {
						e.printStackTrace();
					} 
				}
				content.add(newObject);
				pageView.getRenderer().refresh();
				
				//Sync with server and change ID
				pageLoader.addContentObject(newObject);
			}
		}
	}
	
	private static ContentType translateContentType(String typeName) {
		if ("TextLine".equals(typeName))
			return LowLevelTextType.TextLine;
		else if ("Word".equals(typeName))
			return LowLevelTextType.Word;
		else if ("Glyph".equals(typeName))
			return LowLevelTextType.Glyph;
		else //if ("Region".equals(pageObjectType))
			return RegionType.TextRegion;
	}
	
	/**
	 * Drawing tool types for creating a region
	 */
	public static class DrawingTool {
		public static final DrawingTool Rectangle = new DrawingTool("Rectangle"); 
		
		private String id;
		
		private DrawingTool(String id) {
			this.id = id;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof DrawingTool)
				return id.equals(((DrawingTool)obj).id);
			return false;
		}
	}
}
