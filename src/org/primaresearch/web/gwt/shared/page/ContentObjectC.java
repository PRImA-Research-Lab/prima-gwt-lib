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
package org.primaresearch.web.gwt.shared.page;

import java.io.Serializable;

import org.primaresearch.dla.page.layout.physical.shared.ContentType;
import org.primaresearch.dla.page.layout.shared.GeometricObject;
import org.primaresearch.maths.geometry.Polygon;
import org.primaresearch.shared.variable.VariableMap;

/**
 * Lightweight content object class for use on client side (browser; 'C' for client).
 * 
 * @author Christian Clausner
 *
 */
public class ContentObjectC implements Identifiable, GeometricObject, Serializable {

	private static final long serialVersionUID = 1L;
	
	private Polygon coords;
	private String id;
	private ContentType type;
	private VariableMap attributes;
	private String text;
	private boolean readOnly = false;
	
	/**
	 * Empty constructor (required for GWT)
	 */
	public ContentObjectC() {
		coords = null;
		id = null;
	}
	
	/**
	 * Constructor
	 * @param coords Object outline
	 * @param id Object ID
	 */
	public ContentObjectC(Polygon coords, String id) {
		this.coords = coords;
		this.id = id;
	}

	@Override
	public Polygon getCoords() {
		return coords;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setCoords(Polygon coords) {
		this.coords = coords;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ContentType getType() {
		return type;
	}

	public void setType(ContentType type) {
		this.type = type;
	}

	public VariableMap getAttributes() {
		return attributes;
	}

	public void setAttributes(VariableMap attributes) {
		this.attributes = attributes;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	
	
}
