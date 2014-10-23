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

import java.util.HashMap;
import java.util.Map;

import org.primaresearch.web.gwt.client.ui.RenderStyles;

/**
 * Default content object render styles (for regions, text lines, words and glyphs).
 * Singleton.
 * 
 * @author Christian Clausner
 *
 */
public class DefaultRenderStyles implements RenderStyles {

	private static DefaultRenderStyles instance = null;
	
	private Map<String, RenderStyle> styles = new HashMap<String, RenderStyle>();
	
	
	private DefaultRenderStyles() {
		init();
	}
	
	public static RenderStyle getRenderStyle(String type) {
		return getInstance().getStyle(type);
	}
	
	/**
	 * Returns the singleton instance of this class
	 */
	public static DefaultRenderStyles getInstance() {
		if (instance == null)
			instance = new DefaultRenderStyles();
		return instance;
	}
	
	@Override
	public RenderStyle getStyle(String type) {
		RenderStyle ret = styles.get(type);
		if (ret == null)
			ret = styles.get("Plain");
		return ret;
	}
	
	/**
	 * Populates the style map with entries (ID + colours + line widths)
	 */
	private void init() {
		//Regions
		styles.put("pageContent.TextRegion", 	new RenderStyle("rgb(0,0,255)", "rgba(0,0,255,0.2)", 1));
		styles.put("pageContent.ChartRegion", 	new RenderStyle("rgb(128,0,128)", "rgba(128,0,128,0.2)", 1));
		styles.put("pageContent.FrameRegion", 	new RenderStyle("rgb(112,138,144)", "rgba(112,138,144,0.2)", 1));
		styles.put("pageContent.GraphicRegion",	new RenderStyle("rgb(0,128,0)", "rgba(0,128,0,0.2)", 1));
		styles.put("pageContent.ImageRegion", 	new RenderStyle("rgb(0,206,209)", "rgba(0,206,209,0.2)", 1));
		styles.put("pageContent.LineDrawingRegion", new RenderStyle("rgb(184,134,11)", "rgba(184,134,11,0.2)", 1));
		styles.put("pageContent.MathsRegion", 	new RenderStyle("rgb(0,191,255)", "rgba(0,191,255,0.2)", 1));
		styles.put("pageContent.NoiseRegion", 	new RenderStyle("rgb(255,0,0)", "rgba(255,0,0,0.2)", 1));
		styles.put("pageContent.SeparatorRegion", 	new RenderStyle("rgb(255,0,255)", "rgba(255,0,255,0.2)", 1));
		styles.put("pageContent.TableRegion", 	new RenderStyle("rgb(139,69,19)", "rgba(139,69,19,0.2)", 1));
		styles.put("pageContent.UnknownRegion", new RenderStyle("rgb(139,139,139)", "rgba(139,139,139,0.2)", 1));

		//Text line, word, glyph
		styles.put("pageContent.TextLine",	new RenderStyle("rgb(50,205,50)", "rgba(50,205,50,0.2)", 1));
		styles.put("pageContent.Word", 		new RenderStyle("rgb(174,34,34)", "rgba(174,34,34,0.2)", 1));
		styles.put("pageContent.Glyph", 	new RenderStyle("rgb(46,139,8)", "rgba(46,139,8,0.2)", 1));

		//Border and print space
		styles.put("pageContent.Border", 		new RenderStyle("rgb(255,99,71)", "rgba(255,99,71,0.2)", 1));
		styles.put("pageContent.PrintSpace", 	new RenderStyle("rgb(0,100,0)", "rgba(0,100,0,0.2)", 1));

		//Fallback
		styles.put("Plain", new RenderStyle("rgb(200,200,200)", "rgba(200,200,200,0.2)", 1));

	}

	
	
}
