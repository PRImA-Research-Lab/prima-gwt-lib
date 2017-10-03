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
package org.primaresearch.web.gwt.server;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.w3c.dom.Node;

/**
 * Helper for sending simple SOAP requests and extracting the content of the SOAP response.
 * @author Christian Clausner
 *
 */
public class SimpleSoapRequest {
	
	private String targetUrl;
	private String method;
	private List<String> methodParameterNames = new ArrayList<String>();
	private List<String> methodParameterValues = new ArrayList<String>();
	private boolean DEBUG = false;
	
	/**
	 * Constructor
	 * @param targetUrl SOAP web service URL
	 * @param method Method to be called
	 */
	public SimpleSoapRequest(String targetUrl, String method) {
		this.targetUrl = targetUrl;
		this.method = method;
	}
	
	/**
	 * Adds a parameter for the method that is to be called.
	 * Each parameter is added as a child element of the method element in the SOAP request (XML).
	 * The parameter value is added as text content of the child element.
	 */
	public void addMethodParameter(String name, String value) {
		methodParameterNames.add(name);
		methodParameterValues.add(value);
	}

	/**
	 * Sends the request to the SOAP service and handles the response.
	 * @return Content of the response.
	 * @throws UnsupportedOperationException
	 * @throws SOAPException
	 * @throws MalformedURLException
	 */
	public String send() throws UnsupportedOperationException, SOAPException, MalformedURLException {
		
		SOAPConnectionFactory soapConnectionFact = SOAPConnectionFactory.newInstance();
		
		SOAPConnection connection = soapConnectionFact.createConnection();
		
		MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage outgoingMessage = messageFactory.createMessage();
        
        SOAPPart soappart = outgoingMessage.getSOAPPart();

        SOAPEnvelope envelope = soappart.getEnvelope();

        //SOAPHeader header = envelope.getHeader();

        SOAPBody body = envelope.getBody();

        SOAPBodyElement methodelement = body.addBodyElement(envelope.createName(method, "p",
        													"www.primaresearch.org"));
        		
        for (int i=0; i<methodParameterNames.size(); i++) {
        	methodelement.addChildElement(methodParameterNames.get(i)).addTextNode(methodParameterValues.get(i));
        }
        
        URL soapTarget = new URL(targetUrl);
        
        //Authentication
        //String authorization = new String(Base64.encodeBase64("test:test123".getBytes()));  
        //outgoingMessage.getMimeHeaders().addHeader("Authorization", "Basic "+authorization);
        
        if (DEBUG)
        	System.out.println("SOAP Target URL: "+targetUrl);
        
        
		SOAPMessage incomingMessage = connection.call(outgoingMessage, soapTarget);
		
		/*try {
			System.out.println();
			outgoingMessage.writeTo(System.out);
			System.out.println();
			incomingMessage.writeTo(System.out);
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		
		//Handle response
		String soapResponseContent = null;
		incomingMessage.getSOAPBody().getFirstChild();
					
		Node root = incomingMessage.getSOAPBody().getFirstChild();
		if (root != null) {
			Node node = root.getFirstChild();
			while (node != null) {
				if ("return".equals(node.getNodeName())) {
					soapResponseContent = node.getTextContent();
					break;
				}
				node = node.getNextSibling();
			}
		}

		return soapResponseContent;
	}

	public void setDEBUG(boolean dEBUG) {
		DEBUG = dEBUG;
	}
	
	
}
