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
package org.primaresearch.web.gwt.client.ui;


/**
 * Interface for rendering style factories.
 * 
 * @author Christian Clausner
 *
 */
public interface RenderStyles {

	public RenderStyle getStyle(String type);
	
	/**
	 * Rendering style (including line colour, fill colour and line width).
	 * 
	 * @author Christian Clausner
	 *
	 */
	public static class RenderStyle {
		private String fillColor;
		private String lineColor;
		double lineWidth;
		
		public RenderStyle(String lineColor, String fillColor, double lineWidth) {
			this.fillColor = fillColor;
			this.lineColor = lineColor;
			this.lineWidth = lineWidth;
		}

		public String getFillColor() {
			return fillColor;
		}

		public void setFillColor(String fillColor) {
			this.fillColor = fillColor;
		}

		public String getLineColor() {
			return lineColor;
		}

		public void setLineColor(String lineColor) {
			this.lineColor = lineColor;
		}

		public double getLineWidth() {
			return lineWidth;
		}

		public void setLineWidth(double lineWidth) {
			this.lineWidth = lineWidth;
		}
	}
}
