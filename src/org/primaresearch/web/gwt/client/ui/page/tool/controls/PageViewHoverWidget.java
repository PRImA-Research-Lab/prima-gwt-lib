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

import org.primaresearch.web.gwt.client.ui.page.PageScrollView;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Interface for controls that hover over the page view panel.
 * 
 * @author Christian Clausner
 *
 */
public interface PageViewHoverWidget extends IsWidget{

	public void setPageView(PageScrollView view);
	
	public PageScrollView getPageView();
	
	public void refresh();
	
	/**
	 * Changes the position of the icon control
	 * 
	 * @param x X position in document coordinates
	 * @param y Y position in document coordinates
	 */
	public void setPosistion(int x, int y);
}
