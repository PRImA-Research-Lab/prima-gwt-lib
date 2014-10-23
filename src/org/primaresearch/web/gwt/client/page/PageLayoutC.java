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
package org.primaresearch.web.gwt.client.page;

import java.util.List;

import org.primaresearch.dla.page.layout.physical.shared.ContentType;
import org.primaresearch.dla.page.layout.physical.shared.LowLevelTextType;
import org.primaresearch.dla.page.layout.physical.shared.RegionType;
import org.primaresearch.web.gwt.shared.page.ContentObjectC;

/**
 * Lightweight page layout class for use on client side (browser; 'C' for client).
 * 
 * @author Christian Clausner
 *
 */
public class PageLayoutC {
	
	//Constants for content types
	public static final String TYPE_Regions 	= "Region";
	public static final String TYPE_TextLines 	= "TextLine";
	public static final String TYPE_Words 		= "Word";
	public static final String TYPE_Glyphs 		= "Glyph";
	public static final String TYPE_Border 		= "Border";
	public static final String TYPE_Printspace 	= "PrintSpace";

	private int width = 1000;
	private int height = 1000;
	
	private String id;
	
	private List<ContentObjectC> regions = null;
	private List<ContentObjectC> lines = null;
	private List<ContentObjectC> words = null;
	private List<ContentObjectC> glyphs = null;
	private List<ContentObjectC> border = null;
	private List<ContentObjectC> printSpace = null;
	
	//private MetaData metaData = null;
	
	/**
	 * Empty constructor (required for GWT)
	 */
	public PageLayoutC() {
	}

	/**
	 * Removes all page content objects (regions, lines, words, glyphs) and resets ID as well as dimensions.
	 */
	public void clear() {
		clear(false);
	}
	
	/**
	 * Removes all page content objects (regions, lines, words, glyphs) and resets ID as well as dimensions (optional).
	 */
	public void clear(boolean contentObjectsOnly) {
		regions = null;
		lines = null;
		words = null;
		glyphs = null;
		border = null;
		printSpace = null;
		if (!contentObjectsOnly) {
			id = null;
			width = 1000;
			height = 1000;
		}
	}
	
	/**
	 * Returns the document page width.
	 */
	public int getWidth() {
		return width;
	}
	
	/**
	 * Returns the document page height.
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Sets the document page width
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * Sets the document page height
	 */
	public void setHeight(int height) {
		this.height = height;
	}
	
	/**
	 * Returns all page content regions
	 */
	public List<ContentObjectC> getRegions() {
		return regions; 
	}
	
	/**
	 * Sets the page content region objects
	 */
	public void setRegions(List<ContentObjectC> regions) {
		this.regions = regions;
	}

	/**
	 * Returns all page content text lines
	 */
	public void setTextLines(List<ContentObjectC> lines) {
		this.lines = lines;
	}

	/**
	 * Sets the page content word objects
	 */
	public void setWords(List<ContentObjectC> words) {
		this.words = words;
	}

	/**
	 * Sets the page content glyph objects
	 */
	public void setGlyphs(List<ContentObjectC> glyphs) {
		this.glyphs = glyphs;
	}

	/**
	 * Sets the page border
	 * @param list List with border object at position 0 (or empty list)
	 */
	public void setBorder(List<ContentObjectC> list) {
		this.border = list;
	}
	
	/**
	 * Sets the page print space
	 * @param list List with print space object at position 0 (or empty list)
	 */
	public void setPrintSpace(List<ContentObjectC> list) {
		this.printSpace = list;
	}

	/**
	 * Sets the content object list for the specified content type.
	 */
	public void setContent(String contentType, List<ContentObjectC> contentObjects) {
		if (TYPE_Regions.equals(contentType))
			setRegions(contentObjects);
		else if (TYPE_TextLines.equals(contentType))
			setTextLines(contentObjects);
		else if (TYPE_Words.equals(contentType))
			setWords(contentObjects);
		else if (TYPE_Glyphs.equals(contentType))
			setGlyphs(contentObjects);
		else if (TYPE_Border.equals(contentType))
			setBorder(contentObjects);
		else if (TYPE_Printspace.equals(contentType))
			setPrintSpace(contentObjects);
	}

	/**
	 * Returns the content object list for the specified content type.
	 * @return A list of content objects or <code>null</code>.
	 */
	public List<ContentObjectC> getContent(String contentType) {
		if (TYPE_Regions.equals(contentType))
			return regions;
		else if (TYPE_TextLines.equals(contentType))
			return lines;
		else if (TYPE_Words.equals(contentType))
			return words;
		else if (TYPE_Glyphs.equals(contentType))
			return glyphs;
		else if (TYPE_Border.equals(contentType))
			return border;
		else if (TYPE_Printspace.equals(contentType))
			return printSpace;
		return null;
	}
	
	/**
	 * Returns the content object list for the specified content type.
	 * @return A list of content objects or <code>null</code>.
	 */
	public List<ContentObjectC> getContent(ContentType contentType) {
		if (contentType instanceof RegionType)
			return regions;
		else if (LowLevelTextType.TextLine.equals(contentType))
			return lines;
		else if (LowLevelTextType.Word.equals(contentType))
			return words;
		else if (LowLevelTextType.Glyph.equals(contentType))
			return glyphs;
		else if (ContentType.Border.equals(contentType))
			return border;
		else if (ContentType.PrintSpace.equals(contentType))
			return printSpace;
		return null;
	}
	
	/**
	 * Tries to find a content object at a given position.
	 * @param contentType Type of the content object
	 * @return A content object or <code>null</code>
	 */
	public ContentObjectC getObjectAt(int x, int y, String contentType) {
		List<ContentObjectC> objects = getContent(contentType);
		if (objects != null) {
			for (int i=0; i<objects.size(); i++) {
				if (objects.get(i).getCoords().isPointInside(x, y))
					return objects.get(i);
			}
		}
		return null;
	}

	/**
	 * Returns the page ID (ground truth and storage ID).
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the page ID (ground truth and storage ID).
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * Tries to find a content object (of any type) with the given ID.  
	 * @return A content object or <code>null</code>
	 */
	public ContentObjectC findContentObject(String id) {
		ContentObjectC obj = findContentObject(id, "Region");
		if (obj != null)
			return obj;

		obj = findContentObject(id, "TextLine");
		if (obj != null)
			return obj;

		obj = findContentObject(id, "Word");
		if (obj != null)
			return obj;

		obj = findContentObject(id, "Glyph");
		if (obj != null)
			return obj;
		return null;
	}
	
	
	/**
	 * Tries to find a content object of given type and ID.  
	 * @return A content object or <code>null</code>
	 */
	private ContentObjectC findContentObject(String id, String contentType) {
		List<ContentObjectC> content = getContent(contentType);
		if (content != null) {
			for (int i=0; i<content.size(); i++)
				if (content.get(i).getId().equals(id))
					return content.get(i);
		}
		return null;
	}
	
	/**
	 * Removes the given content object from this page layout.
	 */
	public void remove(ContentObjectC object) {
		if (object == null)
			return;
		List<ContentObjectC> content = getContent(object.getType());
		if (content != null) {
			content.remove(object);
		}
	}
	
	/**
	 * Returns the page content object that is the immediate predecessor of the given object in the internal list.
	 * If the given object is at the beginning of the list the last object is returned as predecessor.
	 * @return The predecessor or null
	 */
	public ContentObjectC getPreviousObject(ContentObjectC object) {
		if (object == null)
			return null;
		List<ContentObjectC> objects = getContent(object.getType());
		int index = objects.indexOf(object);
		if (index < 0)
			return null;
		if (index > 0)
			return objects.get(index-1);
		return objects.get(objects.size()-1); //Jump to last object
	}

	/**
	 * Returns the page content object that is the immediate successor of the given object in the internal list.
	 * If the given object is at the end of the list the first object is returned as successor.
	 * @return The successor or null
	 */
	public ContentObjectC getNextObject(ContentObjectC object) {
		if (object == null)
			return null;
		List<ContentObjectC> objects = getContent(object.getType());
		int index = objects.indexOf(object);
		if (index < 0)
			return null;
		if (index < objects.size()-1)
			return objects.get(index+1);
		return objects.get(0); //Jump to first object
	}

	//public MetaData getMetaData() {
	//	return metaData;
	//}

	//public void setMetaData(MetaData metaData) {
	//	this.metaData = metaData;
	//}


	
}
