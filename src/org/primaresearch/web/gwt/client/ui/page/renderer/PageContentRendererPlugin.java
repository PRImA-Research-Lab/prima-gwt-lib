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
package org.primaresearch.web.gwt.client.ui.page.renderer;

import java.util.List;

import org.primaresearch.web.gwt.client.page.PageLayoutC;
import org.primaresearch.web.gwt.client.ui.RenderStyles.RenderStyle;
import org.primaresearch.web.gwt.shared.page.ContentObjectC;

import com.google.gwt.canvas.dom.client.Context2d;

/**
 * Renderer plug-in for document page view that draws the content objects (outline + fill).
 * 
 * @author Christian Clausner
 *
 */
public class PageContentRendererPlugin implements RendererPlugin {
	private PageRenderer renderer;
	private Context2d context;
	private PageLayoutC pageLayout;
	private boolean enabled = true;
	private boolean highlightEnabled = true;
	private boolean greyedOut = false;
	private boolean drawOutline = true;
	private boolean fill = true;
	
	@Override
	public void enable(boolean enable) {
		enabled = enable;
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	/**
	 * Enables or disables the highlighting of the current object (usually when hovering over the object with the mouse).
	 */
	public void enableHighlight(boolean highlight) {
		highlightEnabled = highlight;
	}
	
	/**
	 * Enables or disables the greyedOut mode that renders the regions with grey outlines instead coloured ones.
	 */
	public void setGreyedOut(boolean greyedOut) {
		this.greyedOut = greyedOut;
	}

	@Override
	public void render(PageRenderer renderer) {
		if (!enabled)
			return;
		
		this.renderer = renderer;
		this.context = renderer.getContext();
		this.pageLayout = renderer.getPageLayout();
		
		//Draw page content
		if (pageLayout != null) {
			drawContentObjects(pageLayout.getContent(renderer.getPageContentToRender()));
		}
	}

	private void drawContentObjects(List<ContentObjectC> objects) {
		if (objects == null)
			return;
		ContentObjectC obj;
		for (int i=0; i<objects.size(); i++) {
			obj = objects.get(i);
			
			RenderStyle style = greyedOut 	? new RenderStyle("rgb(100,100,100)", "transparent", 1.0) 
											: getContentStyle(obj.getType().getName());
			
			RendererHelper.drawPolygon(context, obj.getCoords(), style, renderer.getZoomFactor(), drawOutline, fill);
			
			//Highlight?
			if (highlightEnabled) {
				if (obj.getId().equals(renderer.getContentObjectToHighlightFaintly())) //Just render twice
					RendererHelper.drawPolygon(context, obj.getCoords(), style, renderer.getZoomFactor(), false, true);
			}
		}
	}

	private RenderStyle getContentStyle(String type) {
		return renderer.getRenderStyles().getStyle("pageContent."+type);
	}

	public void setDrawOutline(boolean drawOutline) {
		this.drawOutline = drawOutline;
	}

	public void setFill(boolean fill) {
		this.fill = fill;
	}

	
}
