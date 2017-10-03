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
package org.primaresearch.web.gwt.client.ui;

import com.google.gwt.user.client.ui.Image;

/**
 * Interface for classes that provide access to document images.
 * 
 * @author Christian Clausner
 *
 */
public interface DocumentImageSource {

	/**
	 * Returns the image that fits best to the given zoom factor. 
	 */
	public Image getImage(double zoomFactor);
	
	public int getOriginalImageWidth();
	public int getOriginalImageHeight();
	
	public void addListener(DocumentImageListener listener);
	
	public void removeListener(DocumentImageListener listener);
}
