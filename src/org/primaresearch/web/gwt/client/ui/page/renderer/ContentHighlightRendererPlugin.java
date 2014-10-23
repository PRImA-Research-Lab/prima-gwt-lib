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
package org.primaresearch.web.gwt.client.ui.page.renderer;

import java.util.List;

import org.primaresearch.web.gwt.client.ui.RenderStyles.RenderStyle;
import org.primaresearch.web.gwt.shared.page.ContentObjectC;

/**
 * Highlights a content object if the mouse is hovering over it.  
 * 
 * @author Christian Clausner
 *
 */
public class ContentHighlightRendererPlugin implements RendererPlugin {

	private RenderStyle highlightStyle = null;
	private boolean enabled = true;
	
	@Override
	public void enable(boolean enable) {
		enabled = enable;
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Constructor for using the default content style of the renderer to draw the highlight.
	 */
	public ContentHighlightRendererPlugin() {
	}
	
	/**
	 * Constructor for using a custom style to render the highlight.
	 */
	public ContentHighlightRendererPlugin(RenderStyle style) {
		this.highlightStyle = style;
	}
	
	
	@Override
	public void render(PageRenderer renderer) {
		if (!enabled)
			return;
		
		List<ContentObjectC> objects = null;
		if (renderer.getPageLayout() != null) {
			objects = renderer.getPageLayout().getContent(renderer.getPageContentToRender());
		}
		
		if (objects == null)
			return;
		ContentObjectC obj;
		for (int i=0; i<objects.size(); i++) {
			obj = objects.get(i);
			//Highlight?
			if (obj.getId().equals(renderer.getContentObjectToHighlightFaintly())) { 
				
				RenderStyle style = highlightStyle == null 	? renderer.getRenderStyles().getStyle("pageContent."+obj.getType().getName())
															: highlightStyle;
				PolygonRendererHelper.drawPolygon(	renderer.getContext(), 
													obj.getCoords(), 
													style, renderer.getZoomFactor(), true, true);
			}
		}	
	}


}
