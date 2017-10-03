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

import java.util.Iterator;
import java.util.List;

import org.primaresearch.maths.geometry.Point;
import org.primaresearch.web.gwt.client.page.PageLayoutC;
import org.primaresearch.web.gwt.client.ui.RenderStyles.RenderStyle;
import org.primaresearch.web.gwt.client.ui.page.renderer.RendererHelper.ArrowShape;
import org.primaresearch.web.gwt.shared.page.ContentObjectC;
import org.primaresearch.web.gwt.shared.page.GroupC;
import org.primaresearch.web.gwt.shared.page.GroupMemberC;
import org.primaresearch.web.gwt.shared.page.RegionRefC;

import com.google.gwt.canvas.dom.client.Context2d;

/**
 * Renderer plug-in for document page reading order. 
 * 
 * @author Christian Clausner
 *
 */
public class ReadingOrderRendererPlugin implements RendererPlugin {

	private PageRenderer renderer;
	private Context2d context;
	private PageLayoutC pageLayout;
	private boolean enabled = true;
	
	@Override
	public void enable(boolean enable) {
		enabled = enable;
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void render(PageRenderer renderer) {
		if (!enabled)
			return;
		
		this.renderer = renderer;
		this.context = renderer.getContext();
		this.pageLayout = renderer.getPageLayout();
		
		if (pageLayout != null) 
			drawReadingOrder();
	}

	/**
	 * Draws the reading order
	 */
	private void drawReadingOrder() {

		context.setLineWidth(3);

		drawReadingOrderElement(pageLayout.getReadingOrder(), 0);
	}
	
	/**
	 * Draws the given reading order element and all its child elements.
	 */
	private void drawReadingOrderElement(GroupMemberC element, int level)
	{
		if (element == null) //Should not happen
			return;

		//Group
		if (element instanceof GroupC) {
			GroupC group = (GroupC)element;

			//Draw children recursively
			if (group.members != null) {
				for (Iterator<GroupMemberC> it = group.members.iterator(); it.hasNext() ; )
					drawReadingOrderElement(it.next(), level+1);
			}

			//Draw Group:
			// Ordered Group (arrows from child to child)
			if (group.ordered)
			{
				GroupMemberC child1 = null;
				GroupMemberC child2 = null;
				Point center1 = null;
				Point center2 = null;
				ArrowShape arrow = new ArrowShape();
				arrow.fill = true;
				arrow.width = 15;
				arrow.theta = 0.78;

				List<GroupMemberC> members = group.members;
				if (members != null) {
					for (Iterator<GroupMemberC> it = members.iterator(); it.hasNext(); ) {
						child2 = it.next();
						if (child2 != null)	{
							center2 = getReadingOrderEndPoint(child2);
							if (center2 == null) //happens for empty groups
								continue;
						}
						if (child1 != null && center1 != null && center2 != null) {
							//Draw arrow from centre of child 1 to centre of child 2
							context.setStrokeStyle(getReadingOrderGroupColor(level).getLineColor());
							context.setFillStyle(getReadingOrderGroupColor(level).getLineColor());
	
							RendererHelper.drawArrow(context, center1, center2, arrow);
	
							//Dot
							context.setFillStyle(getReadingOrderGroupColor(level).getLineColor());
							RenderStyle style = renderer.getRenderStyles().getStyle("readingOrder.Center");
							context.setStrokeStyle(style.getLineColor());
							context.setFillStyle(style.getFillColor());
							context.beginPath();
							context.arc(center2.x, center2.y, 9, 0, Math.PI * 2.0, true);
							context.fill();
							context.closePath();
						}
						child1 = child2;
						center1 = getReadingOrderStartPoint(child1);
					}
				}
			}
			//Unordered Group (star - lines from the group centre to the child centres)
			else { 
				Point groupCenter = getReadingOrderStartPoint(group);
				Point center2 = null;
				if (groupCenter != null) {
					context.setStrokeStyle(getReadingOrderGroupColor(level).getLineColor());
					context.setFillStyle(getReadingOrderGroupColor(level).getFillColor());

					//'Star'
					int x1, y1;
					x1 = groupCenter.x;
					y1 = groupCenter.y;
					List<GroupMemberC> members = group.members;
					if (members != null) {
						for (Iterator<GroupMemberC> it = members.iterator(); it.hasNext(); ) {
							center2 = getReadingOrderEndPoint(it.next());
							if (center2 != null) {
								context.moveTo(x1, y1);
								context.lineTo(center2.x, center2.y);
							}
						}
					}
					//Circle
					RenderStyle style = renderer.getRenderStyles().getStyle("readingOrder.Center");
					context.setStrokeStyle(style.getLineColor());
					context.setFillStyle("white");
					context.beginPath();
					context.arc(x1, y1, 15, 0, Math.PI * 2.0, true);
					context.fill();
					context.stroke();
					context.closePath();
				}
			}
		}
		//RegionRef (just draw a dot)
		else {
			//Dot
			Point center = getReadingOrderEndPoint(element);
			if (center != null) {
				RenderStyle style = renderer.getRenderStyles().getStyle("readingOrder.Center");
				context.setStrokeStyle(style.getLineColor());
				context.setFillStyle(style.getFillColor());
				context.beginPath();
				context.arc(center.x, center.y, 9, 0, Math.PI * 2.0, true);
				context.fill();
				context.closePath();
			}
		}
	}
	
	/**
	 * Returns the colour for the reading oder group connectors.
	 */
	private RenderStyle getReadingOrderGroupColor(int level) {
		level = level%3;
		return renderer.getRenderStyles().getStyle("readingOrder.Level"+level);
	}
	
	/**
	 * Returns the centre of the specified reading order element.
	 * For a RegionRef element this is simply the centre of the region.
	 * For groups it is the centre of all child centres.
	 */
	Point getReadingOrderStartPoint(GroupMemberC element) {
		if (element == null)
			return null;

		Point ret = null;

		//RegionRef
		if (element instanceof RegionRefC) {
			ContentObjectC region = pageLayout.findContentObject(((RegionRefC)element).regionId);
			if (region != null)	{
				return region.getCoords().getBoundingBox().getCenter();
			}
		}
		//Group 
		else if (element instanceof GroupC) {
			GroupC group = (GroupC)element;
			List<GroupMemberC> members = group.members;
			if (members == null || members.size() == 0) { //No centre available -> skip the group
				return null;
			}
			if (group.ordered) //ordered -> start point = centre of last child
			{
				for (Iterator<GroupMemberC> it = members.iterator(); it.hasNext(); ) {
					ret = getReadingOrderEndPoint(it.next());
					if (ret != null)
						return ret;
				}
			}
			else { //unordered -> start point = average of all child centres
				int x = 0, y = 0, count = 0;
				for (Iterator<GroupMemberC> it = members.iterator(); it.hasNext(); ) {
					Point childCenter = getReadingOrderEndPoint(it.next());
					if (childCenter != null) {
						x += childCenter.x;
						y += childCenter.y;
						count++;
					}
				}	
				if (x == 0 && y == 0 || count == 0) //No centre available
					return null;
				x /= count;
				y /= count;
				ret = new Point(x, y);
			}
		}
		return ret;		
	}

	
	/**
	 * Returns the centre of the specified reading order element.
	 * For a RegionRef element this is simply the centre of the region.
	 * For groups it is the centre of all child centres.
	 */
	Point getReadingOrderEndPoint(GroupMemberC element) {
		if (element == null)
			return null;

		Point ret = null;

		//RegionRef (centre = centre of bounding box)
		if (element instanceof RegionRefC) {
			ContentObjectC region = pageLayout.findContentObject(((RegionRefC)element).regionId);
			if (region != null)	
				ret = region.getCoords().getBoundingBox().getCenter();
		}
		//Group 
		else if (element instanceof GroupC) {
			GroupC group = (GroupC)element;
			List<GroupMemberC> members = group.members;
			if (members == null || members.size() == 0) { //No centre available -> skip the group
				return null;
			}
			if (group.ordered) //ordered -> end point = centre of first child
			{
				for (Iterator<GroupMemberC> it = members.iterator(); it.hasNext(); ) {
					ret = getReadingOrderEndPoint(it.next());
					if (ret != null)
						return ret;
				}
			}
			else { //unordered (centre = average of all child centres)
				int x = 0, y = 0, count = 0;
				for (Iterator<GroupMemberC> it = members.iterator(); it.hasNext(); ) {
					Point childCenter = getReadingOrderEndPoint(it.next());
					if (childCenter != null) {
						x += childCenter.x;
						y += childCenter.y;
						count++;
					}
				}	
				if (x == 0 && y == 0 || count == 0) { //No centre available
					return null;
				}
				x /= count;
				y /= count;
				ret = new Point(x, y);
			}
		}
		return ret;
	}
}
